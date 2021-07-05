package com.yukiemeralis.blogspot.zenith;

import com.yukiemeralis.blogspot.zenith.module.java.ModuleManager;
import com.yukiemeralis.blogspot.zenith.module.java.annotations.Branch;
import com.yukiemeralis.blogspot.zenith.module.java.enums.BranchType;
import com.yukiemeralis.blogspot.zenith.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.zenith.utils.FileUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Branch(BranchType.RELEASE)
public class Zenith extends JavaPlugin
{
	private static Zenith server_instance;
	private static ModuleManager module_manager;

	private static String nms_version;

	private static boolean isBeingDisabled = false;

	@Override
	public void onEnable()
	{
		server_instance = this;

		String bukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
		nms_version = bukkitPackage.substring(bukkitPackage.lastIndexOf('.') + 1);

		PrintUtils.log("Server version is determined to be \"" + nms_version + "\"", InfoType.INFO);

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

		//
		// Module loading
		//

		long time = System.currentTimeMillis();

		module_manager.gatherAllModules();
		module_manager.enableAllModules();

		PrintUtils.log("Gathering and enabling took " + (System.currentTimeMillis() - time) + " ms.", InfoType.INFO);
	}
	
	@Override
	public void onDisable()
	{
		isBeingDisabled = true;
		module_manager.getEnabledModules().forEach(module -> {
			module_manager.disableModule(module.getName(), CallerToken.ZENITH);
		});
	}

	public static Zenith getInstance()
	{
		return server_instance;
	}

	public static String getNMSVersion()
	{
		return nms_version;
	}

	public static ModuleManager getModuleManager()
	{
		return module_manager;
	}

	public static boolean isBeingDisabled()
	{
		return isBeingDisabled;
	}
}