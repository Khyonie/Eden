package com.yukiemeralis.blogspot.zenith.module.java;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.command.CommandManager;
import com.yukiemeralis.blogspot.zenith.command.ZenithCommand;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule.LoadBefore;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule.ModInfo;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule.ZenConfig;
import com.yukiemeralis.blogspot.zenith.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.zenith.module.java.enums.PreventUnload;
import com.yukiemeralis.blogspot.zenith.utils.DataUtils;
import com.yukiemeralis.blogspot.zenith.utils.FileUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

public class ModuleManager 
{
    private final Map<String, Class<?>> class_cache = new HashMap<>();
	private final Map<String, ModuleClassLoader> loader_cache = new LinkedHashMap<>();

	private final Map<String, String> module_references = new HashMap<>();

	private Map<String, ZenithModule> gathered_modules = new HashMap<>();
	private List<ZenithModule> enabled_modules = new ArrayList<>();
	private List<ZenithModule> disabled_modules = new ArrayList<>();

	private final static Random random = new Random();

	/**
	 * Attempt to gather a ZenithModule out of a local file.
	 * @param file The file to gather from
	 * @return The module stored inside this file
	 */
	public ZenithModule gatherModulesFromFile(File file)
	{
		if (!file.exists())
		{
			PrintUtils.log("Attempted to load module from file " + file.getAbsolutePath() + ", but this file does not exist!", InfoType.ERROR);
			return null;
		}

		ModuleClassLoader loader;

		try {
			loader = new ModuleClassLoader(this.getClass().getClassLoader(), this, file);
		} catch (MalformedURLException | NullPointerException e) {
			e.printStackTrace();
			return null;
		}

		ZenithModule mod = loader.getCoreModule();
		String modName;

        // Load module info based on annotations
		if (mod.getClass().isAnnotationPresent(ModInfo.class))
		{
			ModInfo info = mod.getClass().getAnnotation(ModInfo.class);
			modName = info.modName();

            mod.setInfo(info);
		} else {
			modName = "Unnamed" + random.nextInt(1000);

			PrintUtils.sendMessage("Module " + file.getName() + " is present but does not specify a module name! Loading as \"" + modName + "\".", InfoType.WARN);

            mod.setBlankInfo(modName);
		}

        if (loader_cache.containsKey(modName))
        {
            PrintUtils.sendMessage("A module with the name \"" + modName + "\" already exists! Appending a \"-\"...", InfoType.WARN);
            modName = modName + "-";
        }

		module_references.put(modName, file.getAbsolutePath());
		loader_cache.put(modName, loader);

		return mod;
	}

