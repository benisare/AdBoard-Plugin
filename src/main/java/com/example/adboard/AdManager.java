package com.example.adboard;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Loads, stores, and persists ads to ads.yml.
 */
public class AdManager {

    private final AdBoardPlugin plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<UUID, Ad> ads = new LinkedHashMap<>();

    public AdManager(AdBoardPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ads.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create ads.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        ads.clear();
        if (config.isConfigurationSection("ads")) {
            for (String key : config.getConfigurationSection("ads").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String name = config.getString("ads." + key + ".name", "Unknown");
                    String message = config.getString("ads." + key + ".message", "");
                    long timestamp = config.getLong("ads." + key + ".timestamp", System.currentTimeMillis());
                    ads.put(uuid, new Ad(uuid, name, message, timestamp));
                } catch (IllegalArgumentException ignored) {
                    // skip malformed keys
                }
            }
        }
    }

    public void save() {
        config.set("ads", null);
        for (Ad ad : ads.values()) {
            String path = "ads." + ad.getOwner();
            config.set(path + ".name", ad.getOwnerName());
            config.set(path + ".message", ad.getMessage());
            config.set(path + ".timestamp", ad.getTimestamp());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save ads.yml: " + e.getMessage());
        }
    }

    public void setAd(UUID uuid, String name, String message) {
        ads.put(uuid, new Ad(uuid, name, message, System.currentTimeMillis()));
        save();
    }

    public boolean removeAd(UUID uuid) {
        boolean removed = ads.remove(uuid) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public Ad getAd(UUID uuid) {
        return ads.get(uuid);
    }

    public List<Ad> getAllAds() {
        List<Ad> list = new ArrayList<>(ads.values());
        list.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return list;
    }

    /**
     * Removes any ads older than maxAgeMillis. Returns how many were removed.
     */
    public int purgeExpired(long maxAgeMillis) {
        if (maxAgeMillis <= 0) {
            return 0;
        }
        long now = System.currentTimeMillis();
        int count = 0;
        Iterator<Map.Entry<UUID, Ad>> it = ads.entrySet().iterator();
        while (it.hasNext()) {
            Ad ad = it.next().getValue();
            if (now - ad.getTimestamp() > maxAgeMillis) {
                it.remove();
                count++;
            }
        }
        if (count > 0) {
            save();
        }
        return count;
    }
}
