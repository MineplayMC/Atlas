package be.esmay.atlas.base.scaler;

import be.esmay.atlas.base.AtlasBase;
import be.esmay.atlas.base.config.impl.ScalerConfig;
import be.esmay.atlas.base.provider.ServiceProvider;
import be.esmay.atlas.base.scaler.impl.ProxyScaler;
import be.esmay.atlas.base.utils.Logger;
import be.esmay.atlas.common.models.AtlasServer;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Getter
public final class ScalerManager {

    private final Set<Scaler> scalers = new HashSet<>();

    private ScheduledExecutorService scheduledExecutor;
    private ScheduledFuture<?> scalingTask;
    private ForkJoinPool parallelScalingPool;

    private volatile boolean isShuttingDown = false;

    private final AtlasBase atlasBase;

    public ScalerManager(AtlasBase atlasBase) {
        this.atlasBase = atlasBase;
    }

    public void initialize() {
        this.loadScalers();
        this.ensureAllResourcesReady();
        this.startScalingTask();
    }

    private void loadScalers() {
        this.scalers.clear();

        File groupsFolder = new File(System.getProperty("user.dir"), "groups");
        if (!groupsFolder.exists()) {
            groupsFolder.mkdirs();
        }

        Collection<File> groupFiles = FileUtils.listFiles(groupsFolder, new String[]{"yml"}, true).stream()
                .filter(file -> !file.getName().startsWith("_"))
                .toList();

        for (File file : groupFiles) {
            ScalerConfig scalerConfig = new ScalerConfig(file.getParentFile(), file.getName());

            String type = scalerConfig.getGroup().getScaling().getType();
            if (type == null) {
                Logger.error("No scaling type defined in group file " + file.getName());
                continue;
            }

            Scaler scaler = ScalerRegistry.get(type, scalerConfig.getGroup().getDisplayName(), scalerConfig);
            if (scaler == null) {
                Logger.error("Failed to create scaler for type " + type + " in group file " + file.getName());
                continue;
            }

            this.scalers.add(scaler);
            Logger.info("Loaded scaler {} with type {}", scaler.getGroupName(), type);
        }
    }

    private void ensureAllResourcesReady() {
        ServiceProvider provider = this.atlasBase.getProviderManager().getProvider();
        
        Logger.info("Ensuring all resources are ready before starting scalers...");
        
        for (Scaler scaler : this.scalers) {
            try {
                provider.ensureResourcesReady(scaler.getScalerConfig().getGroup()).get();
            } catch (Exception e) {
                Logger.error("Failed to prepare resources for scaler {}: {}", scaler.getGroupName(), e.getMessage());
                throw new RuntimeException("Cannot start scaling - resource preparation failed", e);
            }
        }
        
        Logger.info("All Docker images are ready - starting scalers");
    }

