/*
Copyright 2021 Yuki_emeralis https://yukiemeralis.blogspot.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
	or "LICENSE.txt" at the root of this project folder.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package fish.yukiemeralis.eden;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.annotation.Branch;
import fish.yukiemeralis.eden.module.event.EdenFinishLoadingEvent;
import fish.yukiemeralis.eden.module.java.ModuleManager;
import fish.yukiemeralis.eden.module.java.enums.BranchType;
import fish.yukiemeralis.eden.module.java.enums.CallerToken;
import fish.yukiemeralis.eden.permissions.EmergencyPermissionsManager;
import fish.yukiemeralis.eden.permissions.PermissionsManager;
import fish.yukiemeralis.eden.prelude.Prelude;
import fish.yukiemeralis.eden.utils.DataUtils;
import fish.yukiemeralis.eden.utils.FileUtils;
import fish.yukiemeralis.eden.utils.JsonUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.logging.ClassicLogger;
import fish.yukiemeralis.eden.utils.logging.Logger.InfoType;
import fish.yukiemeralis.eden.utils.logging.PrecipiceLogger;
import fish.yukiemeralis.eden.utils.option.Option;
import fish.yukiemeralis.eden.utils.option.OptionState;

/**
 * Represents the Eden core plugin, with module management and commands.
 * @author Yuki_emeralis
 */
@Branch(BranchType.RELEASE_CANDIDATE)
public class Eden extends JavaPlugin
{
	private static Eden server_instance;
	private static ModuleManager module_manager;
	private static PermissionsManager permissions_manager; 

	private static String nms_version;

	private static boolean isBeingDisabled = false;
	private static boolean isBeingEnabled = true;

	private static Map<String, String> uuidMap = new HashMap<>();

	private static Map<String, String> config = new HashMap<>();
	private static Map<String, String> defaultConfig = new HashMap<>(Map.of(
		"eColor", "FFB7C5", 
		"verboseLogging", "false",
		"flyingSolo", "false",
		"elevatedUsersIgnorePerms", "true",
		"preferredPermissionsManager", "fish.yukiemeralis.eden.auth.EdenPermissionManager",
		"displayModErrorsWithoutVerbose", "false",
		"loggingTheme", "classic", // Options are "classic" and "precipice"
		"prelude", "true"
	));

