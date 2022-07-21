package fish.yukiemeralis.eden.auth;

import java.util.Iterator;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import fish.yukiemeralis.eden.command.CommandManager;

/**
 * Helper class to obscure commands the player does not have access to.<p>
 * If <code>obscureDisallowedCommands</code> is set to false, this listener does nothing.
 */
public class CommandObscureListener implements Listener
{
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandSend(PlayerCommandSendEvent event)
    {
        if (!SecurityCore.getModuleInstance().getConfig().getBoolean("obscureDisallowedCommands"))
            return;

        Iterator<String> iter = event.getCommands().iterator();
        String str;

        while (iter.hasNext())
        {
            str = iter.next();

            // Trim ambiguity options
            if (str.contains(":"))
            {
                iter.remove();
                continue;
            }

            if (CommandManager.getEdenCommand(str) != null)
            {
                // Eden commands
                if (!CommandManager.getEdenCommand(str).testBasePermission(event.getPlayer(), str))
                    iter.remove();
                continue;
            }

            // Minecraft/bukkit commands
            if (!CommandManager.getCommand(str).testPermissionSilent(event.getPlayer()))
                iter.remove();
        }
    }   
}