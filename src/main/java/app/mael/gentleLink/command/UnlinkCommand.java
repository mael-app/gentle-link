package app.mael.gentleLink.command;

import app.mael.gentleLink.permission.PermissionManager;
import app.mael.gentleLink.service.LinkService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnlinkCommand implements CommandExecutor {
    private final LinkService linkService;

    public UnlinkCommand(LinkService linkService) {
        this.linkService = linkService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Cette commande doit être exécutée par un joueur.", NamedTextColor.RED));
            return true;
        }

        if (!PermissionManager.canUnlink(sender)) {
            sender.sendMessage(Component.text("Vous n'avez pas la permission d'utiliser cette commande.", NamedTextColor.RED));
            return true;
        }

        if (!linkService.isPlayerLinked(player.getUniqueId())) {
            player.sendMessage(Component.text("✗ ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text("Votre compte n'est pas lié à Discord.", NamedTextColor.RED)));
            return true;
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
            player.sendMessage(Component.text("Vous pouvez le lier à nouveau avec /link", NamedTextColor.GRAY, TextDecoration.ITALIC));
            player.sendMessage(Component.empty());
        } else {
            player.sendMessage(Component.text("Une erreur est survenue lors de la suppression de la liaison.", NamedTextColor.RED));
        }

        return true;
    }
}

