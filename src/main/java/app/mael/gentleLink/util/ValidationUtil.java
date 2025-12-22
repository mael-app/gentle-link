package app.mael.gentleLink.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    private static final Pattern DISCORD_ID_PATTERN = Pattern.compile("^\\d{17,19}$");
    private static final Pattern LINK_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{6}$");
    private static final Pattern PLAYER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{1,16}$");
    
    public static boolean isValidDiscordId(String discordId) {
        if (discordId == null || discordId.isEmpty()) {
            return false;
        }
        return DISCORD_ID_PATTERN.matcher(discordId).matches();
    }
    
    public static boolean isValidLinkCode(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        return LINK_CODE_PATTERN.matcher(code).matches();
    }
    
    public static boolean isValidPlayerName(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return false;
        }
        return PLAYER_NAME_PATTERN.matcher(playerName).matches();
    }
    
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.trim();
    }
}
