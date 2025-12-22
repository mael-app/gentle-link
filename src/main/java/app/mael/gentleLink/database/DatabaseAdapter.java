package app.mael.gentleLink.database;

import app.mael.gentleLink.model.AccountLink;
import app.mael.gentleLink.model.LinkCode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DatabaseAdapter {
    void initialize();
    
    void shutdown();
    
    void createLinkCode(LinkCode linkCode);
    
    Optional<LinkCode> getLinkCode(String code);
    
    Optional<LinkCode> getLinkCodeByPlayer(UUID playerUuid);
    
    void deleteLinkCode(String code);
    
    void deleteExpiredLinkCodes();
    
    void createAccountLink(AccountLink accountLink);
    
    Optional<AccountLink> getAccountLink(UUID playerUuid);
    
    Optional<AccountLink> getAccountLinkByDiscord(String discordId);
    
    List<AccountLink> getAllAccountLinks();
    
    void deleteAccountLink(UUID playerUuid);
    
    boolean isPlayerLinked(UUID playerUuid);
}

