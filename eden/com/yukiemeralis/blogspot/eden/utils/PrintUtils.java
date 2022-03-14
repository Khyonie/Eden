package com.yukiemeralis.blogspot.eden.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;

import com.yukiemeralis.blogspot.eden.Eden;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

/**
 * A collection of utilities for printing text to players, entities, and the console.
 * @author Yuki_emeralis
 */
public class PrintUtils 
{
    private static boolean verboseLogging = false;
    private static List<String> log = new ArrayList<>();
    private static String ecolor_hex;

	private static final HashMap<InfoType, String> info_colors = new HashMap<>() {{
        put(InfoType.INFO, "§e");
        put(InfoType.WARN, "§6");
        put(InfoType.ERROR, "§c");
        put(InfoType.FATAL, "§4");
    }};

    private static final HashMap<String, String> log_colors = new HashMap<>()
    {{
        put("(?<=([^\\\\]|\\A))\\[", "§a");
        put("(?<=([^\\\\]|\\A))\\{", "§b");
        put("(?<=([^\\\\]|\\A))\\(", "§e");
        put("(?<=([^\\\\]|\\A))<", "§c");
        put("(?<=[^-\\\\])(\\]|\\}|\\)|>)", "§8");
    }};

    private static final HashMap<String, String> symbols = new HashMap<>()
    {{
        put("spade", "♠");
        put("club", "♣");
        put("heartsuit", "♥");
        put("diamond", "♦");
        put("star", "★");
        put("wstar", "☆");
        put("heart", "❤");
        put("euro", "£");
        put("cent", "¢");
        put("yen", "¥");
        put("divided", "÷");
    }};

    /**
     * Category of information, used for logging.
     * @author Yuki_emeralis
     */
    public static enum InfoType
    {
        INFO,
        WARN,
        ERROR,
        FATAL 
        ;   
    }

    public static void printPrettyStacktrace(Throwable error)
    {
        String header = "§c" + error.getClass().getName();

        if (error.getMessage() != null)
        {
            header = header + " " + error.getMessage();
            header = saveSpecialCharacters(header);
        }
        
        PrintUtils.log(header, InfoType.ERROR);

        for (StackTraceElement ste : error.getStackTrace())
        {
            PrintUtils.log("§c §c §c §c §c §c §c at " + ste.getClassName() + "." + ste.getMethodName() + "\\(" + ste.getFileName() + ":" + ste.getLineNumber() + "\\)", InfoType.ERROR);
        }

        if (error.getCause() != null)
            printPrettyStacktrace(error.getCause(), true);
    }

    public static void printPrettyStacktrace(Throwable error, boolean isCausedBy)
    {
        String header = "§c";
        if (isCausedBy)
            header = header +  "Caused by: ";
        header = header + error.getClass().getName();
        if (error.getMessage() != null)
        {
            header = header + " " + error.getMessage();
            header = saveSpecialCharacters(header);
        }
        
        PrintUtils.log(header, InfoType.ERROR);

        for (StackTraceElement ste : error.getStackTrace())
        {
            PrintUtils.log("§c §c §c §c §c §c §c at " + ste.getClassName() + "." + ste.getMethodName() + "\\(" + ste.getFileName() + ":" + ste.getLineNumber() + "\\)", InfoType.ERROR);
        }

        if (error.getCause() != null)
            printPrettyStacktrace(error.getCause(), true);
    }
    
    private static String saveSpecialCharacters(String input)
    {
        return input
            .replace("(", "\\(")
            .replace("[", "\\[")
            .replace("{", "\\{")
            .replace("<", "\\<")
            .replace(")", "\\)")
            .replace("]", "\\]")
            .replace("}", "\\}")
            .replace(">", "\\>");
    }

    /**
     * Sends a message to an entity.
     * @param target
     * @param message
     */
    public static void sendMessage(Entity target, String message)
    {
        sendComponent(target, buildComponents("&8[<#" + Eden.getInstance().getConfig().get("eColor") + ">e&8] &7" + message, true, true));
    }

    public static void sendComponent(Entity target, BaseComponent[] components)
    {
        target.spigot().sendMessage(components);
    }

