package fish.yukiemeralis.eden.module.java;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.command.CommandManager;
import fish.yukiemeralis.eden.command.EdenCommand;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.EdenModule.LoadBefore;
import fish.yukiemeralis.eden.module.EdenModule.ModInfo;
import fish.yukiemeralis.eden.module.ModuleFamilyRegistry;
import fish.yukiemeralis.eden.module.annotation.EdenConfig;
import fish.yukiemeralis.eden.module.annotation.PreventUnload;
import fish.yukiemeralis.eden.module.event.ModuleDisableEvent;
import fish.yukiemeralis.eden.module.event.ModuleEnableEvent;
import fish.yukiemeralis.eden.module.event.ModuleLoadEvent;
import fish.yukiemeralis.eden.module.event.ModuleUnloadEvent;
import fish.yukiemeralis.eden.module.java.enums.CallerToken;
import fish.yukiemeralis.eden.module.java.enums.ModuleDisableFailure;
import fish.yukiemeralis.eden.utils.DataUtils;
import fish.yukiemeralis.eden.utils.FileUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.exception.VersionNotHandledException;
import fish.yukiemeralis.eden.utils.logging.Logger.InfoType;
import fish.yukiemeralis.eden.utils.option.Option;
import fish.yukiemeralis.eden.utils.option.OptionState;
import fish.yukiemeralis.eden.utils.result.Result;

/**
 * Handler for all tasks related to Eden's modules.</p>
 * 
 * This is divided up into a few services:</p>
 * - Copying internal modules from Eden's plugin file</p>
 * - Gathering modules from a file, and providing access to their classes post-runtime</p>
 * - Enabling and disabling modules, and registering/unregistering their commands and listeners</p>
 * - Obtaining modules of different statuses in different areas</p>
 */
public class ModuleManager 
{
    private final Map<String, Class<?>> class_cache = new HashMap<>();
	private final Map<String, ModuleClassLoader> loader_cache = new LinkedHashMap<>();

	private final Map<String, String> module_references = new HashMap<>();
	private final Map<String, Long> load_times = new HashMap<>();

	private List<EdenModule> enabled_modules = new ArrayList<>();
	private List<EdenModule> disabled_modules = new ArrayList<>();

	private final static File mods_folder = FileUtils.ensureFolder("./plugins/Eden/mods");

	// ##########################################################
	// #################### Module gathering ####################
	// ##########################################################

	/**
	 * Performs the full startup process.
	 */
	public void performFullLoad()
	{
		PrintUtils.logVerbose("-----[ Preload ]-----", InfoType.INFO);

		PrintUtils.logVerbose("Copying internal modules...", InfoType.INFO);
		copyInternalModules();
		
		PrintUtils.logVerbose("Loading modules and their classes into memory...", InfoType.INFO);
		loadAllModules();

		// Handle missing dependencies
		Iterator<Entry<String, ModuleClassLoader>> loaderEntries = loader_cache.entrySet().iterator();
		while (loaderEntries.hasNext())
		{
			Entry<String, ModuleClassLoader> entry = loaderEntries.next();
			ModuleClassLoader loader = entry.getValue();
			if (!loader.getModuleClass().isAnnotationPresent(LoadBefore.class))
				continue;

			String modname = loader.getModuleClass().getAnnotation(ModInfo.class).modName();
			boolean valid = true;

			for (String str : loader.getModuleClass().getAnnotation(LoadBefore.class).loadBefore())
			{
				if (!loader_cache.containsKey(str))
				{
					PrintUtils.log("<Missing dependency \">[" + str + "]<\" for module \">{" + modname + "}<\"!>", InfoType.ERROR);
					valid = false;
					continue;
				}
			}

			if (!valid)
			{
				PrintUtils.logVerbose("Not loading module " + modname + ", as it is missing dependencies.", InfoType.ERROR);
				loaderEntries.remove();
			}
		}

		PrintUtils.logVerbose("Instantiating modules and registering their commands and listeners to their modules...", InfoType.INFO);

		// Finalize by instantiating the modules, commands, and listeners, and registering them to their modules
		for (ModuleClassLoader loader : loader_cache.values())
		{
			try {
				loader.finalizeLoading();
				disabled_modules.add(loader.getModule());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | IOException | NullPointerException e) {
				if (PrintUtils.isVerboseLoggingEnabled() || Eden.getEdenConfig().get("displayModErrorsWithoutVerbose").toLowerCase().equals("true"))
				{
					PrintUtils.log("<Failed to load \"" + "\">");
					PrintUtils.printPrettyStacktrace(e);
					continue;
				}
					
				PrintUtils.log("<Failed to load \"" + loader.getModuleFileName() + "\". Enable verbose logging or set \"displayModErrorsWithoutVerbose\" to \"true\" inside Eden's configuration file for more details.>");
				continue;
			}
		}

		// Register all reliant modules
		for (EdenModule module : disabled_modules)
			if (module.getClass().isAnnotationPresent(LoadBefore.class))
				for (String modName : module.getClass().getAnnotation(LoadBefore.class).loadBefore())
				{
					getDisabledModuleByName(modName).addReliantModule(module);
				}
		PrintUtils.logVerbose("Finished preload!", InfoType.INFO);
	}

