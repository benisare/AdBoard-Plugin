package com.example.adboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Paginated GUI listing every posted ad as a player head.
 */
public class AdGUI implements Listener {

    private static final int PAGE_SIZE = 45;
    private static final String TITLE_PREFIX = ChatColor.DARK_GREEN + "Service Ads";

    private static final int SLOT_PREV = 48;
    private static final int SLOT_CLOSE = 49;
    private static final int SLOT_NEXT = 50;

    private final AdBoardPlugin plugin;
    private final Map<Inventory, Integer> openPages = new HashMap<>();

    public AdGUI(AdBoardPlugin plugin) {
        this.plugin = plugin;
    }

    public void openPage(Player player, int page) {
        List<Ad> ads = plugin.getAdManager().getAllAds();
        int maxPage = ads.isEmpty() ? 0 : (ads.size() - 1) / PAGE_SIZE;
        if (page < 0) page = 0;
        if (page > maxPage) page = maxPage;

        Inventory inv = Bukkit.createInventory(null, 54,
                TITLE_PREFIX + " (" + (page + 1) + "/" + (maxPage + 1) + ")");

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, ads.size());
        for (int i = start; i < end; i++) {
            inv.setItem(i - start, buildAdItem(ads.get(i)));
        }

        if (page > 0) {
            inv.setItem(SLOT_PREV, buildNavItem(Material.ARROW, ChatColor.YELLOW + "Previous Page"));
        }
        inv.setItem(SLOT_CLOSE, buildNavItem(Material.BARRIER, ChatColor.RED + "Close"));
        if (page < maxPage) {
            inv.setItem(SLOT_NEXT, buildNavItem(Material.ARROW, ChatColor.YELLOW + "Next Page"));
        }

        openPages.put(inv, page);
        player.openInventory(inv);
    }

    private ItemStack buildAdItem(Ad ad) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(ad.getOwner()));
            meta.setDisplayName(ChatColor.GOLD + ad.getOwnerName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "\"" + ChatColor.WHITE + ad.getMessage() + ChatColor.GRAY + "\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "Posted " + timeAgo(ad.getTimestamp()));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildNavItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String timeAgo(long timestamp) {
        long diffMs = System.currentTimeMillis() - timestamp;
        long minutes = diffMs / 60000;
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        return days + "d ago";
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!openPages.containsKey(top)) {
            return;
        }
        // Block all interaction (taking items, shift-clicking from player inventory, etc.)
        event.setCancelled(true);

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(top)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        int page = openPages.get(top);

        if (slot == SLOT_CLOSE) {
            player.closeInventory();
        } else if (slot == SLOT_PREV) {
            openPage(player, page - 1);
        } else if (slot == SLOT_NEXT) {
            openPage(player, page + 1);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        openPages.remove(event.getInventory());
    }
}
