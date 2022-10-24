package coffee.khyonieheart.eden.checkpoint;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

import coffee.khyonieheart.eden.Eden;
import coffee.khyonieheart.eden.permissions.PermissionsManager;
import coffee.khyonieheart.eden.permissions.PlayerData;
import coffee.khyonieheart.eden.rosetta.CompletionsManager;
import coffee.khyonieheart.eden.rosetta.CompletionsManager.ObjectMethodPair;
import coffee.khyonieheart.eden.utils.FileUtils;
import coffee.khyonieheart.eden.utils.JsonUtils;
import coffee.khyonieheart.eden.utils.PrintUtils;
import coffee.khyonieheart.eden.utils.logging.Logger.InfoType;
import coffee.khyonieheart.eden.utils.option.Option;

/**
 * Eden's permissions manager. Supplies default permissions, and supports permissions groups out of the box.
 * @since 1.4.10
 * @author Yuki_emeralis
 */
public class EdenPermissionManager extends PermissionsManager
{
    private Map<String, PermissionGroup> permissionGroups = new HashMap<>(); 

    /**
     * Eden permissions manager constructor.
     * @see {@link Eden#setPermissionsManager(PermissionsManager)}
     */
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

    /**
     * Obtains a permissions group with a given name. Has an OptionState of NONE if the group does not exist.
     * @param name A permissions group with the given name.
     * @return A permissions group with the given name.
     * @eden.optional {@link coffee.khyonieheart.eden.auth.PermissionGroup}
     */
    public Option getGroup(String name)
    {
        return permissionGroups.get(name) != null ? Option.some(permissionGroups.get(name)) : Option.none();
    }

    /**
     * Obtains a list of all valid permissions group names.
     * @return A list of all permissions group names.
     */
    public List<String> getGroupNames()
    {
        return new ArrayList<>(permissionGroups.keySet());
    }

    /**
     * Obtains a map of all groups and their names.
     * @return A map of all groups and their names.
     */
    public Map<String, PermissionGroup> getAllGroups()
    {
        return permissionGroups;
    }

    /**
     * Attempts to add a permissions group. Supplies an option where SOME denotes a failure, and provides a message
     * describing the reason.
     * @param group Permissions group to add.
     * @return The processing result of adding the given group.
     * @eden.optional java.lang.String
     */
    public Option addGroup(PermissionGroup group)
    {
        if (permissionGroups.containsKey(group.getName()))
        {
            return Option.some("A permission group with that name already exists.");
        }

        permissionGroups.put(group.getName(), group);

        return Option.none();
    }

    /**
     * Attempts to delete a group. Returns false if the group has not been registered.
     * @param group The permissions group to attempt to remove.
     * @return If the deletion was successful.
     */
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

    /** Default permission group */
    public static PermissionGroup DEFAULT = new PermissionGroup(
        "default",

        // Perms
        "Rosetta.eden", 
        "Rosetta.eden.data.password",
        "Rosetta.eden.helpall"
    );

    /** Administrator permission group */
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
        "Rosetta.eden.perms.specific.add",
        "Rosetta.eden.perms.specific.remove",
        "Rosetta.eden.perms.group.add",
        "Rosetta.eden.perms.group.remove",
        "Rosetta.eden.perms.group.create",
        "Rosetta.eden.perms.group.delete",
        "Rosetta.eden.perms.group.assign",
        "Rosetta.eden.perms.group.unassign"

        // TODO Flock commands
        // TODO Add base command for Eden to act as an extremely simple interface to the module manager
    );
}
