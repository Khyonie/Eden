package coffee.khyonieheart.eden.permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EmergencyPermissionsManager extends PermissionsManager
{
    @Override
    public boolean isAuthorized(CommandSender sender, String permission)
    {
        if (!(sender instanceof Player))
            return true;
        return sender.isOp();
    }
}
