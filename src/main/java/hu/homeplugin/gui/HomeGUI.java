package hu.homeplugin.gui;

import hu.homeplugin.HomePlugin;
import hu.homeplugin.managers.HomeData;
import hu.homeplugin.utils.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class HomeGUI {

    public static final int[] HOME_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private static final int[] BORDER_SLOTS = {
            0,1,2,3,4,5,6,7,8,
            9,17, 18,26, 27,35, 36,44,
            45,46,47,48,49,50,51,52,53
    };

    private static final int PREV_SLOT = 48;
    private static final int CLOSE_SLOT = 49;
    private static final int NEXT_SLOT = 50;
    private static final int INFO_SLOT = 4;

    private final HomePlugin plugin;

    public HomeGUI(HomePlugin plugin) {
        this.plugin = plugin;
    }

    public Inventory buildMainGUI(Player player, int page) {
        String title = MessageUtil.colorizeString(
                plugin.getConfig().getString("gui.title", "&8&l✦ &6&lHomes &8&l✦"));

        Inventory inv = Bukkit.createInventory(null, 54,
                Component.text(title + "§0" + page));

        Map<String, HomeData> homes = plugin.getHomeManager().getHomes(player);
        List<HomeData> list = new ArrayList<>(homes.values());
        list.sort(Comparator.comparing(HomeData::getName));

        int perPage = HOME_SLOTS.length;
        int totalPages = Math.max(1, (int) Math.ceil(list.size() / (double) perPage));
        page = Math.max(0, Math.min(page, totalPages - 1));

        fillBorder(inv);

        int start = page * perPage;
        for (int i = start; i < Math.min(start + perPage, list.size()); i++) {
            inv.setItem(HOME_SLOTS[i - start], buildHomeItem(list.get(i)));
        }

        if (page > 0)
            inv.setItem(PREV_SLOT, buildSimpleItem(Material.ARROW,
                    "&e&l← Előző oldal", "&7Oldal: &e" + page + "/" + totalPages));

        inv.setItem(CLOSE_SLOT, buildSimpleItem(Material.BARRIER, "&c&lBezárás", "&7GUI bezárása"));

        if (page < totalPages - 1)
            inv.setItem(NEXT_SLOT, buildSimpleItem(Material.ARROW,
                    "&a&lKövetkező →", "&7Oldal: &e" + (page + 2) + "/" + totalPages));

        int limit = plugin.getHomeManager().getHomeLimit(player);
        String limitStr = limit == Integer.MAX_VALUE ? "∞" : String.valueOf(limit);
        inv.setItem(INFO_SLOT, buildSimpleItem(Material.COMPASS,
                "&6&lHome-ok &8(" + homes.size() + "/" + limitStr + ")",
                "&7Bal klikk: &aTeleportálás\n&7Jobb klikk: &cTörlés\n&8Oldal: &e" + (page + 1) + "/" + totalPages));

        return inv;
    }

    public Inventory buildDeleteConfirmGUI(Player player, String homeName) {
        String title = MessageUtil.colorizeString("&4&l✖ Törlés megerősítése");
        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text(title + "§1"));
 
        ItemStack red = buildSimpleItem(Material.RED_STAINED_GLASS_PANE, " ", "");
        for (int i = 0; i < 27; i++) inv.setItem(i, red);

        ItemStack confirm = new ItemStack(Material.LIME_WOOL);
        ItemMeta cm = confirm.getItemMeta();
        cm.displayName(MessageUtil.colorize("&a&l✔ Törlés megerősítése"));
        cm.lore(List.of(
                MessageUtil.colorize("&7Home: &e" + homeName),
                MessageUtil.colorize(""),
                MessageUtil.colorize("&cEz nem visszafordítható!")
        ));
        confirm.setItemMeta(cm);
        inv.setItem(11, confirm);

        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta xm = cancel.getItemMeta();
        xm.displayName(MessageUtil.colorize("&c&l✖ Mégsem"));
        xm.lore(List.of(MessageUtil.colorize("&7Vissza a listához")));
        cancel.setItemMeta(xm);
        inv.setItem(15, cancel);

        inv.setItem(13, buildSimpleItem(Material.BOOK,
                "&6Törlés: &e" + homeName, "&7Biztosan törlöd?"));

        return inv;
    }

    private ItemStack buildHomeItem(HomeData home) {
        ItemStack item = new ItemStack(getWorldMaterial(home.getWorldName()));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.colorize("&6&l" + home.getName()));
        meta.lore(List.of(
                MessageUtil.colorize("&8" + home.getWorldName()),
                MessageUtil.colorize(""),
                MessageUtil.colorize("&a▶ Bal klikk: &7Teleportálás"),
                MessageUtil.colorize("&c✖ Jobb klikk: &7Törlés")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildSimpleItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtil.colorize(name));
        if (!lore.isEmpty()) {
            List<Component> loreList = new ArrayList<>();
            for (String line : lore.split("\n"))
                loreList.add(MessageUtil.colorize(line));
            meta.lore(loreList);
        }
        item.setItemMeta(meta);
        return item;
    }

    private void fillBorder(Inventory inv) {
        String colorName = plugin.getConfig().getString("gui.filled-glass-color", "BLACK");
        Material mat = switch (colorName.toUpperCase()) {
            case "WHITE" -> Material.WHITE_STAINED_GLASS_PANE;
            case "GRAY" -> Material.GRAY_STAINED_GLASS_PANE;
            case "RED" -> Material.RED_STAINED_GLASS_PANE;
            case "BLUE" -> Material.BLUE_STAINED_GLASS_PANE;
            case "GREEN" -> Material.GREEN_STAINED_GLASS_PANE;
            case "YELLOW" -> Material.YELLOW_STAINED_GLASS_PANE;
            case "PURPLE" -> Material.PURPLE_STAINED_GLASS_PANE;
            case "CYAN" -> Material.CYAN_STAINED_GLASS_PANE;
            default -> Material.BLACK_STAINED_GLASS_PANE;
        };
        ItemStack glass = buildSimpleItem(mat, " ", "");
        for (int slot : BORDER_SLOTS) inv.setItem(slot, glass);
    }

    private Material getWorldMaterial(String worldName) {
        if (worldName == null) return Material.GRASS_BLOCK;
        String l = worldName.toLowerCase();
        if (l.contains("nether")) return Material.NETHERRACK;
        if (l.contains("end")) return Material.END_STONE;
        return Material.GRASS_BLOCK;
    }

    public static int getPageFromTitle(String title) {
        try {
            int idx = title.lastIndexOf("§0");
            if (idx == -1) return 0;
            return Integer.parseInt(title.substring(idx + 2).trim());
        } catch (NumberFormatException e) { return 0; }
    }

    public static String getHomeNameFromConfirmTitle(String title) {
        int idx = title.lastIndexOf("§1");
        if (idx == -1) return null;
        return title.substring(idx + 2).trim();
    }

    public static boolean isMainGUI(String title) { return title.contains("§0"); }
    public static boolean isConfirmGUI(String title) { return title.contains("§1"); }
}