    private void startScalingTask() {
        int checkInterval = this.atlasBase.getConfigManager().getAtlasConfig().getAtlas().getScaling().getCheckInterval();

        this.scheduledExecutor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r, "Atlas-Scaler");
            thread.setDaemon(true);
            return thread;
        });

        int poolSize = Math.min(Runtime.getRuntime().availableProcessors(), Math.max(1, this.scalers.size()));
        this.parallelScalingPool = new ForkJoinPool(poolSize);

        Logger.info("Starting scaling task with interval: {} seconds, parallel pool size: {}", checkInterval, poolSize);

        this.scalingTask = this.scheduledExecutor.scheduleWithFixedDelay(
                this::performScalingCheck,
                checkInterval,
                checkInterval,
                TimeUnit.SECONDS
        );
    }

    private void performScalingCheck() {
        if (this.isShuttingDown) {
            return;
        }

        try {
            Logger.debug("Performing parallel scaling check for {} scalers", this.scalers.size());
            long startTime = System.currentTimeMillis();

            List<Scaler> proxyScalers = this.scalers.stream()
                    .filter(scaler -> scaler instanceof ProxyScaler)
                    .sorted(Comparator.comparingInt(scaler -> scaler.getScalerConfig().getGroup().getPriority()))
                    .toList();

            for (Scaler proxyScaler : proxyScalers) {
                if (this.isShuttingDown) {
                    break;
                }
                try {
                    Logger.debug(proxyScaler.getScalingStatus());
                    proxyScaler.scaleServers();
                } catch (Exception e) {
                    Logger.error("Error during scaling check for proxy group: {}", proxyScaler.getGroupName(), e);
                }
            }

            List<Scaler> regularScalers = this.scalers.stream()
                    .filter(scaler -> !(scaler instanceof ProxyScaler))
                    .sorted(Comparator.comparingInt(scaler -> scaler.getScalerConfig().getGroup().getPriority()))
                    .toList();

            if (!regularScalers.isEmpty() && !this.isShuttingDown) {
                List<CompletableFuture<Void>> scalingFutures = regularScalers.stream()
                    .map(scaler -> CompletableFuture.runAsync(() -> {
                        if (!this.isShuttingDown) {
                            try {
                                Logger.debug(scaler.getScalingStatus());
                                scaler.scaleServers();
                            } catch (Exception e) {
                                Logger.error("Error during parallel scaling check for group: {}", scaler.getGroupName(), e);
                            }
                        }
                    }, this.parallelScalingPool))
                    .toList();

                CompletableFuture.allOf(scalingFutures.toArray(new CompletableFuture[0]))
                    .exceptionally(throwable -> {
                        Logger.error("Error in parallel scaling execution", throwable);
                        return null;
                    })
                    .join();
            }

            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > 5000) {
                Logger.warn("Scaling check took {} ms for {} scalers", elapsed, this.scalers.size());
            } else {
                Logger.debug("Scaling check completed in {} ms for {} scalers", elapsed, this.scalers.size());
            }
        } catch (Exception e) {
            Logger.error("Unexpected error during parallel scaling check", e);
        }
    }

    public Scaler getScaler(String groupName) {
        Scaler exactMatch = this.scalers.stream()
                .filter(scaler -> scaler.getGroupName().equals(groupName))
                .findFirst()
                .orElse(null);
        
        if (exactMatch != null) {
            return exactMatch;
        }

        Scaler caseInsensitiveMatch = this.scalers.stream()
                .filter(scaler -> scaler.getGroupName().equalsIgnoreCase(groupName))
                .findFirst()
                .orElse(null);
        
        if (caseInsensitiveMatch != null) {
            return caseInsensitiveMatch;
        }

        return this.scalers.stream()
                .filter(scaler -> scaler.getScalerConfig().getGroup().getName() != null && scaler.getScalerConfig().getGroup().getName().equalsIgnoreCase(groupName))
                .findFirst()
                .orElse(null);
    }

    public void reloadScalers() {
        Logger.info("Reloading scaler configurations");
        for (Scaler scaler : this.scalers) {
            try {
                scaler.shutdown();
            } catch (Exception e) {
                Logger.error("Error shutting down scaler during reload: {}", scaler.getGroupName(), e);
            }
        }
        this.loadScalers();
        this.atlasBase.getCronScheduler().reloadCronJobs();
        Logger.info("Scaler configurations reloaded successfully");
    }

    public void shutdown() {
        Logger.info("Shutting down ScalerManager");
        this.isShuttingDown = true;

        if (this.scalingTask != null) {
            this.scalingTask.cancel(false);
        }

        if (this.parallelScalingPool != null) {
            this.parallelScalingPool.shutdown();
            try {
                if (!this.parallelScalingPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    this.parallelScalingPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                this.parallelScalingPool.shutdownNow();
            }
        }

        if (this.scheduledExecutor != null) {
            this.scheduledExecutor.shutdown();
            try {
                if (!this.scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    this.scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                this.scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        for (Scaler scaler : this.scalers) {
            try {
                scaler.shutdown();
            } catch (Exception e) {
                Logger.error("Error shutting down scaler for group: {}", scaler.getGroupName(), e);
            }
        }

        this.scalers.clear();
    }

    public AtlasServer getServerFromTracking(String serverId) {
        for (Scaler scaler : this.scalers) {
            AtlasServer server = scaler.getServer(serverId);
            if (server != null) {
                return server;
            }
        }
        return null;
    }

    public List<AtlasServer> getAllServersFromTracking() {
        return this.scalers.stream()
                .flatMap(scaler -> scaler.getServers().stream())
                .toList();
    }

    public List<AtlasServer> getServersByGroupFromTracking(String group) {
        return this.scalers.stream()
                .filter(scaler -> scaler.getGroupName().equals(group))
                .flatMap(scaler -> scaler.getServers().stream())
                .toList();
    }

    public void loadScaler(String groupName) {
        Scaler existingScaler = this.getScaler(groupName);
        if (existingScaler != null) {
            Logger.warn("Scaler {} is already loaded", groupName);
            return;
        }

        File groupFile = new File("groups", groupName + ".yml");
        if (!groupFile.exists()) {
            Logger.error("Group configuration not found: " + groupFile.getPath());
            return;
        }

        ScalerConfig scalerConfig = new ScalerConfig(groupFile.getParentFile(), groupFile.getName());
        String type = scalerConfig.getGroup().getScaling().getType();
        
        if (type == null) {
            Logger.error("No scaling type defined in group file " + groupFile.getName());
            return;
        }

        Scaler scaler = ScalerRegistry.get(type, scalerConfig.getGroup().getDisplayName(), scalerConfig);
        if (scaler == null) {
            Logger.error("Failed to create scaler for type " + type + " in group file " + groupFile.getName());
            return;
        }

        try {
            ServiceProvider provider = this.atlasBase.getProviderManager().getProvider();
            provider.ensureResourcesReady(scalerConfig.getGroup()).get();
        } catch (Exception e) {
            Logger.error("Failed to prepare resources for scaler {}: {}", scaler.getGroupName(), e.getMessage());
            return;
        }

        this.scalers.add(scaler);
        this.atlasBase.getCronScheduler().scheduleCronJobsForScaler(scaler);
        Logger.info("Loaded scaler {} with type {}", scaler.getGroupName(), type);
    }

    public void unloadScaler(String groupName) {
        Scaler scaler = this.getScaler(groupName);
        if (scaler == null) {
            Logger.error("Scaler not found: " + groupName);
            return;
        }

        try {
            scaler.shutdown();
        } catch (Exception e) {
            Logger.error("Error shutting down scaler during unload: {}", scaler.getGroupName(), e);
        }

        this.scalers.remove(scaler);
        this.atlasBase.getCronScheduler().unscheduleCronJobsForGroup(groupName);
        Logger.info("Scaler {} unloaded successfully", groupName);
    }

}

