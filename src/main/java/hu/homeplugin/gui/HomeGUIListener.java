package hu.homeplugin.gui;

import hu.homeplugin.HomePlugin;
import hu.homeplugin.managers.HomeData;
import hu.homeplugin.utils.MessageUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HomeGUIListener implements Listener {

    private final HomePlugin plugin;
    private final HomeGUI gui;

    public HomeGUIListener(HomePlugin plugin) {
        this.plugin = plugin;
        this.gui = new HomeGUI(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inv = event.getInventory();
        if (inv == null || event.getCurrentItem() == null) return;

        String title = PlainTextComponentSerializer.plainText()
                .serialize(player.getOpenInventory().title());

        if (HomeGUI.isMainGUI(title)) {
            event.setCancelled(true);
            handleMain(player, event.getSlot(), event.getCurrentItem(),
                    title, event.isLeftClick(), event.isRightClick());
        } else if (HomeGUI.isConfirmGUI(title)) {
            event.setCancelled(true);
            handleConfirm(player, event.getSlot(), title);
        }
    }

    private void handleMain(Player player, int slot, ItemStack item,
                             String title, boolean left, boolean right) {
        if (item == null || item.getType().isAir()) return;
        int page = HomeGUI.getPageFromTitle(title);

        if (slot == 49) { player.closeInventory(); return; }
        if (slot == 48) {
            Bukkit.getScheduler().runTask(plugin, () ->
                    player.openInventory(gui.buildMainGUI(player, page - 1)));
            return;
        }
        if (slot == 50) {
            Bukkit.getScheduler().runTask(plugin, () ->
                    player.openInventory(gui.buildMainGUI(player, page + 1)));
            return;
        }

        boolean isHomeSlot = false;
        for (int s : HomeGUI.HOME_SLOTS) if (s == slot) { isHomeSlot = true; break; }
        if (!isHomeSlot) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String rawName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
        String homeName = rawName.replaceAll("[^a-zA-Z0-9_]", "").trim();

        HomeData homeData = plugin.getHomeManager().getHome(player, homeName);
        if (homeData == null) return;

        if (left) {
            player.closeInventory();
            teleport(player, homeData);
        } else if (right) {
            Bukkit.getScheduler().runTask(plugin, () ->
                    player.openInventory(gui.buildDeleteConfirmGUI(player, homeName)));
        }
    }

    private void handleConfirm(Player player, int slot, String title) {
        String homeName = HomeGUI.getHomeNameFromConfirmTitle(title);
        if (homeName == null) { player.closeInventory(); return; }

        if (slot == 11) {
            boolean deleted = plugin.getHomeManager().deleteHome(player, homeName);
            player.closeInventory();
            if (deleted) MessageUtil.send(player, "home-deleted", "{name}", homeName);
            else MessageUtil.send(player, "home-not-found", "{name}", homeName);
        } else if (slot == 15) {
            Bukkit.getScheduler().runTask(plugin, () ->
                    player.openInventory(gui.buildMainGUI(player, 0)));
        }
    }

    private void teleport(Player player, HomeData homeData) {
        if (!player.hasPermission("homeplugin.bypass.cooldown") &&
                plugin.getCooldownManager().isOnCooldown(player.getUniqueId())) {
            long rem = plugin.getCooldownManager().getRemainingSeconds(player.getUniqueId());
            MessageUtil.send(player, "cooldown-active", "{seconds}", String.valueOf(rem));
            return;
        }
        Location loc = homeData.toLocation();
        if (loc == null) { player.sendMessage(MessageUtil.colorize("&cA világ nem található!")); return; }
        player.teleport(loc);
        plugin.getCooldownManager().setCooldown(player.getUniqueId());
        MessageUtil.send(player, "home-teleported", "{name}", homeData.getName());
    }
}