	@Override
	@SuppressWarnings("unchecked")
	public void onEnable()
	{
		server_instance = this;

		String bukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
		nms_version = bukkitPackage.substring(bukkitPackage.lastIndexOf('.') + 1);

		module_manager = new ModuleManager();

		//
		// Various startup things
		//

        // Modules folder
        FileUtils.ensureFolder("./plugins/Eden/mods");

        // Lost and found bin
        FileUtils.ensureFolder("./plugins/Eden/lost-and-found");

        // Configs file
        FileUtils.ensureFolder("./plugins/Eden/configs");

		// Eden config file
		File edenconfig = new File("./plugins/Eden/edenconfig.json");
		if (!edenconfig.exists())
			JsonUtils.toJsonFile(edenconfig.getAbsolutePath(), defaultConfig);

		// Attempt to load config
		config = (Map<String, String>) JsonUtils.fromJsonFile(edenconfig.getAbsolutePath(), HashMap.class);
		if (config == null) 
		{
			PrintUtils.log("<Eden configuration file is corrupt! Moving to lost and found...>", InfoType.ERROR);
			FileUtils.moveToLostAndFound(edenconfig);

			config = new HashMap<>(defaultConfig);
			JsonUtils.toJsonFile(edenconfig.getAbsolutePath(), config);
		}

		// Update config keys
		for (String key : defaultConfig.keySet())
			if (!config.containsKey(key))
				config.put(key, defaultConfig.get(key));

		// Load logger
		switch (config.get("loggingTheme"))
		{
			case "classic" -> PrintUtils.setLoggerTheme(ClassicLogger.class);
			default -> PrintUtils.setLoggerTheme(PrecipiceLogger.class);
		}

		PrintUtils.log("Server version is determined to be \"[" + nms_version + "]\"", InfoType.INFO);
		PrintUtils.log("Eden API version is determined to be \"[" + DataUtils.getMetaVersion() + "]\" | \"(" + DataUtils.getEdenVersion() + ")\"");

		// Search for missing keys in the config
		for (String key : defaultConfig.keySet())
		{
			if (config.containsKey(key))
				continue;

			PrintUtils.log("(Eden config is missing key \"" + key + "\". Filling from default value...)");
			config.put(key, defaultConfig.get(key));
		}

		// UUID cache
		File uuidCacheFile = new File("./plugins/Eden/uuidcache.json");
		if (!uuidCacheFile.exists())
			JsonUtils.toJsonFile(uuidCacheFile.getAbsolutePath(), uuidMap);

		// Attempt to load cache
		uuidMap = (Map<String, String>) JsonUtils.fromJsonFile(uuidCacheFile.getAbsolutePath(), HashMap.class);
		if (uuidMap == null)
		{
			PrintUtils.log("<Player UUID cache file is corrupt! Moving to lost and found...>", InfoType.ERROR);
			FileUtils.moveToLostAndFound(uuidCacheFile);

			uuidMap = new HashMap<>();
			JsonUtils.toJsonFile(uuidCacheFile.getAbsolutePath(), uuidMap);
		}

		if (Boolean.valueOf(config.get("verboseLogging")))
			PrintUtils.enableVerboseLogging();

		//
		// Module loading
		//

		long time = System.currentTimeMillis();

		module_manager.performFullLoad();
		module_manager.enableAllModules();

		// Hotload prelude
		Prelude preludeModule = new Prelude();
		module_manager.enableModule(preludeModule);

		// Ensure a permissions manager is in play
		Class<?> pmClass = module_manager.getCachedClass(Eden.getEdenConfig().get("preferredPermissionsManager"));
		PermissionsManager pm = null;

		if (pmClass != null)
		{
			try {
				pm = (PermissionsManager) pmClass.getConstructor().newInstance();
				setPermissionsManager(pm);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				PrintUtils.log(
					"<Instatiation of permission manager >{" + Eden.getEdenConfig().get("preferredPermissionsManager") + "}< failed. Reason: " +
					e.getClass().getName() + " | " + e.getMessage() + " " + (e.getCause() != null ? "(caused by: " + e.getCause().getClass().getName() + 
					" | " + e.getCause().getMessage() : "") + ">"
				);
				}
		} else {
			PrintUtils.log("<Failed to find valid permission manager " + Eden.getEdenConfig().get("preferredPermissionsManager") + ".>");
			PrintUtils.log("<No permission manager has been set. Emergency permissions manager will be used. Please install a module with a permissions manager.>", InfoType.WARN);
			setPermissionsManager(new EmergencyPermissionsManager());
		}
		
		PrintUtils.log("Loading and enabling took [" + (System.currentTimeMillis() - time) + "] ms.", InfoType.INFO);
		isBeingEnabled = false;	

		this.getServer().getPluginManager().callEvent(new EdenFinishLoadingEvent());
	}
	
	@Override
	public void onDisable()
	{
		isBeingDisabled = true;
		module_manager.getEnabledModules().forEach(module -> {
			module_manager.disableModule(module.getName(), CallerToken.EDEN, true);
		});

		Bukkit.getOnlinePlayers().forEach(player -> {
			JsonUtils.toJsonFile("./plugins/Eden/playerdata/" + player.getUniqueId().toString() + ".json", permissions_manager.getPlayerData(player));
		});

		JsonUtils.toJsonFile("./plugins/Eden/uuidcache.json", uuidMap);
		JsonUtils.toJsonFile("./plugins/Eden/edenconfig.json", config);
	}

	/**
	 * Obtains an instance of Eden that is currently running.
	 * @return The current instance of Eden.
	 */
	public static Eden getInstance()
	{
		return server_instance;
	}

	/**
	 * Obtains the exact server version running.
	 * @return The current server version.
	 */
	public static String getNMSVersion()
	{
		return nms_version;
	}

