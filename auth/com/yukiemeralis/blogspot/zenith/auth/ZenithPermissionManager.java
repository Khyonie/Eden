package com.yukiemeralis.blogspot.zenith.auth;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.core.CompletionsManager;
import com.yukiemeralis.blogspot.zenith.core.CompletionsManager.ObjectMethodPair;
import com.yukiemeralis.blogspot.zenith.permissions.PermissionsManager;
import com.yukiemeralis.blogspot.zenith.permissions.PlayerData;
import com.yukiemeralis.blogspot.zenith.utils.FileUtils;
import com.yukiemeralis.blogspot.zenith.utils.JsonUtils;
import com.yukiemeralis.blogspot.zenith.utils.Option;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

public class ZenithPermissionManager extends PermissionsManager
{
    private Map<String, PermissionGroup> permissionGroups = new HashMap<>(); 

    public ZenithPermissionManager()
    {
        List<File> corruptFiles = new ArrayList<>();
        File groupFile = new File("./plugins/Zenith/permissiongroups/");

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
                PrintUtils.log("(A permission group was found to be corrupt! Moved to lost and found...)", InfoType.ERROR);
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
            PrintUtils.log("(Failed to register completions for ZenithPermissionManager. This may mean the module is outdated or corrupt.)", InfoType.ERROR);
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

            PlayerData account = Zenith.getPermissionsManager().getPlayerData((Player) sender);

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
        "Zenith.zen", 
        "Zenith.zen.helpall"
    );

    public static PermissionGroup ADMIN = new PermissionGroup(
        "administrator", 

        // Core command
        "Zenith.zen.mods",
        "Zenith.zen.mods.config",
        "Zenith.zen.mods.saveconfig",
        "Zenith.zen.mods.reloadconfig",
        "Zenith.zen.mods.clean",
        "Zenith.zen.mods.readconfig",
        "Zenith.zen.logging.verbose",
        "Zenith.zen.logging.export",
        "Zenith.zen.mm.load",
        "Zenith.zen.mm.unload",
        "Zenith.zen.mm.enable",
        "Zenith.zen.mm.disable",
        "Zenith.zen.mm.download",
        "Zenith.zen.data.repair",
        "Zenith.zen.data.reset",

        // Enchants command
        "ZenithEnchants.zench.apply",
        "ZenithEnchants.zench.gui",
        
        // Events command
        "ZenithEvents.scripts",
        "ZenithEvents.scripts.add",
        "ZenithEvents.scripts.remove",
        "ZenithEvents.scripts.edit",

        // Networking command
        "ZenithNetworking.zendl.tdl"
    );
}
