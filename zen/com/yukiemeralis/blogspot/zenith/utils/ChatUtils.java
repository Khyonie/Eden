package com.yukiemeralis.blogspot.zenith.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yukiemeralis.blogspot.zenith.Zenith;

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
                    e.printStackTrace();
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

    @SuppressWarnings("javadoc")
    public static interface ChatAction
    {
        void run();
    }

    @SuppressWarnings("javadoc")
    public static interface TimeoutAction
    {
        void run();
    }

    @EventHandler
    @SuppressWarnings("javadoc")
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
            e.printStackTrace();
        }

        event.setCancelled(true);
    }
}
