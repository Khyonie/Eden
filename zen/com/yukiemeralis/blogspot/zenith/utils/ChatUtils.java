package com.yukiemeralis.blogspot.zenith.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * This class is a little bit interesting: it lets a developer pull a message from a player's chat, and prevents the message from reaching server chat and console logs.
 * 
 * 1) Write an action by implementing ChatAction in a class and overriding run()
 * - Your action should place the result in a seperate string by calling "receiveResult()", then clear the result (for security) by calling "deleteResult()"
 * 2) Prime the listener and thread by calling expectChat(), passing in the target player and action as args
 * 
 * The "args" parameter list is optional, and is intended for arguments passed into your action.
 * 
 * Please always notify a player when they've entered and exitted "text entry" mode, otherwise when the player attempts to chat, their message will be redirected,
 * and they won't know why.
 */
public class ChatUtils implements Listener
{
    private static Map<CommandSender, Thread> active_threads = new HashMap<>();
    private static Map<CommandSender, String> results = new HashMap<>();

    private static List<CommandSender> deactivated_threads = new ArrayList<>();

    /**
     * Primes a thread to expect a chat input.
     * @param target The user to expect input from.
     * @param action An action to perform when input is received.
     * @param timeout An action to perform if the thread times out.
     * @param timeoutTime The time, in ticks (20 ticks = 1 second), to timeout
     * @param args Optional arguments passed into a chataction.
     */
    public static void expectChat(CommandSender target, ChatAction action, TimeoutAction timeout, long timeoutTime, Object... args)
    {
        new BukkitRunnable() {
            @Override
            public void run() 
            {
                timeout.run();
            }
        }.runTaskLater(Zenith.getInstance(), timeoutTime);
        
        expectChat(target, action, args);
    }

    /**
     * Primes a thread to expect a chat input.
     * @param target The user to expect input from.
     * @param action An action to perform when input is received.
     * @param args Optional arguments passed into a chataction.
     */
    public static void expectChat(CommandSender target, ChatAction action, Object... args)
    {
        if (active_threads.containsKey(target))
        {
            deactivated_threads.add(target);

            synchronized(active_threads.get(target)) {
                active_threads.get(target).notify();
            }
                
            active_threads.remove(target);
            deactivated_threads.remove(target);
        }

        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try {
                    synchronized(this) {
                        this.wait();
                    }
                } catch (Exception e) {
                    PrintUtils.printPrettyStacktrace(e);
                    active_threads.remove(target);
                }

                if (deactivated_threads.contains(target)) {
                    active_threads.remove(target);
                    return;
                }
                    
                active_threads.remove(target);
                action.run();
                results.remove(target);
            }
        };

        active_threads.put(target, t);
        t.start();
    }

    /**
     * Receives a string from a player's chat.
     * @param target The player to receive from.
     * @return A string that the player sent as a chat message.
     */
    public static String receiveResult(CommandSender target)
    {
        return results.get(target);
    }

    /**
     * Clears a result.
     * @param target The player to clear the result from.
     */
    public static void deleteResult(CommandSender target)
    {
        results.put(target, "0123456789");
        results.remove(target);
    }

    /**
     * Force-notifies an active chat thread to continue.
     * @param target
     * @param stop
     */
    public static void forceNotify(CommandSender target, boolean stop)
    {
        if (!active_threads.containsKey(target))
            return;

        if (stop && !deactivated_threads.contains(target))
            deactivated_threads.add(target);

        active_threads.get(target).notify();
    }

    /**
     * Converts a hex string to a minecraft format.
     * @param hex
     * @return The given hex color string in minecraft format.
     */
    public static String of(String hex)
    {
        if (hex.length() != 6)
            throw new IllegalArgumentException("Hex color codes must have a length of exactly 6. Given: " + hex.length());

        StringBuilder builder = new StringBuilder("§x");
        for (String str : hex.split(""))
            builder.append("§" + str);
        return builder.toString();
    }

    public static String of(String input, String from, String to, String formatting)
    {
        if (from.length() != 6)
            throw new IllegalArgumentException("Hex color codes must have a length of exactly 6. Given: " + from.length());
        if (to.length() != 6)
            throw new IllegalArgumentException("Hex color codes must have a length of exactly 6. Given: " + to.length());

        String 
            redTo = to.substring(0, 2),
            redFrom = from.substring(0, 2),
            greenTo = to.substring(2, 4),
            greenFrom = from.substring(2, 4),
            blueTo = to.substring(4),
            blueFrom = from.substring(4);

        int
            redDelta = Integer.parseInt(redTo, 16) - Integer.parseInt(redFrom, 16),
            greenDelta = Integer.parseInt(greenTo, 16) - Integer.parseInt(greenFrom, 16),
            blueDelta = Integer.parseInt(blueTo, 16) - Integer.parseInt(blueFrom, 16);

        String red, green, blue;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++)
        {
            red = Integer.toHexString(Integer.parseInt(redFrom, 16) + Math.round((redDelta/input.length()) * i)).toUpperCase();
			blue = Integer.toHexString(Integer.parseInt(blueFrom, 16) + Math.round((blueDelta/input.length()) * i)).toUpperCase();
			green = Integer.toHexString(Integer.parseInt(greenFrom, 16) + Math.round((greenDelta/input.length()) * i)).toUpperCase();

            if (red.length() == 1)
				red = "0" + red;
			if (blue.length() == 1)
				blue = "0" + blue;
			if (green.length() == 1)
				green = "0" + green;

            builder.append(ChatUtils.of(red + green + blue) + formatting + input.charAt(i));
        }

        return builder.toString();
    }

    private static boolean outdatedWarning = false;
    public static String formatWithRegexGroups(String input, String regex, String delimiter, Map<String, String> replacements)
    {
        if (DataUtils.getJavaVersion() < 16)
        {
            if (!outdatedWarning)
            {
                PrintUtils.log("(Java version is )[" + DataUtils.getJavaVersion() + "](. Must be on Java )[16]( or newer to use unicode chat replacements.))", InfoType.ERROR);
                outdatedWarning = true;
            }
                
            return input;
        }

        Matcher matcher = Pattern.compile(regex).matcher(input);
        StringBuilder builder = new StringBuilder(input);

        int delta = 0;
        String replacement, result;

        for (MatchResult match : matcher.results().toList()) // Java 16 feature
        {
            result = match.group().replace(delimiter, "");
            if (!replacements.containsKey(result))
                continue;

            replacement = replacements.get(result);
            builder.replace(match.start() + delta, match.end() + delta, replacement);
            delta += replacement.length() - (match.end() - match.start());
        }

        return builder.toString();
    }

    public static String of(String input, String from, String to)
    {
        return of(input, from, to, "");
    }

    /** Contains an action to run when a player sends a chat message. */
    public static interface ChatAction
    {
        void run();
    }

    /** Contains an action to run when a chat message is expected, but not received within a window. */
    public static interface TimeoutAction
    {
        void run();
    }

    @EventHandler
    /** Listener method. */
    public void onChat(AsyncPlayerChatEvent event)
    {
        if (!active_threads.containsKey(event.getPlayer()))
            return;

        try {
            results.put(event.getPlayer(), event.getMessage());

            synchronized(active_threads.get(event.getPlayer()))
            {
                active_threads.get(event.getPlayer()).notify();
            }
        } catch (Exception e) {
            PrintUtils.printPrettyStacktrace(e);
        }

        event.setCancelled(true);
    }
}