	private void copyInternalModules()
	{
		if (Eden.getEdenConfig().get("flyingSolo").equals("true"))
		{
			PrintUtils.log("{Eden is now flying solo. Base modules will not be automatically installed.}");
			return;
		}
		
		List<File> internalMods = getInternalModules();

		for (File f : internalMods)
		{
			File modFile = new File("./plugins/Eden/mods/" + f.getName());

			if (!modFile.exists())
			{
				PrintUtils.logVerbose("Missing internal module \"" + modFile.getName() + "\". Copying...", InfoType.WARN);
				FileUtils.copy(f, modFile);
			}
		}

		// Clear temp folder
		File temp = new File("./plugins/Eden/mods/temp/");

		if (temp.listFiles() == null) // Nothing was copied
		{
			temp.delete();
			return;
		}

		for (File f : temp.listFiles())
			f.delete();

		temp.delete();
	}

	private void loadAllModules()
	{
		// Obtain all module classes
		for (File f : mods_folder.listFiles())
		{
			if (!f.getName().endsWith(".jar"))
				continue;

			PrintUtils.logVerbose("Attempting to obtain module from \"" + f.getAbsolutePath() + "\"", InfoType.INFO);
			loadModule(f);
		}

		// Cache all classes in order of dependency
		for (ModuleClassLoader mcl : loader_cache.values())
		{
			precacheModuleClasses(mcl, false);
		}
	}

	private ModuleClassLoader loadModule(File f)
	{
		ModuleClassLoader mcl;

		try {
			mcl = new ModuleClassLoader(this.getClass().getClassLoader(), this, f);
		} catch (MalformedURLException e) {
			PrintUtils.printPrettyStacktrace(e);
			return null;
		}

		if (mcl.getModuleClass() == null)
		{
			PrintUtils.log("<Failed to load file \">[" + f.getName() + "]<\"! Reason: invalid module class.>", InfoType.ERROR);
			return null;
		}

		String modName = mcl.getModuleClass().getAnnotation(ModInfo.class).modName();
		
		module_references.put(modName, f.getAbsolutePath());
		loader_cache.put(modName, mcl);

		return mcl;
	}

	private List<ModuleClassLoader> preloadedModules = new ArrayList<>();
	private List<ModuleClassLoader> preloadedRecursiveList = new ArrayList<>();
	private void precacheModuleClasses(ModuleClassLoader mcl, boolean recursive)
	{
		if (preloadedModules.contains(mcl))
		{
			return;
		}

		PrintUtils.logVerbose("Checking if " + mcl.getName() + " has been precached...", InfoType.INFO);

		if (preloadedRecursiveList.contains(mcl) && recursive)
		{
			PrintUtils.log("<Cyclical dependency from module file " + preloadedRecursiveList.get(0).getName() + " detected! Skipping...>", InfoType.ERROR);
			PrintUtils.logVerbose("This usually happens when two or more modules depend on each other, either directly (A -> B -> A) or indirectly (A -> B -> C... -> A).", InfoType.ERROR);
			PrintUtils.logVerbose("If you are a developer seeing this message, please add a FIXME if you intend on releasing this module.", InfoType.ERROR);
			PrintUtils.logVerbose("Related modules:", InfoType.ERROR);
			for (ModuleClassLoader classLoader : preloadedRecursiveList)
				PrintUtils.logVerbose("- " + classLoader.getModuleFileName(), InfoType.ERROR);
			return;
		}
		
		preloadedRecursiveList.add(mcl);
	
		if (mcl.getModuleClass().isAnnotationPresent(LoadBefore.class))
		{
			// Go over loadbefores with recursion
			for (String lb : mcl.getModuleClass().getAnnotation(LoadBefore.class).loadBefore())
			{
				if (!loader_cache.containsKey(lb))
				{
					PrintUtils.logVerbose("Missing dependency \"" + lb + "\". Will complain about it later.", InfoType.ERROR);
					continue;
				}

				ModuleClassLoader recursiveMcl = loader_cache.get(lb);

				precacheModuleClasses(recursiveMcl, true);
			}
		}

		mcl.cacheCommandsAndEvents();

		preloadedRecursiveList.remove(mcl);
		preloadedModules.add(mcl);

		PrintUtils.logVerbose("Precached " + mcl.getName() + "!", InfoType.INFO);
	}