	/**
	 * Copies all internal (core) modules from inside the jar
	 * @return
	 */
	private static List<File> getInternalModules()
	{
		File zenithFile = DataUtils.getZenithJar();
		List<File> moduleFiles = new ArrayList<>();

		try {
			JarFile jarFile = new JarFile(zenithFile);
			Enumeration<JarEntry> iter = jarFile.entries();

			JarEntry jarEntry = null;
			List<JarEntry> moduleEntries = new ArrayList<>();

			while (iter.hasMoreElements())
			{
				jarEntry = iter.nextElement();

				if (!jarEntry.getName().startsWith("internalModules") || jarEntry.isDirectory() || !jarEntry.getName().endsWith(".jar"))
					continue;

				moduleEntries.add(jarEntry);
			}

			// Create some files from the collected entries
			moduleEntries.forEach(entry -> {
				try {
					File file = FileUtils.inputStreamToFile(jarFile.getInputStream(entry), entry.getName().replace("internalModules/", ""));

					moduleFiles.add(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			jarFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return moduleFiles;
	}

	/**
	 * Removes a ZenithModule from memory in its entirety.
	 * @param name The name of the module to remove.
	 */
	public void removeModuleFromMemory(String name, CallerToken caller)
	{
		ZenithModule module = getDisabledModuleByName(name);

		if (module == null)
			return; // Can't unload a module that doesn't exist

		// Check for a required caller token
		if (module.getClass().isAnnotationPresent(PreventUnload.class))
		{
			CallerToken intendedCaller = module.getClass().getAnnotation(PreventUnload.class).value();

			if (!CallerToken.isEqualToOrHigher(caller, intendedCaller))
			{
				PrintUtils.log("An attempt was made to disable \"" + name + "\" but this module's @PreventDisable tag prevented it! Expected token: " + intendedCaller.name() + ", given: " + caller.name(), InfoType.WARN);
				return;
			}	
		}

		// Disable events
		module.getListeners().forEach(event -> {
			HandlerList.unregisterAll(event);
		});
		
		// Disable commands
		module.getCommands().forEach(command -> {
			CommandManager.unregisterCommand(command.getName());
		});

		class_cache.clear();

		gathered_modules.remove(name);
		disabled_modules.remove(module);
		enabled_modules.remove(module);
		loader_cache.remove(name);

	}
	
	/**
	 * Obtain a class instance from a loaded module class cache.
	 * @param name The fully-qualified classname to get
	 * @return The class under the given classname
	 */
	public Class<?> getCachedClass(String name)
	{
		Class<?> class_ = class_cache.get(name);
		
		if (class_ != null)
			return class_;

		// Otherwise look to see if it's in the global loader cache
		for (String current : loader_cache.keySet())
		{
			ModuleClassLoader loader = loader_cache.get(current);
			
			try {
				class_ = loader.findClass(name, false);
			} catch (ClassNotFoundException e) {}
			
			if (class_ != null)
			{
				return class_;
			}
		}
		
		return null;
	}

	public void setClass(String name, Class<?> class_)
	{
		if (!class_cache.containsKey(name))
			class_cache.put(name, class_);
	}

	public boolean hasReferenceTo(String name)
	{
		return module_references.containsKey(name);
	}

	public Map<String, String> getReferences()
	{
		return module_references;
	}

	public Map<String, ZenithModule> getGatheredModules()
	{
		return gathered_modules;
	}

	public List<ZenithModule> getEnabledModules()
	{
		return enabled_modules;
	}

	public List<ZenithModule> getDisabledModules()
	{
		return disabled_modules;
	}

	public boolean isModulePresent(String name)
	{
		for (ZenithModule mod : enabled_modules)
		{
			if (mod.getName().equals(name))
				return true;
		}

		return false;
	}

	public boolean isModuleListPresent(String... names)
	{
		for (String name : names)
		{
			if (!isModulePresent(name))
				return false;
		}

		return true;
	}

	public ZenithModule getEnabledModuleByName(String name)
	{
		for (ZenithModule mod : enabled_modules)
		{
			if (mod.getName().equals(name))
				return mod;
		}

		return null;
	}

	public ZenithModule getGatheredModuleByName(String name)
	{
		return gathered_modules.containsKey(name) ? gathered_modules.get(name) : null;
	}

	public ZenithModule getDisabledModuleByName(String name)
	{
		for (ZenithModule mod : disabled_modules)
		{
			if (mod.getName().equals(name))
				return mod;
		}

		return null;
	}

	/**
	 * Gathers all valid modules from .jar files inside ./plugins/Zenith/mods
	 */
	public void gatherAllModules()
	{		
		File mods_folder = FileUtils.ensureFolder("./plugins/Zenith/mods");
		
		// Copy internal modules
		List<File> internalMods = getInternalModules();

		for (File f : internalMods)
		{
			File modFile = new File("./plugins/Zenith/mods/" + f.getName());

			if (!modFile.exists())
				FileUtils.copy(f, modFile);
		}

		// Clear temp folder
		File temp = new File("./plugins/Zenith/mods/temp/");

		for (File f : temp.listFiles())
			f.delete();

		temp.delete();

		// Gather modules
		ZenithModule module = null;

		PrintUtils.logVerbose("Beginning initialization...", InfoType.INFO);
		PrintUtils.logVerbose("-----[ Gathering modules ]-----", InfoType.INFO);

		for (File f : mods_folder.listFiles())
		{
			if (f.getName().endsWith(".jar"))
			{
				try {
					module = gatherModulesFromFile(f);
					PrintUtils.logVerbose("Gathered module \"" + module.getName() + "\"", InfoType.INFO);
					gathered_modules.put(module.getName(), module);
				} catch (Exception e) {
					PrintUtils.log("Failed to gather module \"" + f.getName() + "\"!", InfoType.ERROR);
					e.printStackTrace();
				}
			}
		}

		PrintUtils.logVerbose("Finished gathering!", InfoType.INFO);
	}

	public void enableAllModules()
	{
		PrintUtils.logVerbose("-----[ Loading modules ]-----", InfoType.INFO);

		// Core loads first
		loadModule(gathered_modules.get("Zenith"));

		// Load other modules
		gathered_modules.forEach((name, mod) -> {
			loadModule(mod);
		});

		// Sort enabled modules alphabetically
		Collections.sort(enabled_modules, new Comparator<ZenithModule>() {
			@Override
			public int compare(ZenithModule mod1, ZenithModule mod2) 
			{
				return mod1.getName().compareTo(mod2.getName());
			}
		});

		PrintUtils.log("Enabled " + enabled_modules.size() + "/" + gathered_modules.size() + " module(s)!", InfoType.INFO);
	}

	public void loadModule(ZenithModule module)
	{
		PrintUtils.logVerbose("Checking if module \"" + module.getName() + "\" has already been loaded...", InfoType.INFO);
		if (enabled_modules.contains(module)) // Don't load twice
		{
			PrintUtils.logVerbose("It has! Skipping...", InfoType.INFO);
			return;
		}

		PrintUtils.logVerbose("It hasn't! Checking for a loadbefore hierarchy...", InfoType.INFO);
		// Dependency tree, recursion style
		if (module.getClass().isAnnotationPresent(LoadBefore.class))
		{
			LoadBefore lb = module.getClass().getAnnotation(LoadBefore.class);
			PrintUtils.logVerbose("Loadbefore found with " + lb.loadBefore().length + " entries!", InfoType.INFO);

			for (String target : lb.loadBefore())
			{
				if (!gathered_modules.containsKey(target))
				{
					PrintUtils.log("Module \"" + module.getName() + "\" requires dependency: \"" + target + "\", which is missing!", InfoType.ERROR);
					continue;
				}

				loadModule(gathered_modules.get(target));
			}

			// Make sure all loadbefores are enabled
			PrintUtils.logVerbose("Double-checking to make sure that all of this module's dependencies are enabled...", InfoType.INFO);
			if (!isModuleListPresent(lb.loadBefore()))
			{
				// Print missing dependencies
				PrintUtils.log("Failed to load module \"" + module.getName() + ". Missing dependencies:", InfoType.ERROR);
				for (String target : lb.loadBefore())
				{
					if (!isModulePresent(target))
						PrintUtils.log("- " + target, InfoType.ERROR);
				}

				// And abort
				if (!disabled_modules.contains(module))
					disabled_modules.add(module);
				return;
			}

			PrintUtils.logVerbose("They are!", InfoType.INFO);
		} else {
			PrintUtils.logVerbose("No loadbefore, continuing...", InfoType.INFO);
		}

		PrintUtils.logVerbose("Registering event(s)...", InfoType.INFO);
		// Register
		for (Listener l : module.getListeners())
			Zenith.getInstance().getServer().getPluginManager().registerEvents(l, Zenith.getInstance());

		PrintUtils.logVerbose("Done! Registering command(s)...", InfoType.INFO);

		for (ZenithCommand c : module.getCommands())
			CommandManager.registerCommand(c.getName(), c);

		switch (Zenith.getNMSVersion())
		{
			case "v1_16_R3":
				((org.bukkit.craftbukkit.v1_16_R3.CraftServer) Bukkit.getServer()).syncCommands();
				break;
			case "v1_17_R1":
				((org.bukkit.craftbukkit.v1_17_R1.CraftServer) Bukkit.getServer()).syncCommands();
				break;
		}
		

		if (module.getClass().isAnnotationPresent(ZenConfig.class))
		{
			PrintUtils.logVerbose("Found module config", InfoType.INFO);
			module.loadConfig();
		}

		PrintUtils.logVerbose("All registered! Enabling " + module.getName() + " v" + module.getVersion() + "...", InfoType.INFO);

		// Enable
		try {
			module.onEnable();
		} catch (Exception e) {
			PrintUtils.log("Failed to enable module! Stacktrace is below...", InfoType.ERROR);

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);

			e.printStackTrace(pw);
			PrintUtils.log(sw.toString(), InfoType.ERROR);

			if (!disabled_modules.contains(module))
				disabled_modules.add(module);
			return;
		}
		
		module.setEnabled();

		disabled_modules.remove(module);
		enabled_modules.add(module);
	}

	public void disableModule(String name, CallerToken caller)
	{
		ZenithModule module = getEnabledModuleByName(name);

		if (module == null)
			return; // Can't disable a module that doesn't exist

		// Check for a required caller token
		if (module.getClass().isAnnotationPresent(PreventUnload.class))
		{
			CallerToken intendedCaller = module.getClass().getAnnotation(PreventUnload.class).value();

			if (!CallerToken.isEqualToOrHigher(caller, intendedCaller))
			{
				PrintUtils.log("An attempt was made to disable \"" + name + "\" but this module's @PreventDisable tag prevented it! Expected token: " + intendedCaller.name() + ", given: " + caller.name(), InfoType.WARN);
				return;
			}	
		}

		// Start disabling
		PrintUtils.log("Disabling " + module.getName(), InfoType.INFO);

		if (module.getClass().isAnnotationPresent(ZenConfig.class))
		{
			try {
				PrintUtils.logVerbose("Saving config...", InfoType.INFO);
				module.saveConfig();
			} catch (Exception e) {
				PrintUtils.log("Failed to save config! Stacktrace is below...", InfoType.ERROR);
				e.printStackTrace();
			}
		}

		try {
			module.onDisable();

			if (!Zenith.isBeingDisabled())
			{
				// Disable events
				module.getListeners().forEach(event -> {
					if (event.getClass().isAnnotationPresent(PreventUnload.class))
						if (!CallerToken.isEqualToOrHigher(caller, event.getClass().getAnnotation(PreventUnload.class).value()))
							return;
							
					HandlerList.unregisterAll(event);
				});
				
				// Disable commands
				module.getCommands().forEach(command -> {
					if (command.getClass().isAnnotationPresent(PreventUnload.class))
						if (!CallerToken.isEqualToOrHigher(caller, command.getClass().getAnnotation(PreventUnload.class).value()))
							return;

					CommandManager.unregisterCommand(command.getName());
				});

				enabled_modules.remove(module);
				disabled_modules.add(module);
			}
		} catch (Exception e) {
			PrintUtils.log("Failed to disable module! Stacktrace is below...", InfoType.ERROR);
			e.printStackTrace();
		}
	}

	public void disableModule(String name)
	{
		disableModule(name, CallerToken.PLAYER);
	}
}