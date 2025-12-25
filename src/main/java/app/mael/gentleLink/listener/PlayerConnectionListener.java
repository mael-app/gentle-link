package app.mael.gentleLink.listener;

import app.mael.gentleLink.service.LinkService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerConnectionListener implements Listener {
    private final LinkService linkService;
    private final MiniMessage miniMessage;
    private final String kickMessage;

    public PlayerConnectionListener(LinkService linkService, String kickMessage) {
        this.linkService = linkService;
        this.miniMessage = MiniMessage.miniMessage();
        this.kickMessage = kickMessage;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!linkService.isPlayerLinked(event.getPlayer().getUniqueId())) {
            String code = linkService.generateLinkCode(event.getPlayer().getUniqueId());
            
            String messageWithCode = kickMessage.replace("{CODE}", code);
            Component kickComponent = miniMessage.deserialize(messageWithCode);
            
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickComponent);
        }
    }
}

