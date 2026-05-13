package hu.homeplugin.managers;

import hu.homeplugin.HomePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HomeManager {

    private final HomePlugin plugin;
    private final File homesFile;
    private FileConfiguration homesConfig;
    private final Map<UUID, Map<String, HomeData>> cache = new HashMap<>();

    public HomeManager(HomePlugin plugin) {
        this.plugin = plugin;
        this.homesFile = new File(plugin.getDataFolder(), "homes.yml");
        loadHomes();
    }

    private void loadHomes() {
        if (!homesFile.exists()) {
            try {
                homesFile.getParentFile().mkdirs();
                homesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create homes.yml: " + e.getMessage());
            }
        }
        homesConfig = YamlConfiguration.loadConfiguration(homesFile);

        if (homesConfig.contains("homes")) {
            for (String uuidStr : homesConfig.getConfigurationSection("homes").getKeys(false)) {
                UUID uuid;
                try { uuid = UUID.fromString(uuidStr); }
                catch (IllegalArgumentException e) { continue; }

                Map<String, HomeData> playerHomes = new HashMap<>();
                String basePath = "homes." + uuidStr;

                if (homesConfig.contains(basePath)) {
                    for (String homeName : homesConfig.getConfigurationSection(basePath).getKeys(false)) {
                        String path = basePath + "." + homeName;
                        String world = homesConfig.getString(path + ".world");
                        double x = homesConfig.getDouble(path + ".x");
                        double y = homesConfig.getDouble(path + ".y");
                        double z = homesConfig.getDouble(path + ".z");
                        float yaw = (float) homesConfig.getDouble(path + ".yaw");
                        float pitch = (float) homesConfig.getDouble(path + ".pitch");
                        playerHomes.put(homeName.toLowerCase(), new HomeData(homeName, world, x, y, z, yaw, pitch));
                    }
                }
                cache.put(uuid, playerHomes);
            }
        }
    }

    public void saveAll() {
        homesConfig.set("homes", null);
        for (Map.Entry<UUID, Map<String, HomeData>> entry : cache.entrySet()) {
            String uuidStr = entry.getKey().toString();
            for (Map.Entry<String, HomeData> homeEntry : entry.getValue().entrySet()) {
                HomeData data = homeEntry.getValue();
                String path = "homes." + uuidStr + "." + homeEntry.getKey();
                homesConfig.set(path + ".world", data.getWorldName());
                homesConfig.set(path + ".x", data.getX());
                homesConfig.set(path + ".y", data.getY());
                homesConfig.set(path + ".z", data.getZ());
                homesConfig.set(path + ".yaw", data.getYaw());
                homesConfig.set(path + ".pitch", data.getPitch());
            }
        }
        try { homesConfig.save(homesFile); }
        catch (IOException e) { plugin.getLogger().severe("Could not save homes.yml: " + e.getMessage()); }
    }

    private void savePlayer(UUID uuid) {
        Map<String, HomeData> homes = cache.getOrDefault(uuid, new HashMap<>());
        String uuidStr = uuid.toString();
        homesConfig.set("homes." + uuidStr, null);
        for (Map.Entry<String, HomeData> homeEntry : homes.entrySet()) {
            HomeData data = homeEntry.getValue();
            String path = "homes." + uuidStr + "." + homeEntry.getKey();
            homesConfig.set(path + ".world", data.getWorldName());
            homesConfig.set(path + ".x", data.getX());
            homesConfig.set(path + ".y", data.getY());
            homesConfig.set(path + ".z", data.getZ());
            homesConfig.set(path + ".yaw", data.getYaw());
            homesConfig.set(path + ".pitch", data.getPitch());
        }
        try { homesConfig.save(homesFile); }
        catch (IOException e) { plugin.getLogger().severe("Could not save homes.yml: " + e.getMessage()); }
    }

    public void reload() {
        cache.clear();
        loadHomes();
    }

    public Map<String, HomeData> getHomes(Player player) {
        return cache.getOrDefault(player.getUniqueId(), new HashMap<>());
    }

    public HomeData getHome(Player player, String name) {
        return cache.getOrDefault(player.getUniqueId(), new HashMap<>()).get(name.toLowerCase());
    }

    public boolean hasHome(Player player, String name) {
        return getHome(player, name) != null;
    }

    public void setHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        cache.computeIfAbsent(uuid, k -> new HashMap<>())
                .put(name.toLowerCase(), new HomeData(name, player.getLocation()));
        savePlayer(uuid);
    }

    public boolean deleteHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        Map<String, HomeData> homes = cache.get(uuid);
        if (homes == null) return false;
        boolean removed = homes.remove(name.toLowerCase()) != null;
        if (removed) savePlayer(uuid);
        return removed;
    }

    public int getHomeCount(Player player) {
        return cache.getOrDefault(player.getUniqueId(), new HashMap<>()).size();
    }

    public int getHomeLimit(Player player) {
        if (player.hasPermission("homeplugin.homes.unlimited")) return Integer.MAX_VALUE;
        int max = plugin.getConfig().getInt("default-home-limit", 5);
        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission("homeplugin.homes.limit." + i)) { max = i; break; }
        }
        return max;
    }

    public boolean isAtLimit(Player player) {
        return getHomeCount(player) >= getHomeLimit(player);
    }
}