	/**
	 * Copies all internal (core) modules from inside the jar
	 * @return
	 */
	private static List<File> getInternalModules()
	{
		File edenFile = DataUtils.getEdenJar();
		List<File> moduleFiles = new ArrayList<>();

		File modsFolder = new File("./plugins/Eden/mods/");

		try {
			JarFile jarFile = new JarFile(edenFile);
			Enumeration<JarEntry> iter = jarFile.entries();

			JarEntry jarEntry = null;
			List<JarEntry> moduleEntries = new ArrayList<>();

			while (iter.hasMoreElements())
			{
				jarEntry = iter.nextElement();

				if (!jarEntry.getName().startsWith("internalModules") || jarEntry.isDirectory() || !jarEntry.getName().endsWith(".jar"))
					continue;

				if (new File(modsFolder.getAbsolutePath() + "/" + jarEntry.getName().substring(16)).exists())
					continue;

				moduleEntries.add(jarEntry);
			}

			// Create some files from the collected entries
			moduleEntries.forEach(entry -> {
				try {
					File file = FileUtils.inputStreamToFile(jarFile.getInputStream(entry), entry.getName().replace("internalModules/", ""));

					moduleFiles.add(file);
				} catch (IOException e) {
					PrintUtils.printPrettyStacktrace(e);
				}
			});

			jarFile.close();
		} catch (IOException e) {
			PrintUtils.printPrettyStacktrace(e);
		}
		
		return moduleFiles;
	}

	/**
	 * Loads a single module from a filepath.
	 * @param filepath The filepath leading to the module file.
	 * @param syncEvent Whether or not to synchronize the resulting {@link ModuleLoadEvent}. <b>This is required when attempting to asynchronously load modules.</b>
	 * @return A result of either an EdenModule or a String describing the error.
	 */
	public Result loadSingleModule(String filepath, boolean syncEvent)
	{
		if (!new File(filepath).exists())
			return Result.err("File does not exist.");

		ModuleClassLoader mcl = loadModule(new File(filepath));

		if (mcl == null)
			return Result.err("File could not be opened.");

		precacheModuleClasses(mcl, false);

		if (mcl.getModuleClass().isAnnotationPresent(LoadBefore.class))
			if (!isModuleListPresent(mcl.getModuleClass().getAnnotation(LoadBefore.class).loadBefore()))
				return Result.err("Module dependencies are missing.");

		try {
			mcl.finalizeLoading();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | IOException e) {
			PrintUtils.printPrettyStacktrace(e);
			return Result.err("Module has invalid classes. Failed with error: " + e.getClass().getSimpleName());
		}

		disabled_modules.add(mcl.getModule());

		// Handle reliance
		if (mcl.getModuleClass().isAnnotationPresent(LoadBefore.class))
		{
			for (String modName : mcl.getModuleClass().getAnnotation(LoadBefore.class).loadBefore())
			{
				loader_cache.get(modName).getModule().addReliantModule(mcl.getModule());
			}
		}

		Eden.callEvent(new ModuleLoadEvent(mcl.getModule(), mcl.getName()), syncEvent);
		Eden.callEvent(new ModuleDisableEvent(mcl.getModule(), filepath), syncEvent);

		return Result.ok(mcl.getModule());
	}

	/**
	 * Loads a single module from a filepath without synchronizing the resulting {@link ModuleLoadEvent}.
	 * @param filepath The filepath leading to the module file.
	 * @return A result of either an EdenModule or a String describing the error.
	 */
	public Result loadSingleModule(String filepath)
	{
		return loadSingleModule(filepath, false);
	}
	// ########################################################################
	// #################### Enabling, disabling, unloading ####################
	// ########################################################################

	/**
	 * Attempts to enable all gathered modules.
	 */
	public void enableAllModules()
	{
		PrintUtils.logVerbose("-----[ Enabling modules ]-----", InfoType.INFO);

		// Core loads first, as long as we aren't flying solo
		if (!Eden.getEdenConfig().get("flyingSolo").equals("true"))
		{
			EdenModule rosettaModule;
			if ((rosettaModule = getDisabledModuleByName("Rosetta")) != null)
			{
				enableModule(rosettaModule);
			} else {
				PrintUtils.log("<Failed to load Rosetta! Eden will now fly solo.>");
				Eden.getEdenConfig().put("flyingSolo", "true");
			}
		}

		// Load other modules
		new ArrayList<>(disabled_modules).forEach(this::enableModule); // Construct a new list to avoid a ConcurrentModificationException
		disabled_modules.removeAll(enabled_modules);

		// Sort enabled modules alphabetically
		Collections.sort(enabled_modules, new Comparator<EdenModule>() {
			@Override
			public int compare(EdenModule mod1, EdenModule mod2) 
			{
				return mod1.getName().compareTo(mod2.getName());
			}
		});

		PrintUtils.log("Enabled [" + enabled_modules.size() + "]/[" + getAllModules().size() + "] " + PrintUtils.plural(getAllModules().size(), "module", "modules") + "!", InfoType.INFO);
	}

	/**
	 * Loads a module, calling it's onEnable() and registering its commands and listeners, and does not attempt to synchronize the resulting {@link ModuleEnableEvent}.
	 * @param module The module to load.
	 */
	public void enableModule(EdenModule module)
	{
		enableModule(module, false);
	}
 
