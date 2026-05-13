package hu.homeplugin.commands;

import hu.homeplugin.HomePlugin;
import hu.homeplugin.utils.MessageUtil;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class HomeReloadCommand implements CommandExecutor {

    private final HomePlugin plugin;

    public HomeReloadCommand(HomePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("homeplugin.admin.reload")) {
            if (sender instanceof Player player) MessageUtil.send(player, "no-permission");
            else sender.sendMessage("No permission!");
            return true;
        }
        plugin.reload();
        if (sender instanceof Player player) MessageUtil.send(player, "plugin-reloaded");
        else sender.sendMessage("[SyntrixHome] Reloaded!");
        return true;
    }
}
