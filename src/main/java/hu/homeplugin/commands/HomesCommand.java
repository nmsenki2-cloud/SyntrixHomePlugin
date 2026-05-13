package hu.homeplugin.commands;

import hu.homeplugin.HomePlugin;
import hu.homeplugin.managers.HomeData;
import hu.homeplugin.utils.MessageUtil;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class HomesCommand implements CommandExecutor {

    private final HomePlugin plugin;

    public HomesCommand(HomePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Player only!"); return true; }
        if (!player.hasPermission("homeplugin.homes")) { MessageUtil.send(player, "no-permission"); return true; }

        Map<String, HomeData> homes = plugin.getHomeManager().getHomes(player);
        int limit = plugin.getHomeManager().getHomeLimit(player);
        String limitStr = limit == Integer.MAX_VALUE ? "∞" : String.valueOf(limit);

        if (homes.isEmpty()) {
            player.sendMessage(MessageUtil.colorize(
                    plugin.getConfig().getString("messages.prefix", "") +
                    "&7Nincs home-od. Használd a &e/sethome <név> &7parancsot!"));
            return true;
        }

        player.sendMessage(MessageUtil.colorize("&8&m----&r &6&lHome-ok &8(" + homes.size() + "/" + limitStr + ") &8&m----"));

        StringJoiner joiner = new StringJoiner("&8, &e", "&e", "");
        homes.values().stream()
                .sorted(Comparator.comparing(HomeData::getName))
                .forEach(h -> joiner.add(h.getName()));
        player.sendMessage(MessageUtil.colorize(" " + joiner));
        player.sendMessage(MessageUtil.colorize("&8&m---------------------------------"));
        return true;
    }
}
