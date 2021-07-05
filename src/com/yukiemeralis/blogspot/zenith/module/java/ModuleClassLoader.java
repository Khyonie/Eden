package com.yukiemeralis.blogspot.zenith.module.java;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

import com.yukiemeralis.blogspot.zenith.command.ZenithCommand;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.module.java.annotations.HideFromCollector;
import com.yukiemeralis.blogspot.zenith.module.java.annotations.Unimplemented;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

@SuppressWarnings("unused")
public class ModuleClassLoader extends URLClassLoader
{
    private File file;
	private ZenithModule module;
    private ModuleManager loader;
	//private Class<? extends ZenithModule> module_class;
	
	private final Map<String, Class<?>> interior_classes = new HashMap<>();
	private final Map<String, Class<?>> unimplemented_classes = new HashMap<>();
	
	public ModuleClassLoader(ClassLoader parent, ModuleManager loader, File file) throws MalformedURLException, NullPointerException
	{
		super(new URL[] { file.toURI().toURL() }, parent);
		
		this.loader = loader;
		this.file = file;

		// Attempt to locate the module file inside the given jar
		try {
			JarFile jar = new JarFile(file);
			Enumeration<JarEntry> entries = jar.entries();
			JarEntry entry;

			String entry_name = null;
			Class<?> buffer = null;

			List<Class<?>> listenerClasses = new ArrayList<>();
			List<Class<?>> commandClasses = new ArrayList<>();
			List<Class<?>> moduleClasses = new ArrayList<>();

			//
			// Gather all classes inside our jar
			//

			while (entries.hasMoreElements())
			{
				buffer = null;

				entry = entries.nextElement();

				// Filter non-classes
				if (entry.isDirectory() || !entry.getName().endsWith(".class"))
					continue;

				entry_name = entry.getName().replace("/", ".").replace(".class", "");

				// Drop the class into a list, if appropriate
				try {
					buffer = Class.forName(entry_name, false, this);

					if (buffer.isAnnotationPresent(Unimplemented.class))
					{
						PrintUtils.logVerbose("Placing \"" + entry_name + "\" into the unimplemented classes map", InfoType.INFO);
						unimplemented_classes.put(entry_name, buffer);
						continue;
					}

					if (!buffer.isAnnotationPresent(HideFromCollector.class))
					{
						if (ZenithModule.class.isAssignableFrom(buffer))
							moduleClasses.add(buffer);

						// Filter invalid commands
						if (ZenithCommand.class.isAssignableFrom(buffer))
						{
							try {
								buffer.getConstructor(ZenithModule.class);
								commandClasses.add(buffer);
							} catch (NoSuchMethodException e) {
								try {
									buffer.getConstructor();
									commandClasses.add(buffer);
								} catch (NoSuchMethodException e2) {
									PrintUtils.log("Command \"" + buffer.getName() + "\" does not contain a valid constructor!", InfoType.ERROR);
									PrintUtils.log("If you are a server owner seeing this message, please update this module, or contact this module's maintainer.", InfoType.ERROR);
								}
							}
						}
							
						// Filter invalid events
						if (Listener.class.isAssignableFrom(buffer))
						{
							try {
								buffer.getConstructor();
								listenerClasses.add(buffer);
							} catch (NoSuchMethodException e) {
								PrintUtils.log("Event listener \"" + buffer.getName() + "\" does not contain a valid no-args constructor! For event listeners that require a constructor with parameters, please annotate them with \"@HideFromCollector\" and add them manually in your module constructor.", InfoType.ERROR);
								PrintUtils.log("If you are a server owner seeing this message, please contact this module's maintainer.", InfoType.ERROR);
							}
						}
					}

					interior_classes.put(entry_name, buffer);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}	
			}

			jar.close();

			//
			// Instantiate the module
			//

			// If no module is found, we don't really need to keep loading this file
			if (moduleClasses.size() == 0)
			{
				PrintUtils.log("Jar file \"" + file.getName() + "\" does not contain a valid ZenithModule class.", InfoType.ERROR);
				return;
			}

			// But we also don't want to have an ambiguous module file
			if (moduleClasses.size() > 1)
			{
				PrintUtils.log("Zenith module file \"" + file.getName() + "\" contains " + moduleClasses.size() + " ambiguous module classes! Not loading any of them...", InfoType.ERROR);
				return;
			}

			// Then we can instantiate the module and add its events and commands
			Constructor<?> moduleConstructor = null;
			for (Constructor<?> c : moduleClasses.get(0).getConstructors())
			{
				moduleConstructor = c;

				if (moduleConstructor.getParameterCount() == 0)
					break;
			}	

			if (moduleConstructor == null)
			{
				PrintUtils.log("Zenith module file \"" + file.getName() + "\" does not contain a valid no-args constructor!", InfoType.ERROR);
				return;
			}

			module = (ZenithModule) moduleConstructor.newInstance();

			PrintUtils.logVerbose("Located module class \"" + module.getClass().getName() + "\".", InfoType.INFO);

			// Finally assign commands and listeners

			List<ZenithCommand> commands = new ArrayList<>();
			List<Listener> events = new ArrayList<>();

			for (Class<?> commandClass : commandClasses)
			{
				try {
					ZenithCommand command = (ZenithCommand) commandClass.getConstructor(ZenithModule.class).newInstance(module);
					commands.add(command);
				} catch (NoSuchMethodException e) {
					try {
						ZenithCommand command = (ZenithCommand) commandClass.getConstructor().newInstance();
						commands.add(command);
					} catch (NoSuchMethodException e2) {}
				}
			}	

			for (Class<?> listenerClass : listenerClasses)
			{
				try {
					Listener listener = (Listener) listenerClass.getConstructor().newInstance();
					events.add(listener);
				} catch (NoSuchMethodException e) {} // We check for this earlier, so it's already handled
			}

			PrintUtils.logVerbose("Registered " + commands.size() + " command(s) and " + events.size() + " event(s).", InfoType.INFO);
			module.addCommand(commands.toArray(new ZenithCommand[commands.size()]));
			module.addListener(events.toArray(new Listener[events.size()]));
		} catch (IOException e) {
			throw new NullPointerException("Failed to access module file.");
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
			throw new NullPointerException();
		}
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		return findClass(name, true);
	}

	public ZenithModule getCoreModule()
	{
		return module;
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

	public Set<String> getClasses()
	{
		return interior_classes.keySet();
	}
}
