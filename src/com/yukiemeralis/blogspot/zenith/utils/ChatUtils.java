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

    public static void expectChat(CommandSender target, ChatAction action, TimeoutAction timeout, long timeoutTime, Object... args)
    {
        new BukkitRunnable() {
            @Override
            public void run() 
            {
                timeout.run();
            }
        }.runTaskLater(Zenith.getInstance(), timeoutTime);
    }

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

    public static String receiveResult(CommandSender target)
    {
        return results.get(target);
    }

    public static void deleteResult(CommandSender target)
    {
        results.put(target, "0123456789");
        results.remove(target);
    }

    public static void forceNotify(CommandSender target, boolean stop)
    {
        if (!active_threads.containsKey(target))
            return;

        if (stop && !deactivated_threads.contains(target))
            deactivated_threads.add(target);

        active_threads.get(target).notify();
    }

    public static interface ChatAction
    {
        void run();
    }

    public static interface TimeoutAction
    {
        void run();
    }

    @EventHandler
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
