package fish.yukiemeralis.eden.auth;

import java.util.Iterator;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import fish.yukiemeralis.eden.command.CommandManager;
import fish.yukiemeralis.eden.command.EdenCommand;
import fish.yukiemeralis.eden.utils.exception.TimeSpaceDistortionException;
import fish.yukiemeralis.eden.utils.option.None;
import fish.yukiemeralis.eden.utils.option.Some;

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

            switch (CommandManager.getEdenCommand(str))
            {
                case Some s -> {
                    if (!s.unwrap(EdenCommand.class).testBasePermission(event.getPlayer(), str))
                    {
                        iter.remove();
                        continue;
                    }
                }
                case None n -> { 
                    continue; 
                }
                default -> throw new TimeSpaceDistortionException();
            }

            // Minecraft/bukkit commands
            if (!CommandManager.getCommand(str).testPermissionSilent(event.getPlayer()))
                iter.remove();
        }
    }   
}