package net.devscape.mlr.utils;

import net.devscape.mlr.MineraveLaserTag;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    public static String format(String message) {
        message = message.replace(">>", "").replace("<<", "");
        Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]){6}");
        Matcher matcher = hexPattern.matcher(message);
        while (matcher.find()) {
            ChatColor hexColor = ChatColor.of(matcher.group().substring(1));
            String before = message.substring(0, matcher.start());
            String after = message.substring(matcher.end());
            message = before + hexColor + after;
            matcher = hexPattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String deformat(String str) {
        return ChatColor.stripColor(format(str));
    }

    public static void msgPlayer(Player player, String... str) {
        for (String msg : str) {
            player.sendMessage(format(msg));
        }
    }

    public static void msgPlayer(CommandSender player, String... str) {
        for (String msg : str) {
            player.sendMessage(format(msg));
        }
    }

    public static void titlePlayer(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(format(title), format(subtitle), fadeIn, stay, fadeOut);
    }

    public static void soundPlayer(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public static List<String> color(List<String> lore) {
        return lore.stream().map(Utils::format).collect(Collectors.toList());
    }

    public static ItemStack makeItem(Material material, String displayName, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(format(displayName));

        itemMeta.setLore(Arrays.asList(lore));
        item.setItemMeta(itemMeta);

        return item;
    }


    public static void setLocation(String map, String location, Player player) {
        MineraveLaserTag.getMlt().getConfig().set("games." + map + ".locations." + location + ".world", player.getLocation().getWorld().getName());
        MineraveLaserTag.getMlt().getConfig().set("games." + map + ".locations." + location + ".x", player.getLocation().getX());
        MineraveLaserTag.getMlt().getConfig().set("games." + map + ".locations." + location + ".y", player.getLocation().getY());
        MineraveLaserTag.getMlt().getConfig().set("games." + map + ".locations." + location + ".z", player.getLocation().getZ());
        MineraveLaserTag.getMlt().getConfig().set("games." + map + ".locations." + location + ".yaw", player.getLocation().getYaw());
        MineraveLaserTag.getMlt().getConfig().set("games." + map + ".locations." + location + ".pitch", player.getLocation().getPitch());
        MineraveLaserTag.getMlt().saveConfig();
    }

    public static void setMode(String map, String mode) {
        MineraveLaserTag.getMlt().getConfig().set("games." + map + ".mode", mode);
        MineraveLaserTag.getMlt().saveConfig();
    }

    public static Location getLocation(String map, String location) {
        if (MineraveLaserTag.getMlt().getConfig().contains("games." + map + ".locations." + location + ".world")) {
            String worldName = MineraveLaserTag.getMlt().getConfig().getString("games." + map + ".locations." + location + ".world");
            double x = MineraveLaserTag.getMlt().getConfig().getDouble("games." + map + ".locations." + location + ".x");
            double y = MineraveLaserTag.getMlt().getConfig().getDouble("games." + map + ".locations." + location + ".y");
            double z = MineraveLaserTag.getMlt().getConfig().getDouble("games." + map + ".locations." + location + ".z");
            float yaw = (float) MineraveLaserTag.getMlt().getConfig().getDouble("games." + map + ".locations." + location + ".yaw");
            float pitch = (float) MineraveLaserTag.getMlt().getConfig().getDouble("games." + map + ".locations." + location + ".pitch");

            assert worldName != null;
            return new Location(Bukkit.getServer().getWorld(worldName), x, y, z, yaw, pitch);
        }

        return null;
    }

    public static ItemStack makeItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(format(displayName));

        itemMeta.setLore(color(lore));
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack makeItem(Material material, String displayName, List<String> lore, int custom_model_data) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(format(displayName));

        if (custom_model_data > 0) {
            itemMeta.setCustomModelData(custom_model_data);
        }

        itemMeta.setLore(color(lore));
        item.setItemMeta(itemMeta);

        return item;
    }

    public static void sendActionBar(Player player, String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);

        TextComponent textComponent = new TextComponent(format(message));

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);
    }

    public static void giveWaitingItems(Player player) {
        player.getInventory().clear();

        for (String str : Objects.requireNonNull(MineraveLaserTag.getMlt().getConfig().getConfigurationSection("items")).getKeys(false)) {
            String displayname = MineraveLaserTag.getMlt().getConfig().getString("items." + str + ".displayname");
            int slot = MineraveLaserTag.getMlt().getConfig().getInt("items." + str + ".slot");
            int custom_model_data = MineraveLaserTag.getMlt().getConfig().getInt("items." + str + ".custom-model-data");
            List<String> lore = MineraveLaserTag.getMlt().getConfig().getStringList("items." + str + ".lore");
            String material = MineraveLaserTag.getMlt().getConfig().getString("items." + str + ".material");

            assert material != null;
            player.getInventory().setItem(slot, makeItem(Material.valueOf(material.toUpperCase()), displayname, color(lore), custom_model_data));
        }
    }

    public static void giveGun(Player player) {
        player.getInventory().clear();
        player.getInventory().addItem(makeItem(Material.STICK, "&a&lLaser Gun"));
    }
}