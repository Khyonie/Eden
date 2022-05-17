package fish.yukiemeralis.eden.auth;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.PrintUtils.InfoType;

public class OpIpListener implements Listener
{
    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event)
    {
        if (!event.getPlayer().isOp())
            return;

        if (Eden.getPermissionsManager().getPlayerData(event.getPlayer()).getLastKnownIP().equals(event.getPlayer().getAddress().getAddress().getHostAddress()))
            return;

        if (!SecurityCore.getModuleInstance().getConfig().get("deopOnIpChange").equals("true"))
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
