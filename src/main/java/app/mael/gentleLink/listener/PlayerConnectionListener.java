package app.mael.gentleLink.listener;

import app.mael.gentleLink.service.LinkService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerConnectionListener implements Listener {
    private final LinkService linkService;
    private final Set<UUID> frozenPlayers;
    private final MiniMessage miniMessage;
    private final String chatMessage;
    private final String titleMessage;
    private final String subtitleMessage;

    public PlayerConnectionListener(LinkService linkService, String chatMessage, String titleMessage, String subtitleMessage) {
        this.linkService = linkService;
        this.frozenPlayers = new HashSet<>();
        this.miniMessage = MiniMessage.miniMessage();
        this.chatMessage = chatMessage;
        this.titleMessage = titleMessage;
        this.subtitleMessage = subtitleMessage;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (!linkService.isPlayerLinked(player.getUniqueId())) {
            applyRestrictions(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            if (event.getFrom().getX() != event.getTo().getX() ||
                event.getFrom().getY() != event.getTo().getY() ||
                event.getFrom().getZ() != event.getTo().getZ()) {
                event.setCancelled(true);
            }
        }
    }

    public void applyRestrictions(Player player) {
        frozenPlayers.add(player.getUniqueId());
        
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.BLINDNESS,
            PotionEffect.INFINITE_DURATION,
            0,
            false,
            false,
            false
        ));
        
        Component chatComponent = miniMessage.deserialize(chatMessage);
        player.sendMessage(chatComponent);
        
        Component titleComponent = miniMessage.deserialize(titleMessage);
        Component subtitleComponent = miniMessage.deserialize(subtitleMessage);
        
        Title title = Title.title(
            titleComponent,
            subtitleComponent,
            Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofDays(999),
                Duration.ofMillis(500)
            )
        );
        
        player.showTitle(title);
    }

    public void removeRestrictions(Player player) {
        frozenPlayers.remove(player.getUniqueId());
        
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.clearTitle();
    }

    public boolean isPlayerFrozen(UUID playerUuid) {
        return frozenPlayers.contains(playerUuid);
    }

    public void clearFrozenPlayer(UUID playerUuid) {
        frozenPlayers.remove(playerUuid);
    }
}

