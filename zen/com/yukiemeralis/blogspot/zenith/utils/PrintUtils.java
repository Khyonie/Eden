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

/**
 * A collection of utilities for printing text to players, entities, and the console.
 * @author Yuki_emeralis
 *
 */
public class PrintUtils 
{
    private static boolean verboseLogging = false;
    private static List<String> log = new ArrayList<>();
    private static String zcolor_hex;

	private static final HashMap<InfoType, String> info_colors = new HashMap<>() {{
        put(InfoType.INFO, "§e");
        put(InfoType.WARN, "§6");
        put(InfoType.ERROR, "§c");
        put(InfoType.FATAL, "§4");
    }};

    /**
     * Category of information, used for logging.
     * @author Yuki_emeralis
     *
     */
    @SuppressWarnings("javadoc")
    public static enum InfoType
    {
        INFO,
        WARN,
        ERROR,
        FATAL 
        ;   
    }

    /**
     * Sends a message to an entity.
     * @param target
     * @param message
     */
    public static void sendMessage(Entity target, String message)
    {
        BaseComponent[] component = new ComponentBuilder("§8[")
            .append("z").color(ChatColor.of("#" + getZColorHex()))
            .append("§8]§7 " + message.replaceAll("  ", " "))
            .create();

        target.spigot().sendMessage(component);
    }

    /**
     * Sends a message to a command sender, which can be an entity, console, or command block.
     * @param target
     * @param message
     */
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

    /**
     * Sends a message to the console.
     * @param message
     */
    public static void sendMessage(String message)
    {
        Zenith.getInstance().getServer().getConsoleSender().sendMessage("§8[§dz§8]§7 " + StringUtils.normalizeSpace(message));
    }

    /**
     * Sends a message to the console with a particular information type.
     * @param message
     * @param type
     */
    public static void sendMessage(String message, InfoType type)
    {
        Zenith.getInstance().getServer().getConsoleSender().sendMessage("§8[§dz§8]§7 " + info_colors.get(type) + StringUtils.normalizeSpace(message));
    }

    /**
     * Enables verbose logging.
     */
    public static void enableVerboseLogging()
    {
        verboseLogging = true;
    }

    /**
     * Disables verbose logging.
     */
    public static void disableVerboseLogging()
    {
        verboseLogging = false;
    }

    /**
     * @return Whether or not verbose logging is enabled or disabled.
     */
    public static boolean isVerboseLoggingEnabled()
    {
        return verboseLogging;
    }

    /**
     * Logs a message into the console and persistent logs. Logs can be exported.
     * @param message
     * @param type
     */
    public static void log(String message, InfoType type)
    {
        Zenith.getInstance().getServer().getConsoleSender().sendMessage("§8[§ez§8]§7 " + info_colors.get(type) + "[" + type.name() + "] " + StringUtils.normalizeSpace(message));

        log.add(message);
    }

    /**
     * Logs a message into the console and persistent logs, with an info level of INFO. Logs can be exported.
     * @param message
     */
    public static void log(String message)
    {
        log(message, InfoType.INFO);
    }

    /**
     * Logs a message into the console if verbose logging is set, and persistent logs. Logs can be exported.
     * @param message
     * @param type
     */
    public static void logVerbose(String message, InfoType type)
    {
        if (verboseLogging)
            Zenith.getInstance().getServer().getConsoleSender().sendMessage("§8[§az§8]§7 " + info_colors.get(type)  + "[" + type.name() + "] " + StringUtils.normalizeSpace(message));

        log.add(message);
    }

    /**
     * Obtains the current log.
     * @return The current log.
     */
    public static List<String> getLog()
    {
        return log;
    }

    /**
     * Generates an indicitave ("was" vs. "were") depending on a value.
     * @param value
     * @return Grammatically accurate indicative.
     */
    public static String indicative(int value)
    {
        return value == 1 ? "was" : "were";
    }
}
