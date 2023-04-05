package me.jesper.sch.jeconomy.utils;

import me.jesper.sch.jeconomy.JEconomy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageManager {
    private static String prefix = ChatColor.AQUA + "JEconomy: " + ChatColor.GREEN;

    public static String getPrefix(){
        return prefix;
    }

    public static void consoleGood(String message){
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GREEN + message);
    }
    public static void consoleBad(String message){
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + message);
    }
    public static void consoleInfo(String message){
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + message);
    }
    public static void playerGood(Player player, String message){
        player.sendMessage(ChatColor.GREEN + message);
    }
    public static void playerBad(Player player, String message){
        player.sendMessage(ChatColor.RED + message);
    }
    public static void playerInfo(Player player, String message){
        player.sendMessage(ChatColor.YELLOW + message);
    }

    public static void playerNeutral(Player player, String message){
        player.sendMessage(ChatColor.AQUA + message);
    }

}

