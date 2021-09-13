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

package com.yukiemeralis.blogspot.zenith;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.module.java.ModuleManager;
import com.yukiemeralis.blogspot.zenith.module.java.annotations.Branch;
import com.yukiemeralis.blogspot.zenith.module.java.enums.BranchType;
import com.yukiemeralis.blogspot.zenith.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.zenith.permissions.PermissionsManager;
import com.yukiemeralis.blogspot.zenith.utils.FileUtils;
import com.yukiemeralis.blogspot.zenith.utils.JsonUtils;
import com.yukiemeralis.blogspot.zenith.utils.Option;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.Option.OptionState;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents the Zenith core plugin, with module management and commands.
 * @author Yuki_emeralis
 */
@Branch(BranchType.NIGHTLY)
public class Zenith extends JavaPlugin
{
	private static Zenith server_instance;
	private static ModuleManager module_manager;
	private static PermissionsManager permissions_manager; 

	private static String nms_version;

	private static boolean isBeingDisabled = false;
	private static boolean isBeingEnabled = true;

	private static Map<String, String> uuidMap = new HashMap<>();

	private static Map<String, String> config = new HashMap<>();
	private static Map<String, String> defaultConfig = new HashMap<>() {{
		put("zColor", "FFB7C5");
		put("verboseLogging", "false");
	}};

	@Override
	@SuppressWarnings("unchecked")
	public void onEnable()
	{
		server_instance = this;

		String bukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
		nms_version = bukkitPackage.substring(bukkitPackage.lastIndexOf('.') + 1);

		PrintUtils.log("Server version is determined to be \"[" + nms_version + "]\"", InfoType.INFO);

		module_manager = new ModuleManager();

		//
		// Various startup things
		//

        // Modules folder
        FileUtils.ensureFolder("./plugins/Zenith/mods");

        // Lost and found bin
        FileUtils.ensureFolder("./plugins/Zenith/lost-and-found");

        // Configs file
        FileUtils.ensureFolder("./plugins/Zenith/configs");

		// Zenith config file
		File zenconfig = new File("./plugins/Zenith/zenithconfig.json");
		if (!zenconfig.exists())
			JsonUtils.toJsonFile(zenconfig.getAbsolutePath(), defaultConfig);

		// Attempt to load config
		config = (Map<String, String>) JsonUtils.fromJsonFile(zenconfig.getAbsolutePath(), HashMap.class);
		if (config == null) 
		{
			PrintUtils.log("(Zenith configuration file is corrupt! Moving to lost and found...)", InfoType.ERROR);
			FileUtils.moveToLostAndFound(zenconfig);

			config = new HashMap<>(defaultConfig);
			JsonUtils.toJsonFile(zenconfig.getAbsolutePath(), config);
		}

		// UUID cache
		File uuidCacheFile = new File("./plugins/Zenith/uuidcache.json");
		if (!uuidCacheFile.exists())
			JsonUtils.toJsonFile(uuidCacheFile.getAbsolutePath(), uuidMap);

		// Attempt to load cache
		uuidMap = (Map<String, String>) JsonUtils.fromJsonFile(uuidCacheFile.getAbsolutePath(), HashMap.class);
		if (uuidMap == null)
		{
			PrintUtils.log("(Player UUID cache file is corrupt! Moving to lost and found...)", InfoType.ERROR);
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

		PrintUtils.log("Loading and enabling took [" + (System.currentTimeMillis() - time) + "] ms.", InfoType.INFO);
		isBeingEnabled = false;
	}
	
	@Override
	public void onDisable()
	{
		isBeingDisabled = true;
		module_manager.getEnabledModules().forEach(module -> {
			module_manager.disableModule(module.getName(), CallerToken.ZENITH);
		});

		Bukkit.getOnlinePlayers().forEach(player -> {
			JsonUtils.toJsonFile("./plugins/Zenith/playerdata/" + player.getUniqueId().toString() + ".json", permissions_manager.getPlayerData(player));
		});

		JsonUtils.toJsonFile("./plugins/Zenith/uuidcache.json", uuidMap);
	}

	/**
	 * Obtains an instance of Zenith that is currently running.
	 * @return The current instance of Zenith.
	 */
	public static Zenith getInstance()
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
	 * Obtains the UUID cache in use by Zenith.
	 * @return The Zenith UUID cache.
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
				PrintUtils.log("(Failed to switch permissions managers. Reason is below:)", InfoType.ERROR);
				PrintUtils.printPrettyStacktrace(e);
				return;
			}
		}

		Option<ZenithModule> host = module_manager.getHostModule(manager.getClass());
		String name = "Unknown module";
		if (host.getState().equals(OptionState.SOME))
			 if (host.unwrap() != null)
				name = "from module \"{" + host.unwrap().getName() + "}\"";
				
		PrintUtils.log("Permissions manager: [" + managerName + "] -> {" + manager.getClass().getSimpleName() + "} \\(" + name + "\\)");

		permissions_manager = manager;

		if (!isBeingEnabled)
			PrintUtils.log("Permissions manager has been set. If this is unexpected, perform an audit of installed modules immediately.", InfoType.INFO);
	}

	/**
	 * Obtains the map of strings that represents Zenith's global configuration file.
	 * @return Zenith's configuration.
	 */
	public static Map<String, String> getZenithConfig()
	{
		return config;
	}

	/**
	 * Whether or not Zenith is being disabled currently. Zenith, when being disabled, skips all callertoken checks for disabling/unloading modules.
	 * @return Whether or not Zenith is being disabled.
	 */
	public static boolean isBeingDisabled()
	{
		return isBeingDisabled;
	}
}