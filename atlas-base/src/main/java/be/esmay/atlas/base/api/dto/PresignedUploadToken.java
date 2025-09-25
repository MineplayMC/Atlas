package be.esmay.atlas.base.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public final class PresignedUploadToken {

    private final String token;
    private final String uploadId;
    private final String serverId;
    private final String targetPath;
    private final UploadType uploadType;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final Map<String, String> metadata;
    private boolean used;

    public enum UploadType {
        SERVER_FILE,
        SERVER_CHUNK,
        TEMPLATE_FILE
    }

    public static PresignedUploadToken create(String serverId, String targetPath, UploadType uploadType, long expirationSeconds, Map<String, String> metadata) {
        return PresignedUploadToken.builder()
            .token(UUID.randomUUID().toString())
            .uploadId(metadata != null ? metadata.get("uploadId") : null)
            .serverId(serverId)
            .targetPath(targetPath)
            .uploadType(uploadType)
            .createdAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(expirationSeconds))
            .metadata(metadata)
            .used(false)
            .build();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    public boolean isValid() {
        return !this.used && !this.isExpired();
    }

    public void markUsed() {
        this.used = true;
    }
}