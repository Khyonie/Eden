package fish.yukiemeralis.eden.auth;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.core.CompletionsManager;
import fish.yukiemeralis.eden.core.CompletionsManager.ObjectMethodPair;
import fish.yukiemeralis.eden.permissions.PermissionsManager;
import fish.yukiemeralis.eden.permissions.PlayerData;
import fish.yukiemeralis.eden.utils.FileUtils;
import fish.yukiemeralis.eden.utils.JsonUtils;
import fish.yukiemeralis.eden.utils.Option;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.PrintUtils.InfoType;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

public class EdenPermissionManager extends PermissionsManager
{
    private Map<String, PermissionGroup> permissionGroups = new HashMap<>(); 

    public EdenPermissionManager()
    {
        List<File> corruptFiles = new ArrayList<>();
        File groupFile = new File("./plugins/Eden/permissiongroups/");

        // Generate default groups
        if (!new File(groupFile.getAbsolutePath() + "/default.json").exists())
            JsonUtils.toJsonFile(groupFile.getAbsolutePath() + "/default.json", DEFAULT);
        if (!new File(groupFile.getAbsolutePath() + "/administrator.json").exists())
            JsonUtils.toJsonFile(groupFile.getAbsolutePath() + "/administrator.json", ADMIN);

        // Populate groups list
        for (File f : groupFile.listFiles())
        {
            PermissionGroup group = JsonUtils.fromJsonFile(f.getAbsolutePath(), PermissionGroup.class);

            if (group == null)
            {
                PrintUtils.log("<A permission group was found to be corrupt! Moved to lost and found...>", InfoType.ERROR);
                corruptFiles.add(f);
                continue;
            }

            permissionGroups.put(group.getName(), group);
        }

        corruptFiles.forEach(FileUtils::moveToLostAndFound);

        // Register completions
        try {
            CompletionsManager.registerCompletion("GROUP", new ObjectMethodPair(this, this.getClass().getMethod("getGroupNames")), true);
        } catch (NoSuchMethodException | SecurityException e) { // This probably can't fire unless the module is outdated
            PrintUtils.log("<Failed to register completions for EdenPermissionManager. This may mean the module is outdated or corrupt.>", InfoType.ERROR);
            PrintUtils.printPrettyStacktrace(e);
        } 
    }

    public PermissionGroup getGroup(String name)
    {
        return permissionGroups.get(name);
    }

    public List<String> getGroupNames()
    {
        return new ArrayList<>(permissionGroups.keySet());
    }

    public Map<String, PermissionGroup> getAllGroups()
    {
        return permissionGroups;
    }

    public Option<String> addGroup(PermissionGroup group)
    {
        Option<String> option = new Option<>(String.class);

        if (permissionGroups.containsKey(group.getName()))
        {
            option.some("A permission group with that name already exists.");
            return option;
        }

        permissionGroups.put(group.getName(), group);

        return option;
    }

    public boolean deleteGroup(PermissionGroup group)
    {
        return permissionGroups.remove(group.getName()) != null;
    }
 
    @Override
    public boolean isAuthorized(CommandSender sender, String permission)
    {
        // Console and command blocks always have access
        if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender || sender instanceof CommandMinecart)
        {
            return true;
        }

        if (sender instanceof Player)
        {
            // Operators always have access
            if (((Player) sender).isOp())
            {
                return true;
            }  

            PlayerData account = Eden.getPermissionsManager().getPlayerData((Player) sender);

            // If the player has no account-specific permissions, check their groups
            if (account.getPermissions() == null)
            {
                return isGroupAuthorized(account, (Player) sender, permission);
            }
            
            // Player has account-specific permissions, those take priority, so we check those first
            if (isAccountAuthorized(account, permission))
                return true; 

            // Player was not authorized with their account, check groups
            if (isGroupAuthorized(account, (Player) sender, permission))
                return true;
        }

        // User lacks authorization
        return false;
    }

    private boolean isGroupAuthorized(PlayerData account, Player sender, String permission)
    {
        // If the player is not part of any groups, they don't have any grouped permissions
        if (account.getPermissionGroups() == null)
        {
            return false;
        }

        if (account.getRevokedPermissions() != null)
            if (account.getRevokedPermissions().contains(permission))
            {
                return false;
            }

        // Otherwise check all given groups for permission, removing unknown groups in the process
        Iterator<String> iter = account.getPermissionGroups().iterator();
        while (iter.hasNext())
        {
            String label = iter.next();
            // Unknown permission group
            if (!this.permissionGroups.containsKey(label))
            {
                PrintUtils.log("Removing unknown permission group \"[" + label + "]\" from player \"{" + sender.getDisplayName() + "}\"'s data.", InfoType.WARN);
                iter.remove();
                continue;
            }

            PermissionGroup group = permissionGroups.get(label);
            if (group.isAuthorized(permission))
                return true;
        }

        return false;
    }

    private boolean isAccountAuthorized(PlayerData account, String permission)
    {
        if (account.getRevokedPermissions() == null)
        {
            if (account.hasPermission(permission))
            {
                return true;
            }

            return false;
        }
            
        if (account.hasPermission(permission) && !account.getRevokedPermissions().contains(permission))
        {
            return true;
        }

        return false;
    }

    // Default permission group
    public static PermissionGroup DEFAULT = new PermissionGroup(
        "default",

        // Perms
        "Rosetta.eden", 
        "Rosetta.eden.data.password",
        "Rosetta.eden.helpall"
    );

    public static PermissionGroup ADMIN = new PermissionGroup(
        "administrator", 

        // Core command
        "Rosetta.eden.mods",
        "Rosetta.eden.mods.config",
        "Rosetta.eden.mods.saveconfig",
        "Rosetta.eden.mods.reloadconfig",
        "Rosetta.eden.mods.clean",
        "Rosetta.eden.mods.readconfig",
        "Rosetta.eden.logging.verbose",
        "Rosetta.eden.logging.export",
        "Rosetta.eden.mm.load",
        "Rosetta.eden.mm.unload",
        "Rosetta.eden.mm.enable",
        "Rosetta.eden.mm.disable",
        "Rosetta.eden.data",
        "Rosetta.eden.data.clearpassword",
        "Rosetta.eden.data.repair",
        "Rosetta.eden.data.reset",
        "Rosetta.eden.sudo",
        "Rosetta.eden.disengage",
        "Rosetta.eden.recachepu",

        // Perms management
        "Rosetta.perms.specific.add",
        "Rosetta.perms.specific.remove",
        "Rosetta.perms.group.add",
        "Rosetta.perms.group.remove",
        "Rosetta.perms.group.create",
        "Rosetta.perms.group.delete",
        "Rosetta.perms.group.assign",
        "Rosetta.perms.group.unassign",

        // Flock commands
        "Flock.edenmr",
        "Flock.edenmr.sync",
        "Flock.edenmr.upgrade",
        "Flock.edenmr.add",
        "Flock.edenmr.remove",
        "Flock.edenmr.open",
        "Flock.edenmr.exportblank",
        "Flock.edenmr.synctimestamps",
        "Flock.edenmr.gentimestamp"
    );
}
