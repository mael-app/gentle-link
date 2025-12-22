package app.mael.gentleLink.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountLink(UUID playerUuid, String discordId, LocalDateTime linkedAt, boolean isManual) {
}

