package be.esmay.atlas.base.metrics;

import be.esmay.atlas.base.utils.Logger;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class CreationMetrics {

    private static final Map<String, TimingData> TIMING_METRICS = new ConcurrentHashMap<>();
    private static final AtomicLong TOTAL_CREATED = new AtomicLong(0);
    private static final AtomicLong TOTAL_FAILED = new AtomicLong(0);

    @Data
    public static class TimingData {
        private final AtomicLong count = new AtomicLong(0);
        private final AtomicLong totalTime = new AtomicLong(0);
        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxTime = new AtomicLong(0);

        public void recordTiming(long timeMs) {
            this.count.incrementAndGet();
            this.totalTime.addAndGet(timeMs);
            this.minTime.updateAndGet(current -> Math.min(current, timeMs));
            this.maxTime.updateAndGet(current -> Math.max(current, timeMs));
        }

        public double getAverageTime() {
            long count = this.count.get();
            return count > 0 ? (double) this.totalTime.get() / count : 0.0;
        }
    }

    public static void recordServerCreation(String operation, long durationMs, boolean success) {
        TimingData timing = TIMING_METRICS.computeIfAbsent(operation, k -> new TimingData());
        timing.recordTiming(durationMs);

        if (success) {
            TOTAL_CREATED.incrementAndGet();
        } else {
            TOTAL_FAILED.incrementAndGet();
        }

        if (durationMs > 30000) {
            Logger.warn("Slow {} operation: {}ms", operation, durationMs);
        } else if (durationMs > 10000) {
            Logger.debug("Moderate {} operation: {}ms", operation, durationMs);
        }
    }

    public static void recordBatchCreation(String group, int count, long totalDurationMs) {
        String operation = "batch_creation_" + group;
        TimingData timing = TIMING_METRICS.computeIfAbsent(operation, k -> new TimingData());
        timing.recordTiming(totalDurationMs);

        TOTAL_CREATED.addAndGet(count);

        double avgPerServer = (double) totalDurationMs / count;
        Logger.info("Batch creation metrics for {}: {} servers in {}ms (avg: {:.1f}ms per server)",
                group, count, totalDurationMs, avgPerServer);

        if (avgPerServer > 15000) {
            Logger.warn("Very slow batch creation for group {}: {:.1f}ms average per server", group, avgPerServer);
        }
    }

    public static void recordScalingCheck(String group, long durationMs) {
        String operation = "scaling_check_" + group;
        TimingData timing = TIMING_METRICS.computeIfAbsent(operation, k -> new TimingData());
        timing.recordTiming(durationMs);

        if (durationMs > 5000) {
            Logger.warn("Slow scaling check for group {}: {}ms", group, durationMs);
        }
    }

    public static void recordTemplateApplication(String group, long durationMs) {
        String operation = "template_application_" + group;
        TimingData timing = TIMING_METRICS.computeIfAbsent(operation, k -> new TimingData());
        timing.recordTiming(durationMs);

        if (durationMs > 10000) {
            Logger.warn("Slow template application for group {}: {}ms", group, durationMs);
        }
    }

    public static String getMetricsReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Atlas Creation Metrics ===\n");
        report.append(String.format("Total Created: %d | Total Failed: %d%n",
                TOTAL_CREATED.get(), TOTAL_FAILED.get()));

        if (!TIMING_METRICS.isEmpty()) {
            report.append("\nOperation Timings:\n");
            TIMING_METRICS.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        String operation = entry.getKey();
                        TimingData data = entry.getValue();
                        report.append(String.format("  %s: %d ops, avg: %.1fms, min: %dms, max: %dms%n",
                                operation, data.getCount().get(), data.getAverageTime(),
                                data.getMinTime().get() == Long.MAX_VALUE ? 0 : data.getMinTime().get(),
                                data.getMaxTime().get()));
                    });
        }

        return report.toString();
    }

    public static void logPeriodicReport() {
        if (TOTAL_CREATED.get() > 0 || TOTAL_FAILED.get() > 0) {
            Logger.info(getMetricsReport());
        }
    }

    public static void reset() {
        TIMING_METRICS.clear();
        TOTAL_CREATED.set(0);
        TOTAL_FAILED.set(0);
    }

    public static long getTotalCreated() {
        return TOTAL_CREATED.get();
    }

    public static long getTotalFailed() {
        return TOTAL_FAILED.get();
    }

    public static Map<String, TimingData> getAllTimings() {
        return new ConcurrentHashMap<>(TIMING_METRICS);
    }
}