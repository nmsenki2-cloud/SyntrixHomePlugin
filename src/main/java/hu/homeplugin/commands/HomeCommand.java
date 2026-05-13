package hu.homeplugin.commands;

import hu.homeplugin.HomePlugin;
import hu.homeplugin.gui.HomeGUI;
import hu.homeplugin.managers.HomeData;
import hu.homeplugin.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class HomeCommand implements CommandExecutor, TabCompleter {

    private final HomePlugin plugin;
    private final HomeGUI gui;

    public HomeCommand(HomePlugin plugin) {
        this.plugin = plugin;
        this.gui = new HomeGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Player only!"); return true; }
        if (!player.hasPermission("homeplugin.home")) { MessageUtil.send(player, "no-permission"); return true; }

        if (args.length == 0) {
            player.openInventory(gui.buildMainGUI(player, 0));
            return true;
        }

        HomeData home = plugin.getHomeManager().getHome(player, args[0]);
        if (home == null) { MessageUtil.send(player, "home-not-found", "{name}", args[0]); return true; }

        if (!player.hasPermission("homeplugin.bypass.cooldown") &&
                plugin.getCooldownManager().isOnCooldown(player.getUniqueId())) {
            long rem = plugin.getCooldownManager().getRemainingSeconds(player.getUniqueId());
            MessageUtil.send(player, "cooldown-active", "{seconds}", String.valueOf(rem));
            return true;
        }

        Location loc = home.toLocation();
        if (loc == null) { player.sendMessage(MessageUtil.colorize("&cA világ nem található!")); return true; }
        player.teleport(loc);
        plugin.getCooldownManager().setCooldown(player.getUniqueId());
        MessageUtil.send(player, "home-teleported", "{name}", home.getName());
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
