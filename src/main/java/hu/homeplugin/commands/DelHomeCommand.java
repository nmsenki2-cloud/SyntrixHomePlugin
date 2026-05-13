package hu.homeplugin.commands;

import hu.homeplugin.HomePlugin;
import hu.homeplugin.utils.MessageUtil;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class DelHomeCommand implements CommandExecutor, TabCompleter {

    private final HomePlugin plugin;

    public DelHomeCommand(HomePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Player only!"); return true; }
        if (!player.hasPermission("homeplugin.delhome")) { MessageUtil.send(player, "no-permission"); return true; }
        if (args.length == 0) { player.sendMessage(MessageUtil.colorize("&cHasználat: /delhome <név>")); return true; }

        if (!plugin.getHomeManager().hasHome(player, args[0])) {
            MessageUtil.send(player, "home-not-found", "{name}", args[0]);
            return true;
        }

        plugin.getHomeManager().deleteHome(player, args[0]);
        MessageUtil.send(player, "home-deleted", "{name}", args[0]);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player) || args.length != 1) return new ArrayList<>();
        List<String> homes = new ArrayList<>(plugin.getHomeManager().getHomes(player).keySet());
        homes.removeIf(h -> !h.startsWith(args[0].toLowerCase()));
        return homes;
    }
}
