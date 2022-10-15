package fish.yukiemeralis.eden.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.module.annotation.HideFromCollector;

/**
 * Listener to update last known player IPs
 */
@HideFromCollector
public class PlayerIPListener implements Listener
{
    /**
     * On player join event handler
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event)
    {
        Eden.getPermissionsManager().getPlayerData(event.getPlayer()).setLastKnownIP(event.getPlayer().getAddress().getAddress().getHostAddress());
    }   
}
