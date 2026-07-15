package com.example.adboard;

import org.bukkit.plugin.java.JavaPlugin;

public class AdBoardPlugin extends JavaPlugin {

    private AdManager adManager;
    private AdGUI adGUI;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.adManager = new AdManager(this);
        this.adGUI = new AdGUI(this);

        getServer().getPluginManager().registerEvents(adGUI, this);

        AdCommand adCommand = new AdCommand(this);
        getCommand("ad").setExecutor(adCommand);
        getCommand("ad").setTabCompleter(adCommand);

        int expiryHours = getConfig().getInt("ad-expiry-hours", 0);
        if (expiryHours > 0) {
            long ticks = 20L * 60L * 60L; // check once per hour
            long maxAgeMillis = expiryHours * 3600000L;
            getServer().getScheduler().runTaskTimer(this, () -> {
                int removed = adManager.purgeExpired(maxAgeMillis);
                if (removed > 0) {
                    getLogger().info("Purged " + removed + " expired ad(s).");
                }
            }, ticks, ticks);
        }

        getLogger().info("AdBoard has been enabled!");
    }

    @Override
    public void onDisable() {
        if (adManager != null) {
            adManager.save();
        }
        getLogger().info("AdBoard has been disabled!");
    }

    public AdManager getAdManager() {
        return adManager;
    }

    public AdGUI getAdGUI() {
        return adGUI;
    }
}
