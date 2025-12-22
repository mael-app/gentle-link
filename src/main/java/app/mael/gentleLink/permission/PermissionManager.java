package app.mael.gentleLink.permission;

import org.bukkit.command.CommandSender;

public class PermissionManager {
    public static final String LINK = "gentlelink.link";
    public static final String LINK_INFO = "gentlelink.link.info";
    public static final String LINK_INFO_OTHERS = "gentlelink.link.info.others";
    public static final String LINK_LIST = "gentlelink.link.list";
    public static final String LINK_BYPASS = "gentlelink.link.bypass";
    public static final String UNLINK = "gentlelink.unlink";
    public static final String UNLINK_OTHERS = "gentlelink.unlink.others";
    public static final String ADMIN = "gentlelink.admin";

    public static boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission);
    }

    public static boolean canLink(CommandSender sender) {
        return hasPermission(sender, LINK);
    }

    public static boolean canViewOwnInfo(CommandSender sender) {
        return hasPermission(sender, LINK_INFO);
    }

    public static boolean canViewOthersInfo(CommandSender sender) {
        return hasPermission(sender, LINK_INFO_OTHERS);
    }

    public static boolean canListLinks(CommandSender sender) {
        return hasPermission(sender, LINK_LIST);
    }

    public static boolean canBypassLink(CommandSender sender) {
        return hasPermission(sender, LINK_BYPASS);
    }

    public static boolean canUnlink(CommandSender sender) {
        return hasPermission(sender, UNLINK);
    }

    public static boolean canUnlinkOthers(CommandSender sender) {
        return hasPermission(sender, UNLINK_OTHERS);
    }

    public static boolean isAdmin(CommandSender sender) {
        return hasPermission(sender, ADMIN);
    }
}