	/**
	 * Loads a module, calling it's onEnable() and registering its commands and listeners.
	 * @param module The module to load.
	 * @param syncEvent Whether or not to synchronize the resulting {@link ModuleEnableEvent}. <b>This is required when attempting to asynchronously enable modules.</b>
	 */
	public void enableModule(EdenModule module, boolean syncEvent)
	{
		if (module == null)
		{
			// Can't load a module that doesn't exist
			PrintUtils.log("<Attempted to load a null module! Aborting...>");
			return;
		}

		if (module.getName() == null)
		{
			PrintUtils.log("<Module failed to load. Keeping as an unloaded module.>", InfoType.ERROR);
			return;
		}

		PrintUtils.logVerbose("Checking if module \"" + module.getName() + "\" has already been loaded...", InfoType.INFO);
		if (enabled_modules.contains(module)) // Don't load twice
		{
			PrintUtils.logVerbose("It has! Skipping...", InfoType.INFO);
			return;
		}

		PrintUtils.logVerbose("Registering event(s)...", InfoType.INFO);
		// Register
		for (Listener l : module.getListeners())
			Eden.getInstance().getServer().getPluginManager().registerEvents(l, Eden.getInstance());

		PrintUtils.logVerbose("Done! Registering command(s)...", InfoType.INFO);

		for (EdenCommand c : module.getCommands())
			CommandManager.registerCommand(c.getName(), c);

		switch (Eden.getNMSVersion())
		{
			case "v1_16_R3":
				((org.bukkit.craftbukkit.v1_16_R3.CraftServer) Bukkit.getServer()).syncCommands();
				break;
			case "v1_17_R1":
				((org.bukkit.craftbukkit.v1_17_R1.CraftServer) Bukkit.getServer()).syncCommands();
				break;
			case "v1_18_R1":
				((org.bukkit.craftbukkit.v1_18_R1.CraftServer) Bukkit.getServer()).syncCommands();
				break;
			case "v1_18_R2":
				((org.bukkit.craftbukkit.v1_18_R2.CraftServer) Bukkit.getServer()).syncCommands();
				break;
			case "v1_19_R1":
				((org.bukkit.craftbukkit.v1_19_R1.CraftServer) Bukkit.getServer()).syncCommands();
				break;
			default:
				throw new VersionNotHandledException();
		}

		if (module.getClass().isAnnotationPresent(EdenConfig.class))
		{
			PrintUtils.logVerbose("Found module config", InfoType.INFO);
			if (!module.loadConfig())
				return;
		}

		PrintUtils.logVerbose("All registered! Enabling " + module.getName() + " " + module.getVersion() + "...", InfoType.INFO);

		// Enable
		try {
			module.onEnable();
			module.setEnabled();
		} catch (Exception e) {
			PrintUtils.log("<Failed to enable module! Stacktrace is below...>", InfoType.ERROR);

			PrintUtils.printPrettyStacktrace(e);

			if (!disabled_modules.contains(module))
				disabled_modules.add(module);
			return;
		}

		disabled_modules.remove(module);
		enabled_modules.add(module);

		Eden.callEvent(new ModuleEnableEvent(module, this.module_references.get(module.getName())), syncEvent);
	}

