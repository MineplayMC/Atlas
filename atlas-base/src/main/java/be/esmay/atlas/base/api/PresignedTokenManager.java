package be.esmay.atlas.base.api;

import be.esmay.atlas.base.api.dto.PresignedUploadToken;
import be.esmay.atlas.base.utils.Logger;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PresignedTokenManager {

    private final Map<String, PresignedUploadToken> tokens = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("PresignedTokenCleanup");
        thread.setDaemon(true);
        return thread;
    });

    public PresignedTokenManager() {
        this.startCleanupTask();
    }

    private void startCleanupTask() {
        this.cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.MINUTES);
    }

    public PresignedUploadToken createToken(String serverId, String targetPath, PresignedUploadToken.UploadType uploadType, long expirationSeconds, Map<String, String> metadata) {
        if (expirationSeconds <= 0 || expirationSeconds > 3600) {
            throw new IllegalArgumentException("Expiration must be between 1 and 3600 seconds");
        }

        PresignedUploadToken token = PresignedUploadToken.create(serverId, targetPath, uploadType, expirationSeconds, metadata);
        this.tokens.put(token.getToken(), token);

        Logger.debug("Created presigned upload token {} for {} (expires in {}s)", token.getToken(), targetPath, expirationSeconds);

        return token;
    }

    public PresignedUploadToken getToken(String tokenString) {
        if (tokenString == null || tokenString.isEmpty()) {
            return null;
        }

        PresignedUploadToken token = this.tokens.get(tokenString);
        if (token == null) {
            return null;
        }

        if (token.isExpired()) {
            this.tokens.remove(tokenString);
            Logger.debug("Token {} has expired", tokenString);
            return null;
        }

        return token;
    }

    public PresignedUploadToken validateAndConsumeToken(String tokenString, boolean singleUse) {
        PresignedUploadToken token = this.getToken(tokenString);
        if (token == null || !token.isValid()) {
            return null;
        }

        if (singleUse) {
            token.markUsed();
            this.tokens.remove(tokenString);
            Logger.debug("Token {} consumed (single use)", tokenString);
        }

        return token;
    }

    private void cleanupExpiredTokens() {
        Instant now = Instant.now();
        int removed = 0;

        for (Map.Entry<String, PresignedUploadToken> entry : this.tokens.entrySet()) {
            if (entry.getValue().isExpired()) {
                this.tokens.remove(entry.getKey());
                removed++;
            }
        }

        if (removed > 0) {
            Logger.debug("Cleaned up {} expired presigned tokens", removed);
        }
    }

    public void shutdown() {
        this.cleanupExecutor.shutdown();
        try {
            if (!this.cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                this.cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}