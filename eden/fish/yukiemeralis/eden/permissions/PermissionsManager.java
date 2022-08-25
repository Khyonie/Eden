package fish.yukiemeralis.eden.permissions;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.utils.FileUtils;
import fish.yukiemeralis.eden.utils.JsonUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.PrintUtils.InfoType;

/**
 * Base class for all Eden permissions handlers.
 * @author Yuki_emeralis
 */
public abstract class PermissionsManager 
{
	private Map<Player, PlayerData> active_players = new HashMap<>();
    private List<Player> elevated_users = new ArrayList<>();

	/**
	 * Boolean method that runs a check on a command sender to see if they have appropriate permissions.
	 * @param sender The command sender.
	 * @param permission The permission to check.
	 * @return Whether or not the command sender has authorization for a resource.
	 */
    public abstract boolean isAuthorized(CommandSender sender, String permission);

	public Map<Player, PlayerData> getAllData()
    {
        return active_players;
    }

    public PlayerData getOfflinePlayerData(String uuid)
    {
        if (!Eden.getUuidCache().values().contains(uuid))
            return null;
        File accountFile = new File("./plugins/Eden/playerdata/" + uuid + ".json");

        PlayerData account;
        if (!accountFile.exists())
        {
            PrintUtils.log("§cUser with UUID \"{" + uuid + "}§c\" is registered in cache but does not have a data file! Creating one...", InfoType.ERROR);
            account = new PlayerData();
            JsonUtils.toJsonFile(accountFile.getAbsolutePath(), account);
            return account;
        }

        account = JsonUtils.fromJsonFile(accountFile.getAbsolutePath(), PlayerData.class);

        if (account == null)
        {
            PrintUtils.log("<Account file for " + uuid + " is corrupt! Moving to " + FileUtils.moveToLostAndFound(accountFile).getAbsolutePath() + ">", InfoType.ERROR);
            accountFile.delete();

            account = new PlayerData();
            JsonUtils.toJsonFile(accountFile.getAbsolutePath(), account);

            return account;
        }

        return account;
    }

    /**
     * Returns a player's data. </p>
     * If the account isn't loaded into memory, the account will be loaded from a local file, or generated fresh.
     * @param player The player to obtain an account to
     * @return The data tied to this player
     */
    public PlayerData getPlayerData(Player player)
    {
        if (active_players.containsKey(player))
            return active_players.get(player);

        PlayerData account;

        File accountFile = new File("./plugins/Eden/playerdata/" + player.getUniqueId() + ".json");
        if (!accountFile.exists())
        {
            account = new PlayerData();
            account.addPermissionGroup("default");
            account.setLastKnownIP(player.getAddress().getAddress().getHostAddress());
            JsonUtils.toJsonFile(accountFile.getAbsolutePath(), account);
        }

        account = JsonUtils.fromJsonFile(accountFile.getAbsolutePath(), PlayerData.class); 
        
        if (account == null)
        {
            PrintUtils.log("<Account file for " + player.getName() + " is corrupt! Moving to " + (FileUtils.moveToLostAndFound(accountFile).getAbsolutePath().replace('\\', '/')) + ">", InfoType.ERROR);
            accountFile.delete();

            account = new PlayerData();
            account.addPermissionGroup("default");
            JsonUtils.toJsonFile(accountFile.getAbsolutePath(), account);

            return getPlayerData(player);
        }

        // Update outdated accounts

		PlayerData data = new PlayerData();
        if (!data.getDataVersion().equals(account.getDataVersion()))
        {
            List<String> knownFields = new ArrayList<>();
            for (Field f : data.getClass().getDeclaredFields())
                knownFields.add(f.getName());

            List<String> knownCurrentFields = new ArrayList<>();
            for (Field f : account.getClass().getDeclaredFields())
                knownCurrentFields.add(f.getName());

            int newFields = 0;
            // Add missing new fields
            for (String field : knownFields)
            {
                if (!knownCurrentFields.contains(field))
                    try {
                        Field f = account.getClass().getDeclaredField(field);
                        f.setAccessible(true);
                        f.set(account, knownCurrentFields.getClass().getDeclaredField(field));

                        newFields++;
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        PrintUtils.printPrettyStacktrace(e);
                    }
            }

            int deletedFields = 0;
            // Remove outdated fields
            for (String field : knownCurrentFields)
            {
                if (!knownFields.contains(field))
                    try {
                        Field f = account.getClass().getDeclaredField(field);
                        f.setAccessible(true);
                        f.set(account, null);

                        deletedFields++;
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        PrintUtils.printPrettyStacktrace(e);
                    }
            }

            PrintUtils.log("Updated player data file for [" + player.getDisplayName() + "]");
            String accountVersion = account.getDataVersion();
            if (accountVersion == null)
                accountVersion = "unknown";
                
            PrintUtils.log("Versions: [" + accountVersion + "] -> {" + data.getDataVersion() + "}");
            PrintUtils.log("New fields: [" + newFields + "], deleted fields: {" + deletedFields + "}");

            account.updateDataVersion(data.getDataVersion());
        }

        return account;
    }

	public void addUserData(Player player, PlayerData data)
	{
		this.active_players.put(player, data);
	}

	public void removeUserData(Player player)
	{
		this.active_players.remove(player);
	}

    public void addElevatedUser(Player player)
    {
        PrintUtils.log("§aPlayer \"" + player.getName() + "\" is now elevated.");
        elevated_users.add(player);
    }

    public void removeElevatedUser(Player player)
    {
        if (!elevated_users.contains(player))
            return;

        PrintUtils.log("§aPlayer \"" + player.getName() + "\" is no longer elevated.");
        elevated_users.remove(player);
    }

    public boolean isElevated(Player player)
    {
        return elevated_users.contains(player);
    }
}