    public static BaseComponent[] buildComponents(String input, boolean translateAnd, boolean translateSymbols)
    {
        String data = input;
        if (translateAnd)
            data = data.replace('&', '§');
        if (translateSymbols)
            data = ChatUtils.formatWithRegexGroups(data, ":[^\s:]{1,9}:", ":", symbols);

        String[] noColorSplit = data.split("<#[0-9a-fA-F]{0,6}>");
        String[] colorSplit = Pattern.compile("<#[0-9a-fA-F]{0,6}>").matcher(data).results().map(MatchResult::group).toArray(String[]::new);

        // Sanitize colors
        for (int i = 0; i < colorSplit.length; i++)
        {
            String color = colorSplit[i].replaceAll("[<>]", "");
            if (color.length() != 7) // Invalid color
            {
                color = DataUtils.fixColor(color);
            }
                
            colorSplit[i] = color;
        }

        ComponentBuilder component = new ComponentBuilder();

        if (colorSplit.length == 0)
        {
            component = component.append(input);
            return component.create();
        }

        if (noColorSplit.length == 0) // No text
        {
            return component.append("").create();
        }

        if (noColorSplit[0].equals("")) // Starts with color
        {
            component = component.append(noColorSplit[1]).color(ChatColor.of(colorSplit[0]));

            for (int i = 1; i < colorSplit.length; i++)
                component = component.append(noColorSplit[i + 1]).color(ChatColor.of(colorSplit[i]));

            return component.create();
        }

        if (noColorSplit.length > colorSplit.length) // Starts and ends with text
        {
            component = component.append(noColorSplit[0]).color(ChatColor.WHITE);
            for (int i = 1; i < noColorSplit.length; i++)
                component = component.append(noColorSplit[i]).color(ChatColor.of(colorSplit[i - 1]));

            return component.create();
        }

        for (int i = 0; i < noColorSplit.length; i++)
            component = component.append(noColorSplit[i]).color(ChatColor.of(colorSplit[i]));

        return component.create();
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

    @SuppressWarnings("unused")
    private static String getEColorHex()
    {
        if (ecolor_hex == null)
            ecolor_hex = Eden.getEdenConfig().get("eColor");
        if (!ecolor_hex.equals(Eden.getEdenConfig().get("eColor")))
            ecolor_hex = Eden.getEdenConfig().get("eColor");
        return ecolor_hex;
    }

    /**
     * Sends a message to the console.
     * @param message
     */
    public static void sendMessage(String message)
    {
        Eden.getInstance().getServer().getConsoleSender().sendMessage("§8[§de§8]§7 " + StringUtils.normalizeSpace(message));
    }

    /**
     * Sends a message to the console with a particular information type.
     * @param message
     * @param type
     */
    public static void sendMessage(String message, InfoType type)
    {
        Eden.getInstance().getServer().getConsoleSender().sendMessage("§8[§de§8]§7 " + info_colors.get(type) + StringUtils.normalizeSpace(message));
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
        // Give some nice formatting to messages
        String formattedMessage = formatLog(message).replaceAll("\\\\(?=[\\[\\]\\(\\)\\{\\}<>])", "");
        Eden.getInstance().getServer().getConsoleSender().sendMessage("§8[" + info_colors.get(type) + "e§8]§8 " + StringUtils.normalizeSpace(formattedMessage));

        log.add("[" + type.name() + "] " + formattedMessage.replaceAll("§[a-fA-F0-9klmnor]{1}", ""));
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
     * Outputs a log message on a condition. Logging level will always be "INFO".
     * @param condition The condition to evaluate.
     * @param message The message to print if and only if "condition" is true.
     */
    public static void logCondition(boolean condition, String message)
    {
        if (condition)
            log(message);
    }

    /**
     * Outputs a log message on a condition, with a unique message for both outcomes. Logging level will always be "INFO".
     * @param condition The condition to evaluate.
     * @param trueMessage The message to print if and only if "condition" is true.
     * @param falseMessage The message to print if and only if "condition" is false.
     */
    public static void logCondition(boolean condition, String trueMessage, String falseMessage)
    {
        if (condition)
        {
            log(trueMessage);
            return;
        }

        log(falseMessage);
    }

    /**
     * Outputs a log message on a condition.
     * @param condition The condition to evaluate.
     * @param message The message to print if and only if "condition" is true.
     * @param type The logging level to use.
     */
    public static void logCondition(boolean condition, String message, InfoType type)
    {
        if (condition)
            log(message, type);
    }

    /**
     * Outputs a log message on a condition, with a unique message for both outcomes.
     * @param condition The condition to evaluate.
     * @param trueMessage The message to print if and only if "condition" is true.
     * @param trueType The logging level to use for a true output.
     * @param falseMessage The message to print if and only if "condition" is false.
     * @param falseType The logging level to use for a false output.
     */
    public static void logCondition(boolean condition, String trueMessage, InfoType trueType, String falseMessage, InfoType falseType)
    {
        if (condition) 
        {
            log(trueMessage, trueType);
            return;
        }

        log(falseMessage, falseType);
    }

    /**
     * Logs a message into the console if verbose logging is set, and persistent logs. Logs can be exported.
     * @param message
     * @param type
     */
    public static void logVerbose(String message, InfoType type)
    {
        if (verboseLogging)
            Eden.getInstance().getServer().getConsoleSender().sendMessage("§8[§ae§8]§7 " + info_colors.get(type)  + StringUtils.normalizeSpace(message));

        log.add("[" + type.name() + "] " + message);
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

    public static String plural(int count, String singular, String plural)
    {
        return count == 1 ? singular : plural;
    }

    public static String plural(long count, String singular, String plural)
    {
        return count == 1 ? singular : plural;
    }

    /**
     * Formats a logging message.
     * @param input A given message.
     * @return A log message with Eden's formatting.
     */
    private static String formatLog(String input)
    {
        String buffer = input;
        for (String regex : log_colors.keySet())
            buffer = buffer.replaceAll(regex, log_colors.get(regex));
        return buffer;
    }
}