	private static List<Class<? extends EdenModule>> dependentModuleTree = new ArrayList<>();
	/**
	 * Disables a module, unregistering its commands and listeners.
	 * @param name The expected name of a module.
	 * @param caller The token of the requestee.
	 * @param disabledModuleList A list to be populated with a number of downstream disabled modules.
	 * @param syncEvent Whether or not the {@link ModuleDisableEvent} should be triggered synchronously.<p>
	 * - If you are unsure, <code>false</code> is generally safe.
	 * @param force Whether or not to force downstream modules to be disabled as well.
	 * @return Whether or not disabling was successful.
	 */
	public Option disableModule(String name, CallerToken caller, List<EdenModule> disabledModuleList, boolean syncEvent, boolean force)
	{
		EdenModule module = getEnabledModuleByName(name);

		if (module == null)
			return Option.some(new ModuleDisableFailureData(disabledModuleList, ModuleDisableFailure.NULL_MODULE));; // Can't disable a module that doesn't exist

		// Check for a required caller token
		if (module.getClass().isAnnotationPresent(PreventUnload.class))
		{
			CallerToken intendedCaller = module.getClass().getAnnotation(PreventUnload.class).value();

			if (!CallerToken.isEqualToOrHigher(caller, intendedCaller))
			{
				PrintUtils.log("<An attempt was made to disable \"" + name + "\" but this module's @PreventDisable tag prevented it! Expected token: " + intendedCaller.name() + ", given: " + caller.name() + ">", InfoType.WARN);
				return Option.some(new ModuleDisableFailureData(disabledModuleList, ModuleDisableFailure.UNAUTHORIZED_CALLERTOKEN));
			}	
		}

		// Start disabling
		PrintUtils.log("Disabling [" + module.getName() + "]...", InfoType.INFO);

		List<EdenModule> disabledMods = new ArrayList<>();

		dependentModuleTree.add(module.getClass());

		// Disable reliant modules
		if (!force)
			for (EdenModule mod : module.getReliantModules())
			{
				PrintUtils.log("- Reliant module: [" + mod.getName() + "]", InfoType.INFO);

				if (dependentModuleTree.contains(mod.getClass()))
				{
					PrintUtils.logVerbose("Cyclical dependency containing " + dependentModuleTree.size() + " modules detected. Stopping recursion here.", InfoType.WARN);
					continue;
				}

				if (!disableModule(mod.getName(), caller, disabledModuleList, syncEvent, force).getState().equals(OptionState.NONE)) // If we can't disable a reliant module, reload all disabled modules and abort
				{
					PrintUtils.log("<Failed to unload module \"" + module.getName() + "\"'s dependencies. Aborting disable.>", InfoType.ERROR);	
					
					dependentModuleTree.remove(module.getClass());
					return Option.some(new ModuleDisableFailureData(disabledModuleList, ModuleDisableFailure.DOWNSTREAM_DISABLE_FAILURE));
				}

				if (!disabledModuleList.contains(mod))
					disabledModuleList.add(mod);

				mod.removeReliantModule(module);
				disabledMods.add(mod);
				mod.setDisabled();				
			}

		dependentModuleTree.remove(module.getClass());

		if (module.getClass().isAnnotationPresent(EdenConfig.class))
		{
			try {
				PrintUtils.logVerbose("Saving config...", InfoType.INFO);
				module.saveConfig();
			} catch (Exception e) {
				PrintUtils.log("<Failed to save config! Stacktrace is below...>", InfoType.ERROR);
				PrintUtils.printPrettyStacktrace(e);
			}
		}

		try {
			module.onDisable();

			if (!Eden.isBeingDisabled())
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

				if (!syncEvent)
				{
					Eden.callEvent(new ModuleDisableEvent(module, this.module_references.get(module.getName())));
					PrintUtils.log("Successfully disabled [" + module.getName() + "]!", InfoType.INFO);
					return Option.none();
				}

				Eden.callSyncEvent(new ModuleDisableEvent(module, this.module_references.get(module.getName())));
				PrintUtils.log("Successfully disabled [" + module.getName() + "]! (SYNCHRONIZED FOR NEXT TICK)");

				return Option.none();
			}
		} catch (Exception e) {
			PrintUtils.log("<Failed to disable module! Stacktrace is below...>", InfoType.ERROR);
			PrintUtils.printPrettyStacktrace(e);

			return Option.some(new ModuleDisableFailureData(disabledModuleList, ModuleDisableFailure.JAVA_ERROR));
		}

