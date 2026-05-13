package hu.homeplugin;

import hu.homeplugin.commands.*;
import hu.homeplugin.gui.HomeGUIListener;
import hu.homeplugin.managers.HomeManager;
import hu.homeplugin.managers.CooldownManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HomePlugin extends JavaPlugin {

    private static HomePlugin instance;
    private HomeManager homeManager;
    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        homeManager = new HomeManager(this);
        cooldownManager = new CooldownManager(this);

        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("home").setTabCompleter(new HomeCommand(this));
        getCommand("sethome").setExecutor(new SetHomeCommand(this));
        getCommand("sethome").setTabCompleter(new SetHomeCommand(this));
        getCommand("delhome").setExecutor(new DelHomeCommand(this));
        getCommand("delhome").setTabCompleter(new DelHomeCommand(this));
        getCommand("homes").setExecutor(new HomesCommand(this));
        getCommand("homereload").setExecutor(new HomeReloadCommand(this));

        getServer().getPluginManager().registerEvents(new HomeGUIListener(this), this);

        getLogger().info("SyntrixHomePlugin enabled!");
    }

    @Override
    public void onDisable() {
        if (homeManager != null) homeManager.saveAll();
        getLogger().info("SyntrixHomePlugin disabled.");
    }

    public static HomePlugin getInstance() { return instance; }
    public HomeManager getHomeManager() { return homeManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }

    public void reload() {
        reloadConfig();
        homeManager.reload();
        cooldownManager.reload();
    }
}
