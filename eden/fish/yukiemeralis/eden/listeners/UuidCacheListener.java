package fish.yukiemeralis.eden.listeners;

import java.util.Iterator;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.module.java.annotations.HideFromCollector;

@HideFromCollector // Just in case Eden gets installed as a module on accident
public class UuidCacheListener implements Listener
{
    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        if (Eden.getUuidCache().containsKey(event.getPlayer().getName()))
        {
            if (!Eden.getUuidCache().get(event.getPlayer().getName()).equals(event.getPlayer().getUniqueId().toString())) // Different UUID, same name
            {
                Eden.getUuidCache().put(event.getPlayer().getName(), event.getPlayer().getUniqueId().toString());
                return;
            }

            // Same UUID, same name
            return;
        }

        // Clear entries where the user has a different name, same UUID
        Iterator<String> iter = Eden.getUuidCache().values().iterator();
        while(iter.hasNext())
        {
            if (iter.next().equals(event.getPlayer().getUniqueId().toString()))
                iter.remove();
        }

        // Player is not cached
        Eden.getUuidCache().put(event.getPlayer().getName(), event.getPlayer().getUniqueId().toString());
    }
}