		return Option.some(new ModuleDisableFailureData(disabledModuleList, ModuleDisableFailure.UNKNOWN_ERROR));
	}

	/**
	 * Attempts to disable an enabled module. Runs with caller token PLAYER, and does not attempt to force a disable if a downstream module fails to be disabled.
	 * The resulting {@link ModuleDisableEvent} will be run asynchronously. See {@link ModuleManager#disableModuleSync(String name)} if synchronization is needed.
	 * @param name The expected name of a module.
	 * @return An option containing either NONE or a {@link ModuleDisableFailureData} object, providing details and tools on disable failure
	 */
	public Option disableModule(String name)
	{
		List<EdenModule> disabledModuleList = new ArrayList<>();
		return disableModule(name, CallerToken.PLAYER, disabledModuleList, false, false);
	}

	/**
	 * Attempts to disable an enabled module. Does not attempt to force a disable if a downstream module fails to be disabled.
	 * The resulting {@link ModuleDisableEvent} will be run asynchronously. See {@link ModuleManager#disableModuleSync(String name, CallerToken token)} if synchronization is needed.
	 * @param name The expected name of a module.
	 * @param token Privilege level of caller
	 * @return An option containing either NONE or a {@link ModuleDisableFailureData} object, providing details and tools on disable failure
	 */
	public Option disableModule(String name, CallerToken token)
	{
		List<EdenModule> disabledModuleList = new ArrayList<>();
		return disableModule(name, token, disabledModuleList, false, false);
	}
	
	/**
	 * Attempts to disable an enabled module. Runs with caller token PLAYER.
	 * The resulting {@link ModuleDisableEvent} will be run asynchronously. See {@link ModuleManager#disableModuleSync(String name, boolean force)} if synchronization is needed.
	 * @param name The expected name of a module.
	 * @param force Whether or not to continue with a disable operation if a downstream module fails to be correctly disabled 
	 * @return An option containing either NONE or a {@link ModuleDisableFailureData} object, providing details and tools on disable failure
	 */
	public Option disableModule(String name, boolean force)
	{
		List<EdenModule> disabledModuleList = new ArrayList<>();
		return disableModule(name, CallerToken.PLAYER, disabledModuleList, false, force);
	}

	/**
	 * Attempts to disable an enabled module. Runs with caller token PLAYER.
	 * The resulting {@link ModuleDisableEvent} will be run asynchronously. See {@link ModuleManager#disableModuleSync(String name, CallerToken token, boolean force)} if synchronization is needed.
	 * @param name The expected name of a module.
	 * @param token Privilege level of caller
	 * @param force Whether or not to continue with a disable operation if a downstream module fails to be correctly disabled 
	 * @return An option containing either NONE or a {@link ModuleDisableFailureData} object, providing details and tools on disable failure
	 */
	public Option disableModule(String name, CallerToken token, boolean force)
	{
		List<EdenModule> disabledModuleList = new ArrayList<>();
		return disableModule(name, token, disabledModuleList, false, force);
	}

	/**
	 * Attempts to disable an enabled module, running the resulting {@link ModuleDisableEvent} in sync with the server. Runs with caller token PLAYER, 
	 * and does not attempt to force a disable if a downstream module fails to be disabled.
	 * @param name The expected name of a module.
	 * @param token Privilege level of caller
	 * @param force Whether or not to continue with a disable operation if a downstream module fails to be correctly disabled 
	 * @return An option containing either NONE or a {@link ModuleDisableFailureData} object, providing details and tools on disable failure
	 */
	public Option disableModuleSync(String name)
	{
		List<EdenModule> disabledModuleList = new ArrayList<>();
		return disableModule(name, CallerToken.PLAYER, disabledModuleList, true, false);
	}

	/**
	 * Attempts to disable an enabled module, running the resulting {@link ModuleDisableEvent} in sync with the server. Does not attempt to force a disable 
	 * if a downstream module fails to be disabled.
	 * @param name The expected name of a module.
	 * @param token Privilege level of caller
	 * @return An option containing either NONE or a {@link ModuleDisableFailureData} object, providing details and tools on disable failure
	 */
	public Option disableModuleSync(String name, CallerToken token)
	{
		List<EdenModule> disabledModuleList = new ArrayList<>();
		return disableModule(name, token, disabledModuleList, true, false);
	}

	/**
	 * Attempts to disable an enabled module, running the resulting {@link ModuleDisableEvent} in sync with the server.
	 * @param name The expected name of a module.
	 * @param force Whether or not to continue with a disable operation if a downstream module fails to be correctly disabled 
	 * @return An option containing either NONE or a {@link ModuleDisableFailureData} object, providing details and tools on disable failure
	 */
	public Option disableModuleSync(String name, boolean force)
	{
		List<EdenModule> disabledModuleList = new ArrayList<>();
		return disableModule(name, CallerToken.PLAYER, disabledModuleList, true, force);
	}

	/**
	 * Attempts to disable an enabled module, running the resulting {@link ModuleDisableEvent} in sync with the server.
	 * @param name The expected name of a module.
	 * @param token Privilege level of caller
	 * @param force Whether or not to continue with a disable operation if a downstream module fails to be correctly disabled 
	 * @return An option containing either NONE or a {@link ModuleDisableFailureData} object, providing details and tools on disable failure
	 */
	public Option disableModuleSync(String name, CallerToken token, boolean force)
	{
		List<EdenModule> disabledModuleList = new ArrayList<>();
		return disableModule(name, token, disabledModuleList, true, force);
	}

	/**
	 * Removes an EdenModule from memory. Modules being unloaded are expected to already be disabled.
	 * @param name The name of the module to remove.
	 * @param caller The token for the caller.
	 * @see {@link ModuleManager#disableModule(String, CallerToken)}
	 */
	public void removeModuleFromMemory(String name, CallerToken caller)
	{
		EdenModule module = getDisabledModuleByName(name);

		if (module == null)
			return; // Can't unload a module that doesn't exist

		// Check for a required caller token
		if (module.getClass().isAnnotationPresent(PreventUnload.class))
		{
			CallerToken intendedCaller = module.getClass().getAnnotation(PreventUnload.class).value();

			if (!CallerToken.isEqualToOrHigher(caller, intendedCaller))
			{
				PrintUtils.log("<An attempt was made to disable \"" + name + "\" but this module's @PreventDisable tag prevented it! Expected token: " + intendedCaller.name() + ", given: " + caller.name() + ">", InfoType.WARN);
				return;
			}	
		}

		// Disable events
		module.getListeners().forEach(HandlerList::unregisterAll);
		
		// Disable commands
		module.getCommands().forEach(command -> {
			CommandManager.unregisterCommand(command.getName());
		});

		class_cache.clear();

		Eden.getInstance().getServer().getPluginManager().callEvent(new ModuleUnloadEvent(name, module, this.module_references.get(name)));

		ModuleFamilyRegistry.unregister(module);

		disabled_modules.remove(module);
		enabled_modules.remove(module);
		loader_cache.remove(name);
	}

	/**
	 * Forcibly unloads an EdenModule from memory. This method bypasses {@link CallerToken}s, &#64;{@link PreventUnload} annotations, and other safety checks.<p>
	 * <b><h2>This is not intended to be a bypass of the standard module removal protocol.</h2></b>
	 * As such, issues arising from improper usage of this utility will be ignored.
	 * @param name Module name to unload.
	 * @param syncEvent Whether or not to synchronize the resulting {@link ModuleUnloadEvent}
	 * @see {@link ModuleManager#removeModuleFromMemory(String name, CallerToken token)}
	 */
	public void forceRemoveModuleFromMemory(String name, boolean syncEvent)
	{
		EdenModule module = getDisabledModuleByName(name);

		if (module == null)
			return;

		// Disable events
		module.getListeners().forEach(HandlerList::unregisterAll);
		
		// Disable commands
		module.getCommands().forEach(command -> {
			CommandManager.unregisterCommand(command.getName());
		});

		class_cache.clear();

		Eden.callEvent(new ModuleUnloadEvent(name, module, this.module_references.get(name)), syncEvent);

		ModuleFamilyRegistry.unregister(module);

		disabled_modules.remove(module);
		enabled_modules.remove(module);
		loader_cache.remove(name);
	}

	/**
	 * Forcibly reloads a module.
	 * @param name Module name to reload.
	 * @param enableIfDisabled Whether or not to enable the reloaded module, even if it was previously disabled.
	 * @param syncEvent Whether or not to synchronize all the events generated by this method
	 * @param continueOnFailure Whether or not to continue on a failure, ensuring that the module will be reloaded. This may result in configs not being updated.
	 * @return Whether or not the reload was successful
	 */
	public boolean forceReload(String name, boolean enableIfDisabled, boolean syncEvent, boolean continueOnFailure)
	{
		EdenModule module = getModuleByName(name);
		
		if (module == null)
			return false;
		
		boolean enabled = module.getIsEnabled();

		// Disable, potentially continuing on failure
		if (enabled)
		{
			Option option;
			try {
				option = disableModule(name, CallerToken.EDEN, new ArrayList<>(), syncEvent, true);

				if (option.isSome())
					if (!continueOnFailure)
						return false;		

			} catch (Exception e) {
				PrintUtils.printPrettyStacktrace(e);
				if (!continueOnFailure)
					return false;
			}

			enabled_modules.remove(module);
			disabled_modules.add(module);

			//Eden.getInstance().getServer().getPluginManager().callEvent(new ModuleDisableEvent(module, this.module_references.get(module.getName())));
			module.setDisabled();

			PrintUtils.log("Successfully disabled [" + module.getName() + "]!", InfoType.INFO);
		}

		// Unload
		try {
			forceRemoveModuleFromMemory(name, syncEvent);
		} catch (Exception e) {
			PrintUtils.printPrettyStacktrace(e);
		}

		// Load
		Result result = loadSingleModule(module_references.get(name), syncEvent);

		if (result.isErr())
		{
			PrintUtils.log("Failed to load module \"" + name + "\" after reload! Reason: " + result.unwrapErr(String.class));
			return false;
		}

		if (!enabled && !enableIfDisabled)
			return true;

		enableModule(result.unwrapOk(EdenModule.class), syncEvent);
		result.unwrapOk(EdenModule.class).setEnabled();
		PrintUtils.log("Finished reload.");
		return true;
	}

	// #############################################################
	// #################### Getters and setters ####################
	// #############################################################
	
	/**
	 * Obtain a class instance from a loaded module class cache.
	 * @param name The fully-qualified classname to get
	 * @return The class under the given classname
	 */
	public Class<?> getCachedClass(String name)
	{
		PrintUtils.log("Checking if class " + name + " is cached");
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

	/**
	 * Finds the module a class is associated with. <p>
	 * <b>Results:</b><p>
	 * SOME - A module was found. Unwrap to obtain module. If null, class was not found anywhere.<p>
	 * NONE - Class was found outside of a module. 
	 * @param clazz The class to search with.
	 * @return Option containing a module on SOME, no module if NONE, or Some(null) if a class was not found anywhere
	 */
	public Option getHostModule(Class<?> clazz) 
	{
		try {
			Class.forName(clazz.getName(), false, this.getClass().getClassLoader()); // Part of Eden, bukkit/spigot, or NMS
			return Option.none(); // Option is none
		} catch (ClassNotFoundException e) {}

		for (String current : loader_cache.keySet())
		{
			ModuleClassLoader loader = loader_cache.get(current);

			try {
				loader.findClass(clazz.getName(), false);
				return Option.some(loader.getModule()); // Class found 
			} catch (ClassNotFoundException e) {
				continue;
			}
		}

		return Option.some(null); // Class was not found anywhere
	}

	/**
	 * Obtains a list of reliant (dependencies from the module being depended on's perspective) modules related to the given module. 
	 * @param target Module to obtain a list of reliant modules with
	 * @return A list of reliant modules that depend on the given module.
	 */
	public List<EdenModule> getReliantModules(EdenModule target)
	{
		List<EdenModule> reliant = new ArrayList<>();
		List<EdenModule> processed = new ArrayList<>();

		if (target.getReliantModules().size() == 0)
			return reliant;

		return getReliantModulesRecursive(target, reliant, processed);
	}

	private List<EdenModule> getReliantModulesRecursive(EdenModule target, List<EdenModule> host, List<EdenModule> processed)
	{
		if (processed.contains(target))
			return host;

		processed.add(target);

		if (target.getReliantModules().size() == 0)
			return host;

		for (EdenModule reliant : target.getReliantModules())
		{
			if (!host.contains(reliant))
				host.add(reliant);

			getReliantModulesRecursive(reliant, host, processed);
		}

		return host;
	}

	/**
	 * Assigns a class to a name set in the class cache.
	 * @param name The fully-qualified name of the given class.
	 * @param class_ The class to assign.
	 */
	public void setClass(String name, Class<?> class_)
	{
		if (!class_cache.containsKey(name))
			class_cache.put(name, class_);
	}

	/**
	 * Checks if this module manager holds a reference (module name + module filepath) for a module.
	 * @param name The expected name of a module reference.
	 * @return Whether or not this module manager holds a reference to a module.
	 */
	public boolean hasReferenceTo(String name)
	{
		return module_references.containsKey(name);
	}

	/**
	 * Obtains a map of all module references.
	 * @return A map of all module references.
	 */
	public Map<String, String> getReferences()
	{
		return module_references;
	}

	/**
	 * Obtains a list of all modules.
	 * @return A map of all modules.
	 */
	public List<EdenModule> getAllModules()
	{
		return DataUtils.concatMultipleCollections(enabled_modules, disabled_modules);
	}

	/**
	 * Obtains a list of all enabled modules.
	 * @return A list of enabled modules.
	 */
	public List<EdenModule> getEnabledModules()
	{
		return enabled_modules;
	}

	/**
	 * Obtains a list of disabled modules.
	 * @return A list of disabled modules.
	 */
	public List<EdenModule> getDisabledModules()
	{
		return disabled_modules;
	}

	/**
	 * Checks if a module with a given name is enabled.
	 * @param name The expected module name.
	 * @return Whether or not a module with a given name is enabled or not. Returns false if the module cannot be found.
	 */
	public boolean isModulePresent(String name)
	{
		for (EdenModule mod : getAllModules())
		{
			if (mod.getName().equals(name))
				return true;
		}

		return false;
	}

	/**
	 * Checks if an array of modules, by name, are all present. Returns false if one or more modules are missing.
	 * @param names An array of expected module names.
	 * @return Whether or not a set of modules are enabled.
	 * @see {@link ModuleManager#isModulePresent(String)}
	 */
	public boolean isModuleListPresent(String... names)
	{
		for (String name : names)
		{
			if (!isModulePresent(name))
				return false;
		}

		return true;
	}

	/**
	 * Obtains an enabled module whose name matches the given name.
	 * @param name The name of a module.
	 * @return A module matching the given name. Returns null if no module matches the given name.
	 * @see {@link ModuleManager#getEnabledModules()}
	 */
	public EdenModule getEnabledModuleByName(String name)
	{
		for (EdenModule mod : enabled_modules)
		{
			if (mod.getName().equals(name))
				return mod;
		}

		return null;
	}

	/**
	 * Obtains a disabled module whose name matches the given name.
	 * @param name The name of a module.
	 * @return A module matching the given name. Returns null if no module matches the given name.
	 * @see {@link ModuleManager#getDisabledModules()}
	 */
	public EdenModule getDisabledModuleByName(String name)
	{
		for (EdenModule mod : disabled_modules)
		{
			if (mod.getName().equals(name))
				return mod;
		}

		return null;
	}

	/**
	 * Obtains a module by its name.
	 * @param name The name of a module.
	 * @return A module matching the given name. Returns null if no module matches the given name.
	 */
	public EdenModule getModuleByName(String name)
	{
		EdenModule mod = getEnabledModuleByName(name);

		if (mod != null)
			return mod;

		mod = getDisabledModuleByName(name);

		if (mod != null)
			return mod;
		return null;
	}

	/**
	 * Validates if a list of NMS versions contains the current running server version.
	 * @param versions Variable-args array of NMS versions (I.E "v1_16_R3", "v1_19_R1", etc.)
	 * @return Whether or not a list of versions contains the current running server version
	 */
	public boolean isCompatible(String... versions)
	{
		for (String v : versions)
			if (v.equals(Eden.getNMSVersion()))
				return true;
		return false;
	}

	/**
	 * Validates if a list of NMS versions contains the current running server version.
	 * @param versions List of NMS versions (I.E "v1_16_R3", "v1_19_R1", etc.)
	 * @return Whether or not a list of versions contains the current running server version
	 */
	public boolean isCompatible(List<String> versions)
	{
		return versions.contains(Eden.getNMSVersion());
	}

	/**
	 * Registers the time it took to load something.
	 * @param name
	 * @param time
	 */
	void registerLoadTime(String name, long time)
	{
		load_times.put(name, time);
	}

	/**
	 * Obtains the previous load time for a given target
	 * @param name
	 * @return
	 */
	public long getLastLoadTime(String name)
	{
		if (!load_times.containsKey(name))
			return 0;
		return load_times.get(name);
	}

	/**
	 * Obtains the previous load time for a given module
	 * @param mod
	 * @return
	 */
	public long getLastLoadTime(EdenModule mod)
	{
		return getLastLoadTime(mod.getName());
	}
}