package fish.yukiemeralis.eden.auth;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.logging.Logger.InfoType;

/**
 * Helper class to deop oped players when they login from a different IP than they did previously.<p>
 * If <code>deopOnIpChange</code> is set to false, this listener does nothing.
 */
public class OpIpListener implements Listener
{
    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event)
    {
        if (!event.getPlayer().isOp())
            return;

        if (Eden.getPermissionsManager().getPlayerData(event.getPlayer()).getLastKnownIP().equals(event.getPlayer().getAddress().getAddress().getHostAddress()))
            return;

        if (!SecurityCore.getModuleInstance().getConfig().getBoolean("deopOnIpChange"))
            return;

        // IP doesn't match, deop
        event.getPlayer().setOp(false);
        PrintUtils.log(
            "Operator " + event.getPlayer().getName() + " joined from new IP. (Last known: " + 
            Eden.getPermissionsManager().getPlayerData(event.getPlayer()).getLastKnownIP() + 
            ", new: " + event.getPlayer().getAddress().getAddress().getHostAddress() + 
            ") Deoping for security.", InfoType.WARN
        );

        PrintUtils.sendMessage(event.getPlayer(), "You have connected from a new IP. Restricting privileges.");
    }
}
