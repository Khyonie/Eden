package com.yukiemeralis.blogspot.eden.module.java;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.event.Listener;

import com.yukiemeralis.blogspot.eden.Eden;
import com.yukiemeralis.blogspot.eden.command.EdenCommand;
import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.module.EdenModule.ModInfo;
import com.yukiemeralis.blogspot.eden.module.java.annotations.HideFromCollector;
import com.yukiemeralis.blogspot.eden.module.java.annotations.Unimplemented;
import com.yukiemeralis.blogspot.eden.utils.DataUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils.InfoType;

/**
 * Special classloader that loads an Eden module from a file.
 * @author Yuki_emeralis
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public class ModuleClassLoader extends URLClassLoader
{
    private File file;
	private Class<? extends EdenModule> moduleClass;
	private EdenModule module;
    private ModuleManager loader;
	
	private final Map<String, Class<?>> interior_classes = new HashMap<>();
	private final Map<String, Class<?>> unimplemented_classes = new HashMap<>();

	private final List<String> dependency_entries = new ArrayList<>();

	private JarFile jar = null;

	// Loading buffers
	private final List<Class<? extends EdenCommand>> commandClasses = new ArrayList<>();
	private final List<Class<? extends Listener>> listenerClasses = new ArrayList<>();
	
	/**
	 * Classloader to load modules from a .jar file. Modules must contain a class that extends {@link EdenModule} with the {@link ModInfo} class annotation.
	 * @param parent Parent classloader
	 * @param loader The module manager for the current Eden context
	 * @param file File to load from
	 * @throws MalformedURLException When the filepath is malformed.
	 * @throws NullPointerException When the filepath is incorrect.
	 */
	public ModuleClassLoader(ClassLoader parent, ModuleManager loader, File file) throws MalformedURLException, NullPointerException
	{
		super(file.getName(), new URL[] { file.toURI().toURL() }, parent);
		
		this.loader = loader;
		this.file = file;

		try {
			jar = new JarFile(file);
		} catch (IOException e) {
			throw new NullPointerException("Failed to access module file.");
		}
	}

	@SuppressWarnings("unchecked")
	private Class<? extends EdenModule> locateModuleClass()
	{
		Class<? extends EdenModule> clazz = null;

		Enumeration<JarEntry> entries = jar.entries();
		JarEntry entry;
		while (entries.hasMoreElements())
		{
			entry = entries.nextElement();

			if (!entry.getName().endsWith(".class") || entry.isDirectory())
				continue;

			try {
				Class<?> buffer = Class.forName(entry.getName().replace("/", ".").replace(".class", ""), false, this);
				
				if (!EdenModule.class.isAssignableFrom(buffer))
					continue;

				if (!buffer.isAnnotationPresent(ModInfo.class))
				{
					PrintUtils.log("(Module class \")[" + clazz.getPackageName() + "](\" does not specify any module information!)", InfoType.ERROR);
					PrintUtils.log("(If you are a developer seeing this message, please attach an @ModInfo annotation to your module class.)", InfoType.ERROR);
					PrintUtils.log("(If you are a server owner seeing this message, please either update this module, update Eden, or contact this module's maintainer.)", InfoType.ERROR);
					PrintUtils.log("(Offending file:) [" + file.getName() + "]", InfoType.ERROR);
					return null;
				}

				if (!Arrays.asList(buffer.getAnnotation(ModInfo.class).supportedApiVersions()).contains(Eden.getNMSVersion()))
				{
					PrintUtils.log("(Module \")[" + buffer.getAnnotation(ModInfo.class).modName() + "](\" is declared as compatible with the following version(s):) {" + Arrays.toString(buffer.getAnnotation(ModInfo.class).supportedApiVersions()) + "}(,)", InfoType.ERROR);
					PrintUtils.log("(however this server is running on version )[" + Eden.getNMSVersion() + "](.)", InfoType.ERROR);
					PrintUtils.log("(Please upgrade or downgrade this module, or contact this module's maintainer.)", InfoType.ERROR);
					return null;
				}

				clazz = (Class<? extends EdenModule>) buffer;
			} catch (ClassNotFoundException | NoClassDefFoundError e) { continue; }
		}

		if (clazz != null) 
		{
			this.moduleClass = clazz;
			interior_classes.put(clazz.getPackageName(), clazz);
		}

		return clazz;
	}

	void cacheCommandsAndEvents()
	{
		commandClasses.addAll(findSubclasses(true, EdenCommand.class));
		commandClasses.forEach(clazz -> { PrintUtils.logVerbose("Found command class \"" + clazz.getName() + "\"", InfoType.INFO); });
		listenerClasses.addAll(findSubclasses(true, Listener.class));
		listenerClasses.forEach(clazz -> { PrintUtils.logVerbose("Found listener class \"" + clazz.getName() + "\"", InfoType.INFO); });
	}

	void finalizeLoading() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException
	{
		// Put it all together
		module = moduleClass.getConstructor().newInstance();
		module.setInfo(moduleClass.getAnnotation(ModInfo.class));
		
		// Register commands and listeners
		List<EdenCommand> commands = new ArrayList<>();
		List<Listener> listeners = new ArrayList<>();

		for (Class<? extends EdenCommand> clazz : commandClasses)
		{
			Constructor<? extends EdenCommand> commandConstructor;
			EdenCommand command;

			try {
				commandConstructor = clazz.getConstructor();
				command = commandConstructor.newInstance();
			} catch (NoSuchMethodException e) {
				try {
					commandConstructor = clazz.getConstructor(EdenModule.class);
					command = commandConstructor.newInstance(module);
				} catch (NoSuchMethodException e_) {
					PrintUtils.log("(Command class \")[" + clazz.getPackageName() + "](\" does not contain a valid constructor!)", InfoType.ERROR);
					PrintUtils.log("(If you are a developer seeing this message, ensure your class contains an EdenModule.)", InfoType.ERROR);
					PrintUtils.log("(Alternatively, you may hide this command from automatic loading by annotating your class with an @HideFromCollector and adding it manually.)", InfoType.ERROR);
					PrintUtils.log("(If you are a server owner seeing this message, please update this module \")[" + module.getName() + "](\", update Eden, or contact this module's maintainer.)", InfoType.ERROR);
					continue;
				}
			}

			commands.add(command);
		}

		for (Class<? extends Listener> clazz : listenerClasses)
		{
			try {
				Listener listener = clazz.getConstructor().newInstance();
				listeners.add(listener);
			} catch (NoSuchMethodException e) {
				continue;
			}
		}

		module.addCommand(commands.toArray(new EdenCommand[commands.size()]));
		module.addListener(listeners.toArray(new Listener[listeners.size()]));

		PrintUtils.logVerbose("Registered " + commands.size() + " " + PrintUtils.plural(commands.size(), "command", "commands") + " and " + listeners.size() + " " + PrintUtils.plural(listeners.size(), "event", "events") + " to " + module.getName() + ".", InfoType.INFO);

		jar.close();
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		return findClass(name, true);
	}

	/**
	 * Obtains the module loaded by this classloader.
	 * @return The module loaded by this classloader.
	 */
	public EdenModule getModule()
	{
		return module;
	}

	/**
	 * Obtains a set of classes loaded by this classloader.
	 * @return A set of classes loaded by this classloader.
	 */
	public Set<String> getClasses()
	{
		return interior_classes.keySet();
	}

	/**
	 * Gets the module class inside the jar file associated with this module.
	 * If a module class has not already been found, one will be searched for. 
	 * May return null if the given .jar does not contain an EdenModule class.
	 * @return Gets the module class found inside the given .jar.
	 * @since 2.1.2
	 */
	public Class<? extends EdenModule> getModuleClass()
	{
		if (this.moduleClass == null) {
			if (locateModuleClass() == null)
			{
				return null;
			}
		}

		return this.moduleClass;
	}

	@SuppressWarnings("unchecked")
	private <T> List<Class<? extends T>> findSubclasses(boolean allowHideFromCollector, Class<?>... filter)
	{
		List<Class<? extends T>> buffer = new ArrayList<>();

		Enumeration<JarEntry> entries = jar.entries();
		JarEntry entry;
		while (entries.hasMoreElements())
		{
			entry = entries.nextElement();

			if (entry.isDirectory() || !entry.getName().endsWith(".class"))
				continue;

			Class<?> clazz;
			try {
				clazz = Class.forName(entry.getName().replace("/", ".").replace(".class", ""), false, this);
			} catch (ClassNotFoundException e) {
				continue;
			}

			// Check filter
			if (!isListAssignableFrom(clazz, filter))
				continue;

			// Handle annotations
			if (clazz.isAnnotationPresent(Unimplemented.class))
			{
				PrintUtils.logVerbose("Placing class \"" + clazz.getName() + "\" into unimplemented class map", InfoType.INFO);
				unimplemented_classes.put(clazz.getName(), clazz);
				continue;
			}

			if (clazz.isAnnotationPresent(HideFromCollector.class) && allowHideFromCollector)
				continue;

			buffer.add((Class<? extends T>) clazz);
		}

		return buffer;
	}

	private boolean isListAssignableFrom(Class<?> input, Class<?>... filter)
	{
		for (Class<?> clazz : filter)
			if (!clazz.isAssignableFrom(input))
				return false;

		return true;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;
		if (!(obj instanceof ModuleClassLoader))
			return false;
		if (obj.hashCode() == this.hashCode())
			return true;
		return false;
	}
	
	Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException
	{
		if (unimplemented_classes.containsKey(name))
			throw new ClassNotFoundException(unimplemented_classes.get(name).getAnnotation(Unimplemented.class).value());

		Class<?> result = interior_classes.get(name);

		if (result == null) 
		{
			if (checkGlobal)
			{
				result = loader.getCachedClass(name);
			}

			if (result == null)
			{
				result = super.findClass(name);

				if (result != null)
				{
					loader.setClass(name, result);
				}
			}

			interior_classes.put(name, result);
		}

		return result;
	}

	public String getModuleFileName()
	{
		return this.file.getName();
	}
}
