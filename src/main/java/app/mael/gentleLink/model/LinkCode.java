package app.mael.gentleLink.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record LinkCode(UUID playerUuid, String code, LocalDateTime expiresAt) {
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
