package hu.homeplugin.managers;

import hu.homeplugin.HomePlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final HomePlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public CooldownManager(HomePlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() { cooldowns.clear(); }

    public boolean isOnCooldown(UUID uuid) {
        if (!cooldowns.containsKey(uuid)) return false;
        int cd = plugin.getConfig().getInt("teleport-cooldown", 3);
        if (cd <= 0) return false;
        return System.currentTimeMillis() - cooldowns.get(uuid) < cd * 1000L;
    }

    public long getRemainingSeconds(UUID uuid) {
        if (!cooldowns.containsKey(uuid)) return 0;
        int cd = plugin.getConfig().getInt("teleport-cooldown", 3);
        long elapsed = System.currentTimeMillis() - cooldowns.get(uuid);
        return Math.max(0, (cd * 1000L - elapsed) / 1000 + 1);
    }

    public void setCooldown(UUID uuid) { cooldowns.put(uuid, System.currentTimeMillis()); }
    public void clearCooldown(UUID uuid) { cooldowns.remove(uuid); }
}
