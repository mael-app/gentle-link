package app.mael.gentleLink.command;

import app.mael.gentleLink.Main;
import app.mael.gentleLink.model.AccountLink;
import app.mael.gentleLink.permission.PermissionManager;
import app.mael.gentleLink.service.LinkService;
import app.mael.gentleLink.util.ValidationUtil;
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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LinkCommand implements CommandExecutor, TabCompleter {
    private final LinkService linkService;
    private final Main plugin;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public LinkCommand(LinkService linkService, Main plugin) {
        this.linkService = linkService;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /link <info|list|bypass>", NamedTextColor.RED));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "info" -> {
                if (args.length == 1) {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(Component.text("Cette commande doit être exécutée par un joueur.", NamedTextColor.RED));
                        return true;
                    }
                    if (!PermissionManager.canViewOwnInfo(sender)) {
                        sender.sendMessage(Component.text("Vous n'avez pas la permission d'utiliser cette commande.", NamedTextColor.RED));
                        return true;
                    }
                    handleInfoSelf(player);
                } else {
                    if (!PermissionManager.canViewOthersInfo(sender)) {
                        sender.sendMessage(Component.text("Vous n'avez pas la permission de voir les informations des autres joueurs.", NamedTextColor.RED));
                        return true;
                    }
                    handleInfoPlayer(sender, args[1]);
                }
            }
            case "list" -> {
                if (!PermissionManager.canListLinks(sender)) {
                    sender.sendMessage(Component.text("Vous n'avez pas la permission d'utiliser cette commande.", NamedTextColor.RED));
                    return true;
                }
                handleListLinks(sender);
            }
            case "bypass" -> {
                if (!PermissionManager.canBypassLink(sender)) {
                    sender.sendMessage(Component.text("Vous n'avez pas la permission d'utiliser cette commande.", NamedTextColor.RED));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /link bypass <pseudo> <discord_id>", NamedTextColor.RED));
                    return true;
                }
                handleBypassLink(sender, args[1], args[2]);
            }
            default -> sender.sendMessage(Component.text("Sous-commande inconnue. Utilisez: /link, /link info, /link list, /link bypass", NamedTextColor.RED));
        }
        
        return true;
    }


    private void handleInfoSelf(Player player) {
        Optional<AccountLink> link = linkService.getAccountLink(player.getUniqueId());
        
        if (link.isEmpty()) {
            player.sendMessage(Component.text("✗ ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text("Votre compte n'est pas lié à Discord.", NamedTextColor.RED)));
            player.sendMessage(Component.text("Utilisez ", NamedTextColor.GRAY)
                .append(Component.text("/link", NamedTextColor.GREEN, TextDecoration.BOLD)
                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand("/link")))
                .append(Component.text(" pour générer un code de liaison.", NamedTextColor.GRAY)));
            return;
        }
        
        displayLinkInfo(player, link.get(), null);
    }

    private void handleInfoPlayer(CommandSender sender, String playerName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        
        if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
            sender.sendMessage(Component.text("Le joueur '" + playerName + "' n'a jamais joué sur ce serveur.", NamedTextColor.RED));
            return;
        }
        
        Optional<AccountLink> link = linkService.getAccountLink(offlinePlayer.getUniqueId());
        
        if (link.isEmpty()) {
            sender.sendMessage(Component.text("✗ ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text("Le compte de ", NamedTextColor.RED))
                .append(Component.text(playerName, NamedTextColor.WHITE, TextDecoration.BOLD))
                .append(Component.text(" n'est pas lié à Discord.", NamedTextColor.RED)));
            return;
        }
        
        displayLinkInfo(sender, link.get(), playerName);
    }

    private void displayLinkInfo(CommandSender sender, AccountLink accountLink, String targetPlayerName) {
        String linkType = accountLink.isManual() ? "Manuel" : "Automatique";
        String linkedDate = accountLink.linkedAt().format(DATE_FORMATTER);
        
        Component discordIdComponent = Component.text(accountLink.discordId(), NamedTextColor.AQUA, TextDecoration.BOLD)
            .clickEvent(net.kyori.adventure.text.event.ClickEvent.copyToClipboard(accountLink.discordId()))
            .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                Component.text("Cliquez pour copier", NamedTextColor.GRAY, TextDecoration.ITALIC)));
        
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD, TextDecoration.BOLD));
        
        if (targetPlayerName != null) {
            sender.sendMessage(Component.text("  Informations de liaison - ", NamedTextColor.YELLOW, TextDecoration.BOLD)
                .append(Component.text(targetPlayerName, NamedTextColor.WHITE, TextDecoration.BOLD)));
        } else {
            sender.sendMessage(Component.text("  Informations de liaison", NamedTextColor.YELLOW, TextDecoration.BOLD));
        }
        
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Discord ID: ", NamedTextColor.GRAY).append(discordIdComponent));
        sender.sendMessage(Component.text("Type: ", NamedTextColor.GRAY)
            .append(Component.text(linkType, NamedTextColor.YELLOW)));
        sender.sendMessage(Component.text("Lié le: ", NamedTextColor.GRAY)
            .append(Component.text(linkedDate, NamedTextColor.GREEN)));
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.empty());
    }

    private void handleListLinks(CommandSender sender) {
        List<AccountLink> links = linkService.getAllLinks();
        
        if (links.isEmpty()) {
            sender.sendMessage(Component.text("Aucun compte n'est actuellement lié.", NamedTextColor.YELLOW));
            return;
        }

        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.text("  Comptes liés (" + links.size() + ")", NamedTextColor.YELLOW, TextDecoration.BOLD));
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.empty());

        for (AccountLink link : links) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(link.playerUuid());
            String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Inconnu";
            String linkType = link.isManual() ? "Manuel" : "Automatique";
            String linkedDate = link.linkedAt().format(DATE_FORMATTER);
            
            Component discordIdComponent = Component.text(link.discordId(), NamedTextColor.AQUA)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.copyToClipboard(link.discordId()))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                    Component.text("Cliquez pour copier", NamedTextColor.GRAY, TextDecoration.ITALIC)));
            
            sender.sendMessage(Component.text("▸ ", NamedTextColor.DARK_GRAY)
                .append(Component.text(playerName, NamedTextColor.WHITE, TextDecoration.BOLD))
                .append(Component.text(" ➜ ", NamedTextColor.GRAY))
                .append(discordIdComponent));
            
            sender.sendMessage(Component.text("  Type: ", NamedTextColor.GRAY)
                .append(Component.text(linkType, NamedTextColor.YELLOW))
                .append(Component.text(" | Lié le: ", NamedTextColor.GRAY))
                .append(Component.text(linkedDate, NamedTextColor.GREEN)));
            
            sender.sendMessage(Component.empty());
        }

        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.empty());
    }

    private void handleBypassLink(CommandSender sender, String playerName, String discordId) {
        playerName = ValidationUtil.sanitizeInput(playerName);
        discordId = ValidationUtil.sanitizeInput(discordId);
        
        if (!ValidationUtil.isValidPlayerName(playerName)) {
            sender.sendMessage(Component.text("Nom de joueur invalide.", NamedTextColor.RED));
            return;
        }
        
        if (!ValidationUtil.isValidDiscordId(discordId)) {
            sender.sendMessage(Component.text("ID Discord invalide. L'ID doit être composé de 17 à 19 chiffres.", NamedTextColor.RED));
            return;
        }
        
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        
        if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
            sender.sendMessage(Component.text("Le joueur '" + playerName + "' n'a jamais joué sur ce serveur.", NamedTextColor.RED));
            return;
        }

        UUID playerUuid = offlinePlayer.getUniqueId();
        
        if (linkService.isPlayerLinked(playerUuid)) {
            sender.sendMessage(Component.text("Ce joueur est déjà lié à un compte Discord!", NamedTextColor.RED));
            return;
        }

        try {
            linkService.createManualLink(playerUuid, discordId);
            
            sender.sendMessage(Component.text("✓ ", NamedTextColor.GREEN, TextDecoration.BOLD)
                .append(Component.text("Liaison créée avec succès!", NamedTextColor.GREEN)));
            sender.sendMessage(Component.text("  Joueur: ", NamedTextColor.GRAY)
                .append(Component.text(playerName, NamedTextColor.WHITE))
                .append(Component.text(" ➜ Discord ID: ", NamedTextColor.GRAY))
                .append(Component.text(discordId, NamedTextColor.AQUA)));
            
            Player onlinePlayer = Bukkit.getPlayer(playerUuid);
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                onlinePlayer.sendMessage(Component.empty());
                onlinePlayer.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD, TextDecoration.BOLD));
                onlinePlayer.sendMessage(Component.text("✓ ", NamedTextColor.GREEN, TextDecoration.BOLD)
                    .append(Component.text("Compte Lié!", NamedTextColor.GREEN, TextDecoration.BOLD)));
                onlinePlayer.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GOLD, TextDecoration.BOLD));
                onlinePlayer.sendMessage(Component.empty());
                onlinePlayer.sendMessage(Component.text("Votre compte a été lié manuellement à Discord!", NamedTextColor.WHITE));
                onlinePlayer.sendMessage(Component.text("Vous pouvez maintenant accéder au serveur.", NamedTextColor.GREEN));
                onlinePlayer.sendMessage(Component.empty());
            }
        } catch (Exception e) {
            sender.sendMessage(Component.text("Erreur lors de la création de la liaison: " + e.getMessage(), NamedTextColor.RED));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            if (PermissionManager.canViewOwnInfo(sender)) {
                completions.add("info");
            }
            if (PermissionManager.canListLinks(sender)) {
                completions.add("list");
            }
            if (PermissionManager.canBypassLink(sender)) {
                completions.add("bypass");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("bypass") && PermissionManager.canBypassLink(sender)) {
                return null;
            } else if (args[0].equalsIgnoreCase("info") && PermissionManager.canViewOthersInfo(sender)) {
                return null;
            }
        }
        
        return completions;
    }
}

