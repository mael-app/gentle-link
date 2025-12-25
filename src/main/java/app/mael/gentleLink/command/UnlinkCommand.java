package app.mael.gentleLink.command;

import app.mael.gentleLink.Main;
import app.mael.gentleLink.permission.PermissionManager;
import app.mael.gentleLink.service.LinkService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UnlinkCommand implements CommandExecutor, TabCompleter {
    private final LinkService linkService;
    private final Main plugin;

    public UnlinkCommand(LinkService linkService, Main plugin) {
        this.linkService = linkService;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /unlink <playername> (admin)
        if (args.length > 0) {
            if (!PermissionManager.canUnlinkOthers(sender)) {
                sender.sendMessage(Component.text("Vous n'avez pas la permission de délier les autres joueurs.", NamedTextColor.RED));
                return true;
            }
            handleUnlinkOther(sender, args[0]);
            return true;
        }
        
        // /unlink (self)
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Cette commande doit être exécutée par un joueur.", NamedTextColor.RED));
            return true;
        }

        if (!PermissionManager.canUnlink(sender)) {
            sender.sendMessage(Component.text("Vous n'avez pas la permission d'utiliser cette commande.", NamedTextColor.RED));
            return true;
        }

        handleUnlinkSelf(player);
        return true;
    }

    private void handleUnlinkSelf(Player player) {
        if (!linkService.isPlayerLinked(player.getUniqueId())) {
            player.sendMessage(Component.text("✗ ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text("Votre compte n'est pas lié à Discord.", NamedTextColor.RED)));
            return;
        }

        boolean success = linkService.unlinkAccount(player.getUniqueId());
        
        if (success) {
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD, TextDecoration.BOLD));
            player.sendMessage(Component.text("✓ ", NamedTextColor.GREEN, TextDecoration.BOLD)
                .append(Component.text("Liaison supprimée", NamedTextColor.GREEN, TextDecoration.BOLD)));
            player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD, TextDecoration.BOLD));
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("Votre compte Minecraft a été délié de Discord.", NamedTextColor.GRAY));
            player.sendMessage(Component.text("Reconnectez-vous pour obtenir un nouveau code de liaison.", NamedTextColor.YELLOW, TextDecoration.ITALIC));
            player.sendMessage(Component.empty());
            
            Bukkit.getScheduler().runTask(plugin, () -> 
                player.kick(Component.text("Compte délié. Reconnectez-vous pour obtenir votre code de liaison.", NamedTextColor.YELLOW))
            );
        } else {
            player.sendMessage(Component.text("Une erreur est survenue lors de la suppression de la liaison.", NamedTextColor.RED));
        }
    }

    private void handleUnlinkOther(CommandSender sender, String playerName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if (!linkService.isPlayerLinked(offlinePlayer.getUniqueId())) {
            sender.sendMessage(Component.text("✗ ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text("Le compte de ", NamedTextColor.RED))
                .append(Component.text(playerName, NamedTextColor.WHITE, TextDecoration.BOLD))
                .append(Component.text(" n'est pas lié à Discord.", NamedTextColor.RED)));
            return;
        }

        boolean success = linkService.unlinkAccount(offlinePlayer.getUniqueId());
        
        if (success) {
            sender.sendMessage(Component.text("✓ ", NamedTextColor.GREEN, TextDecoration.BOLD)
                .append(Component.text("Liaison supprimée pour ", NamedTextColor.GREEN))
                .append(Component.text(playerName, NamedTextColor.WHITE, TextDecoration.BOLD)));
            
            Player onlinePlayer = Bukkit.getPlayer(offlinePlayer.getUniqueId());
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                onlinePlayer.sendMessage(Component.empty());
                onlinePlayer.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD, TextDecoration.BOLD));
                onlinePlayer.sendMessage(Component.text("⚠ ", NamedTextColor.YELLOW, TextDecoration.BOLD)
                    .append(Component.text("Liaison supprimée", NamedTextColor.YELLOW, TextDecoration.BOLD)));
                onlinePlayer.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD, TextDecoration.BOLD));
                onlinePlayer.sendMessage(Component.empty());
                onlinePlayer.sendMessage(Component.text("Votre compte a été délié par un administrateur.", NamedTextColor.GRAY));
                onlinePlayer.sendMessage(Component.text("Reconnectez-vous pour obtenir un nouveau code de liaison.", NamedTextColor.YELLOW, TextDecoration.ITALIC));
                onlinePlayer.sendMessage(Component.empty());
                
                Bukkit.getScheduler().runTask(plugin, () -> 
                    onlinePlayer.kick(Component.text("Compte délié par un administrateur. Reconnectez-vous pour obtenir votre code de liaison.", NamedTextColor.YELLOW))
                );
            }
        } else {
            sender.sendMessage(Component.text("Une erreur est survenue lors de la suppression de la liaison.", NamedTextColor.RED));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1 && PermissionManager.canUnlinkOthers(sender)) {
            return null; // Return player names
        }
        
        return completions;
    }
}

