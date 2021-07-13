package com.yukiemeralis.blogspot.zenith.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;

import com.yukiemeralis.blogspot.zenith.Zenith;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class PrintUtils 
{
    private static boolean verboseLogging = false;
    private static List<String> log = new ArrayList<>();
    private static String zcolor_hex;

    @SuppressWarnings("serial")
	private static final HashMap<InfoType, String> info_colors = new HashMap<>() {{
        put(InfoType.INFO, "§e");
        put(InfoType.WARN, "§6");
        put(InfoType.ERROR, "§c");
        put(InfoType.FATAL, "§4");
    }};

    public static enum InfoType
    {
        INFO,
        WARN,
        ERROR,
        FATAL 
        ;   
    }

    public static void sendMessage(Entity target, String message)
    {
        BaseComponent[] component = new ComponentBuilder("§8[")
            .append("z").color(ChatColor.of("#" + getZColorHex()))
            .append("§8]§7 " + message.replaceAll("  ", " "))
            .create();

        target.spigot().sendMessage(component);
    }

    public static void sendMessage(CommandSender target, String message)
    {
        if (target instanceof ConsoleCommandSender)
        {
            sendMessage(message);
            return;
        }

        sendMessage((Entity) target, message);
    }

    private static String getZColorHex()
    {
        if (zcolor_hex == null)
            zcolor_hex = Zenith.getZenithConfig().get("zColor");
        if (!zcolor_hex.equals(Zenith.getZenithConfig().get("zColor")))
            zcolor_hex = Zenith.getZenithConfig().get("zColor");
        return zcolor_hex;
    }

    public static void sendMessage(String message)
    {
        Zenith.getInstance().getServer().getConsoleSender().sendMessage("§8[§dz§8]§7 " + StringUtils.normalizeSpace(message));
    }

    public static void sendMessage(String message, InfoType type)
    {
        Zenith.getInstance().getServer().getConsoleSender().sendMessage("§8[§dz§8]§7 " + info_colors.get(type) + StringUtils.normalizeSpace(message));
    }

    public static void enableVerboseLogging()
    {
        verboseLogging = true;
    }

    public static void disableVerboseLogging()
    {
        verboseLogging = false;
    }

    public static boolean isVerboseLoggingEnabled()
    {
        return verboseLogging;
    }

    public static void log(String message, InfoType type)
    {
        Zenith.getInstance().getServer().getConsoleSender().sendMessage("§8[§ez§8]§7 " + info_colors.get(type) + "[" + type.name() + "] " + StringUtils.normalizeSpace(message));

        log.add(message);
    }

    public static void logVerbose(String message, InfoType type)
    {
        if (verboseLogging)
            Zenith.getInstance().getServer().getConsoleSender().sendMessage("§8[§az§8]§7 " + info_colors.get(type)  + "[" + type.name() + "] " + StringUtils.normalizeSpace(message));

        log.add(message);
    }

    public static List<String> getLog()
    {
        return log;
    }

    public static String indicative(int value)
    {
        return value == 1 ? "was" : "were";
    }
}