	/**
	 * Obtains the module manager in use for the server.
	 * @return The current module manager.
	 */
	public static ModuleManager getModuleManager()
	{
		return module_manager;
	}

	/**
	 * Obtains the permissions manager in use for the server.
	 * @return The current permissions manager.
	 */
	public static PermissionsManager getPermissionsManager()
	{
		return permissions_manager;
	}

	/**
	 * Obtains the UUID cache in use by Eden.
	 * @return The Eden UUID cache.
	 */
	public static Map<String, String> getUuidCache()
	{
		return uuidMap;
	}

	
	/**
	 * Sets the server's permissions manager. Calling this method will notify the console of the change.
	 * @param manager The manager to set.
	 */
	public static void setPermissionsManager(PermissionsManager manager)
	{
		if (manager == null)
			return;

		String managerName = "None";
		if (permissions_manager != null)
		{
			managerName = permissions_manager.getClass().getSimpleName();

			// Copy revelant fields
			Field target; 
			try {
				target = PermissionsManager.class.getDeclaredField("active_players");
				target.set(manager, target.get(permissions_manager));

				target = PermissionsManager.class.getDeclaredField("elevated_users");
				target.set(manager, target.get(permissions_manager));
			} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
				PrintUtils.log("<Failed to switch permissions managers. Reason is below:>", InfoType.ERROR);
				PrintUtils.printPrettyStacktrace(e);
				return;
			}
		}

		Option host = module_manager.getHostModule(manager.getClass());
		String name = "from Unknown module";
		if (host.getState().equals(OptionState.SOME))
			 if (host.unwrap(EdenModule.class) != null)
				name = "from module \"{" + host.unwrap(EdenModule.class).getName() + "}\"";
				
		PrintUtils.log("Permissions manager: [" + managerName + "] -> {" + manager.getClass().getSimpleName() + "} \\(" + name + "\\)");

		permissions_manager = manager;

		if (!isBeingEnabled)
			PrintUtils.log("Permissions manager has been set. If this is unexpected, perform an audit of installed modules immediately.", InfoType.INFO);
	}

	/**
	 * Obtains the map of strings that represents Eden's global configuration file.
	 * @return Eden's configuration.
	 */
	public static Map<String, String> getEdenConfig()
	{
		return config;
	}

	public static void callEvent(Event event)
	{
		getInstance().getServer().getPluginManager().callEvent(event);
	}

	public static void callEvent(Event event, boolean sync)
	{
		if (sync)
		{
			callSyncEvent(event);
			return;
		}

		callEvent(event);
	}

	public static void callSyncEvent(Event event)
	{
		Bukkit.getScheduler().runTask(getInstance(), () -> callEvent(event));
	}

	/**
	 * Whether or not Eden is being disabled currently. Eden, when being disabled, skips all callertoken checks for disabling/unloading modules.
	 * @return Whether or not Eden is being disabled.
	 */
	public static boolean isBeingDisabled()
	{
		return isBeingDisabled;
	}

	private static String version = null;
	/**
	 * Returns the current version of Eden, derived from the file name. This may not be accurate.
	 * @return The current version of Eden.
	 * @see {@link DataUtils#getEdenVersion()} for friendly version name and {@link DataUtils#getMetaVersion()} for meta versioning
	 * @deprecated Use DataUtils implementation instead. This method will remain for backwards compatibility until further notice.
	 */
	@Deprecated(forRemoval = false, since = "1.7.3")
    public static String getEdenVersion()
    {
        if (version == null)
        {
            for (String filename : new File("./plugins/").list())
            {
                if (filename.startsWith("Eden-"))
                {
                    PrintUtils.logVerbose("Found Eden plugin, pulling the version from that...", InfoType.INFO);
                    version = "v" + filename.replaceAll("Eden-|.jar", "");
                    return version;
                }
            }

            PrintUtils.log("<Failed to locate the Eden plugin file! Version has been set to \"unknown\". Please ensure that the plugin starts with \"Eden-\".>", InfoType.WARN);
            version = "Unknown";
        }

        return version;
    }
}