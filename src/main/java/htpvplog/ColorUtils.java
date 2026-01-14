package htpvplog;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    public static String colorize(String message) {
        if (message == null) return "";
        
        // 1.16+ Hex Color Desteği
        Matcher matcher = HEX_PATTERN.matcher(message);
        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, ChatColor.of(color) + "");
            matcher = HEX_PATTERN.matcher(message);
        }
        
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
