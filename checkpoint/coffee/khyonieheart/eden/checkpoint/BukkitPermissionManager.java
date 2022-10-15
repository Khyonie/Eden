package coffee.khyonieheart.eden.checkpoint;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import coffee.khyonieheart.eden.permissions.PermissionsManager;

/**
 * Simple permissions manager with integration with Bukkit's default permissions scheme
 * @author Yuki_emeralis
 * @since 1.4.10
 */
public class BukkitPermissionManager extends PermissionsManager 
{
    @Override
    public boolean isAuthorized(CommandSender sender, String permission)
    {
        if (sender instanceof ConsoleCommandSender)
            return true;

        if (sender instanceof Player)
        {
            if (sender.isOp())
                return true;
            if (((Player) sender).hasPermission(permission))
                return true;
        }
        return false;
    }
}
