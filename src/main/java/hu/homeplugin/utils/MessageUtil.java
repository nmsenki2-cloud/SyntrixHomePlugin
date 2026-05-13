package hu.homeplugin.utils;

import hu.homeplugin.HomePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtil {

    private static final LegacyComponentSerializer SERIALIZER =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();

    public static Component colorize(String text) {
        return SERIALIZER.deserialize(text);
    }

    public static String colorizeString(String text) {
        return text.replace('&', '§');
    }

    public static void send(Player player, String key, String... replacements) {
        HomePlugin plugin = HomePlugin.getInstance();
        String prefix = plugin.getConfig().getString("messages.prefix", "[Home] ");
        String message = plugin.getConfig().getString("messages." + key, "&c[Missing: " + key + "]");
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        player.sendMessage(colorize(prefix + message));
    }

    public static void sendRaw(Player player, String message) {
        player.sendMessage(colorize(message));
    }

    public static boolean isValidHomeName(String name) {
        return name.matches("[a-zA-Z0-9_]+");
    }
}
