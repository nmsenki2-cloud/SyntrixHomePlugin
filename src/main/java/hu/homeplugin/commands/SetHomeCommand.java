package hu.homeplugin.commands;

import hu.homeplugin.HomePlugin;
import hu.homeplugin.utils.MessageUtil;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class SetHomeCommand implements CommandExecutor, TabCompleter {

    private final HomePlugin plugin;

    public SetHomeCommand(HomePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Player only!"); return true; }
        if (!player.hasPermission("homeplugin.sethome")) { MessageUtil.send(player, "no-permission"); return true; }
        if (args.length == 0) { player.sendMessage(MessageUtil.colorize("&cHasználat: /sethome <név>")); return true; }

        String name = args[0];
        if (!MessageUtil.isValidHomeName(name)) { MessageUtil.send(player, "home-name-invalid"); return true; }
        if (name.length() > 16) { MessageUtil.send(player, "home-name-too-long"); return true; }

        boolean isNew = !plugin.getHomeManager().hasHome(player, name);
        if (isNew && plugin.getHomeManager().isAtLimit(player)) {
            MessageUtil.send(player, "home-limit-reached", "{limit}",
                    String.valueOf(plugin.getHomeManager().getHomeLimit(player)));
            return true;
        }

        plugin.getHomeManager().setHome(player, name);
        MessageUtil.send(player, "home-set", "{name}", name);
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
