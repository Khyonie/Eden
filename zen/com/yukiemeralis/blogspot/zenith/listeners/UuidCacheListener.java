package com.yukiemeralis.blogspot.zenith.listeners;

import java.util.Iterator;

import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.module.java.annotations.HideFromCollector;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@HideFromCollector // Just in case Zenith gets installed as a module on accident
public class UuidCacheListener implements Listener
{
    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        if (Zenith.getUuidCache().containsKey(event.getPlayer().getName()))
        {
            if (!Zenith.getUuidCache().get(event.getPlayer().getName()).equals(event.getPlayer().getUniqueId().toString())) // Different UUID, same name
            {
                Zenith.getUuidCache().put(event.getPlayer().getName(), event.getPlayer().getUniqueId().toString());
                return;
            }

            // Same UUID, same name
            return;
        }

        // Clear entries where the user has a different name, same UUID
        Iterator<String> iter = Zenith.getUuidCache().values().iterator();
        while(iter.hasNext())
        {
            if (iter.next().equals(event.getPlayer().getUniqueId().toString()))
                iter.remove();
        }

        // Player is not cached
        Zenith.getUuidCache().put(event.getPlayer().getName(), event.getPlayer().getUniqueId().toString());
    }
}
