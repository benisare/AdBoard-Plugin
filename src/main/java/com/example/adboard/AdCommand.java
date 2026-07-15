package com.example.adboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AdCommand implements CommandExecutor, TabCompleter {

    private final AdBoardPlugin plugin;

    public AdCommand(AdBoardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a subcommand. Try /ad list");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("adboard.browse")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to browse ads.");
                return true;
            }
            plugin.getAdGUI().openPage(player, 0);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "post":
                return handlePost(sender, args);
            case "remove":
                return handleRemove(sender);
            case "browse":
                return handleBrowse(sender);
            case "list":
                return handleList(sender);
            case "reload":
                return handleReload(sender);
            case "clear":
                return handleClear(sender, args);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handlePost(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can post ads.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("adboard.post")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to post ads.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /ad post <your ad message>");
            return true;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        int maxLen = plugin.getConfig().getInt("max-message-length", 100);
        if (message.length() > maxLen) {
            player.sendMessage(ChatColor.RED + "Your ad is too long (max " + maxLen + " characters).");
            return true;
        }
        plugin.getAdManager().setAd(player.getUniqueId(), player.getName(), message);
        player.sendMessage(ChatColor.GREEN + "Your ad has been posted! Others can see it with /ad browse.");
        return true;
    }

    private boolean handleRemove(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can remove their ad.");
            return true;
        }
        Player player = (Player) sender;
        if (plugin.getAdManager().removeAd(player.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "Your ad has been removed.");
        } else {
            player.sendMessage(ChatColor.YELLOW + "You don't have an active ad.");
        }
        return true;
    }

    private boolean handleBrowse(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can browse ads in the GUI. Try /ad list instead.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("adboard.browse")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to browse ads.");
            return true;
        }
        plugin.getAdGUI().openPage(player, 0);
        return true;
    }

    private boolean handleList(CommandSender sender) {
        List<Ad> ads = plugin.getAdManager().getAllAds();
        if (ads.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "There are no ads posted right now.");
            return true;
        }
        sender.sendMessage(ChatColor.GOLD + "=== Service Ads (" + ads.size() + ") ===");
        for (Ad ad : ads) {
            sender.sendMessage(ChatColor.AQUA + ad.getOwnerName() + ChatColor.GRAY + ": " + ChatColor.WHITE + ad.getMessage());
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("adboard.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
        }
        plugin.reloadConfig();
        plugin.getAdManager().load();
        sender.sendMessage(ChatColor.GREEN + "AdBoard config and ads reloaded.");
        return true;
    }

    private boolean handleClear(CommandSender sender, String[] args) {
        if (!sender.hasPermission("adboard.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ad clear <player>");
            return true;
        }
        @SuppressWarnings("deprecation")
        UUID target = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
        if (plugin.getAdManager().removeAd(target)) {
            sender.sendMessage(ChatColor.GREEN + "Removed ad for " + args[1] + ".");
        } else {
            sender.sendMessage(ChatColor.YELLOW + args[1] + " has no active ad.");
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== AdBoard Help ===");
        sender.sendMessage(ChatColor.AQUA + "/ad" + ChatColor.GRAY + " - Open the ad browser GUI");
        sender.sendMessage(ChatColor.AQUA + "/ad post <message>" + ChatColor.GRAY + " - Post or update your service ad");
        sender.sendMessage(ChatColor.AQUA + "/ad remove" + ChatColor.GRAY + " - Remove your ad");
        sender.sendMessage(ChatColor.AQUA + "/ad browse" + ChatColor.GRAY + " - Browse ads in a GUI");
        sender.sendMessage(ChatColor.AQUA + "/ad list" + ChatColor.GRAY + " - List all ads as plain text");
        if (sender.hasPermission("adboard.admin")) {
            sender.sendMessage(ChatColor.AQUA + "/ad reload" + ChatColor.GRAY + " - Reload config");
            sender.sendMessage(ChatColor.AQUA + "/ad clear <player>" + ChatColor.GRAY + " - Remove a player's ad");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("post", "remove", "browse", "list", "help"));
            if (sender.hasPermission("adboard.admin")) {
                subs.addAll(Arrays.asList("reload", "clear"));
            }
            String input = args[0].toLowerCase();
            return subs.stream().filter(s -> s.startsWith(input)).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("clear")) {
            String input = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
