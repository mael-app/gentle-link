package app.mael.gentleLink.service;

import app.mael.gentleLink.database.DatabaseAdapter;
import app.mael.gentleLink.model.AccountLink;
import app.mael.gentleLink.model.LinkCode;
import app.mael.gentleLink.util.ValidationUtil;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LinkService {
    private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRATION_MINUTES = 5;
    private static final int RATE_LIMIT_SECONDS = 30;
    
    private final DatabaseAdapter database;
    private final SecureRandom random;
    private final Map<UUID, LocalDateTime> lastCodeGeneration;

    public LinkService(DatabaseAdapter database) {
        this.database = database;
        this.random = new SecureRandom();
        this.lastCodeGeneration = new ConcurrentHashMap<>();
    }

    public String generateLinkCode(UUID playerUuid) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastGeneration = lastCodeGeneration.get(playerUuid);
        
        if (lastGeneration != null && now.isBefore(lastGeneration.plusSeconds(RATE_LIMIT_SECONDS))) {
            long secondsLeft = java.time.Duration.between(now, lastGeneration.plusSeconds(RATE_LIMIT_SECONDS)).getSeconds();
            throw new IllegalStateException("Veuillez attendre " + secondsLeft + " secondes avant de regénérer un code.");
        }
        
        Optional<LinkCode> existingCode = database.getLinkCodeByPlayer(playerUuid);
        
        if (existingCode.isPresent() && !existingCode.get().isExpired()) {
            return existingCode.get().code();
        }
        
        String code = generateRandomCode();
        LocalDateTime expiresAt = now.plusMinutes(CODE_EXPIRATION_MINUTES);
        
        LinkCode linkCode = new LinkCode(playerUuid, code, expiresAt);
        database.createLinkCode(linkCode);
        
        lastCodeGeneration.put(playerUuid, now);
        
        return code;
    }

    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CODE_CHARACTERS.length());
            code.append(CODE_CHARACTERS.charAt(index));
        }
        return code.toString();
    }

    public boolean validateAndLink(String code, String discordId) {
        if (!ValidationUtil.isValidLinkCode(code)) {
            return false;
        }
        
        if (!ValidationUtil.isValidDiscordId(discordId)) {
            return false;
        }
        
        database.deleteExpiredLinkCodes();
        
        Optional<LinkCode> linkCodeOpt = database.getLinkCode(code);
        if (linkCodeOpt.isEmpty()) {
            return false;
        }
        
        LinkCode linkCode = linkCodeOpt.get();
        if (linkCode.isExpired()) {
            database.deleteLinkCode(code);
            return false;
        }
        
        Optional<AccountLink> existingLink = database.getAccountLinkByDiscord(discordId);
        if (existingLink.isPresent()) {
            return false;
        }
        
        AccountLink accountLink = new AccountLink(
            linkCode.playerUuid(),
            discordId,
            LocalDateTime.now(),
            false
        );
        
        database.createAccountLink(accountLink);
        database.deleteLinkCode(code);
        
        return true;
    }

    public void createManualLink(UUID playerUuid, String discordId) {
        if (!ValidationUtil.isValidDiscordId(discordId)) {
            throw new IllegalArgumentException("Invalid Discord ID format");
        }
        
        AccountLink accountLink = new AccountLink(
            playerUuid,
            discordId,
            LocalDateTime.now(),
            true
        );
        
        database.createAccountLink(accountLink);
    }

    public boolean unlinkAccount(UUID playerUuid) {
        if (!database.isPlayerLinked(playerUuid)) {
            return false;
        }
        
        database.deleteAccountLink(playerUuid);
        return true;
    }

    public Optional<AccountLink> getAccountLink(UUID playerUuid) {
        return database.getAccountLink(playerUuid);
    }

    public List<AccountLink> getAllLinks() {
        return database.getAllAccountLinks();
    }

    public boolean isPlayerLinked(UUID playerUuid) {
        return database.isPlayerLinked(playerUuid);
    }

    public void cleanupExpiredCodes() {
        database.deleteExpiredLinkCodes();
    }
}

