package com.yukiemeralis.blogspot.eden.module;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.yukiemeralis.blogspot.eden.Eden;
import com.yukiemeralis.blogspot.eden.command.EdenCommand;
import com.yukiemeralis.blogspot.eden.module.java.ModuleManager;
import com.yukiemeralis.blogspot.eden.module.java.annotations.DefaultConfig;
import com.yukiemeralis.blogspot.eden.module.java.enums.PreventUnload;
import com.yukiemeralis.blogspot.eden.utils.ChatUtils;
import com.yukiemeralis.blogspot.eden.utils.FileUtils;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;
import com.yukiemeralis.blogspot.eden.utils.JsonUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils.InfoType;

/**
 * Represents an Eden module.
 * @author Yuki_emeralis
 */
@SuppressWarnings("unused")
public abstract class EdenModule
{
    protected String modName, version, description, maintainer, modFamily;
    protected Material modIcon;
    private boolean isEnabled = false;

    private List<Listener> listeners = new ArrayList<>();
    private List<EdenCommand> commands = new ArrayList<>();

    protected Map<String, String> config;

    protected List<EdenModule> reliantModules = new ArrayList<>(); // Modules that depend on this module

    /**
     * Runs once when the module is first loaded.
     */
    public abstract void onEnable();
    /**
     * Runs once when the module is being unloaded.
     */
    public abstract void onDisable();

    /**
     * Add one or more listeners to be registered on startup.
     * @param listener Variable argument array of listeners.
     */
    public void addListener(Listener... listener)
    {
        for (Listener l : listener)
            listeners.add(l);
    }

    /**
     * Add one or more commands to be registered on startup.
     * @param command Variable argument array of Eden commands.
     */
    public void addCommand(EdenCommand... command)
    {
        for (EdenCommand c : command)
            commands.add(c);
    }

    /**
     * Obtains whether or not this module is enabled.
     * @return If the module is enabled or not.
     */
    public boolean getIsEnabled()
    {
        return this.isEnabled;
    }

    /**
     * Sets this module's state to enabled.
     */
    public void setEnabled()
    {
        this.isEnabled = true;
    }

    /**
     * Sets this module's state to disabled.
     */
    public void setDisabled()
    {
        this.isEnabled = false;
    }

    /**
     * Obtains this module's name.
     * @return This module's name.
     */
    public String getName()
    {
        return this.modName;
    }

    /**
     * Obtains this module's version.
     * @return This module's version.
     */
    public String getVersion()
    {
        return this.version;
    }

    /**
     * Obtains this module's family. May not be set.
     * @return This module's family.
     */
    public String getModFamily()
    {
        return this.modFamily;
    }

    /**
     * Obtains this module's configuration.
     * @return This module's configuration.
     */
    public Map<String, String> getConfig()
    {
        return config;
    }

    /**
     * Obtains a list of listeners registered to this module.
     * @return A list of listeners.
     */
    public List<Listener> getListeners()
    {
        return this.listeners;
    }

    /**
     * Obtains a list of Eden commands registered to this module.
     * @return A list of Eden commands.
     */
    public List<EdenCommand> getCommands()
    {
        return this.commands;
    }

    /**
     * Obtains a list of other modules that depend on this module.
     * @return A list of modules.
     */
    public List<EdenModule> getReliantModules()
    {
        return this.reliantModules;
    }

    /**
     * Adds a module to this module as a reliant.
     * @param module The module to add.
     */
    public void addReliantModule(EdenModule module)
    {
        this.reliantModules.add(module);
    }

    /**
     * Removes a reliant module from this module.
     * @param module The module to remove.
     */
    public void removeReliantModule(EdenModule module)
    {
        this.reliantModules.remove(module);
    }

    public static EdenModule getModuleInstance()
    {
        // For some unholy reason this is faster than Thread.getCurrentThread().getCurrentStacktrace()
        StackTraceElement current = new Throwable().getStackTrace()[1];
        PrintUtils.log("(Method) [" + current.getMethodName() + "] in [" + current.getClassName() + "] (requested instance from a module, however this module does not hide EdenModule#getInstance\\\\(\\\\)!)", InfoType.ERROR);
        return null;
    }

    /**
     * Returns the instance of Eden running on this server.<p>
     * <i> Not to be confused with {@link EdenModule#getModuleInstance()}
     * @return
     */
    public static JavaPlugin getInstance()
    {
        return Eden.getInstance();
    }

    //
    // Module info generation
    //

    private final static String 
        primary = "DD73CE",
        secondary = "9CE8A7"
        ;


    /**
     * Generates an itemstack representation of this module.
     * Please note that this may become deprecated in the future in favor of a GUIComponent.
     * @return This module represented by an itemstack.
     */
    public ItemStack toIcon()
    {
        String status;
        if (isEnabled) {
            status = "§aenabled";
        } else {
            status = "§cdisabled";
        }

        String rulesHeader = "§r" + ChatUtils.of(primary) + "§lCan be disabled:";
        String 
            byPlayer =  "§r" + ChatUtils.of(secondary) + "- By player? [§a§l✔§r" + ChatUtils.of(secondary) + "]", 
            byConsole = "§r" + ChatUtils.of(secondary) + "- By console? [§a§l✔§r" + ChatUtils.of(secondary) + "]", 
            byEden =  "§r" + ChatUtils.of(secondary) + "- By Eden? [§a§l✔§r" + ChatUtils.of(secondary) + "]";

        if (this.getClass().isAnnotationPresent(PreventUnload.class))
        {
            switch (this.getClass().getAnnotation(PreventUnload.class).value())
            {
                case CONSOLE:
                    byPlayer = "§r" + ChatUtils.of(secondary) + "- By player? [§c§l✖§r" + ChatUtils.of(secondary) + "]";
                    break;
                case EDEN:
                    byPlayer = "§r" + ChatUtils.of(secondary) + "- By player? [§c§l✖§r" + ChatUtils.of(secondary) + "]";
                    byConsole = "§r" + ChatUtils.of(secondary) + "- By console? [§c§l✖§r" + ChatUtils.of(secondary) + "]";
                    break;
                default: break;
            }
        }

        ItemStack icon = new ItemStack(this.modIcon);
        ItemUtils.applyName(icon, "§r" + ChatUtils.of(primary) + "§l" + this.modName);
        ItemUtils.applyLore(icon, 
            "§r" + ChatUtils.of(secondary) + this.description,
            "§r" + ChatUtils.of(secondary) + "Version: " + this.version,
            "§r" + ChatUtils.of(secondary) + "Maintainer: " + this.maintainer,
            "§r" + ChatUtils.of(secondary) + "Family: " + this.modFamily,
            "",
            "§r" + ChatUtils.of(secondary) + "Status: " + status,
            "",
            rulesHeader,
            byPlayer,
            byConsole,
            byEden
        );

        ItemUtils.saveToNamespacedKey(icon, "modName", this.modName);

        return icon;
    }

    /**
     * Applies module info to this module from its ModInfo annotation.
     * @param info The annotation containing this module's information.
     */
    public void setInfo(ModInfo info)
    {
        this.modName = info.modName();
        this.version = info.version();
        this.description = info.description();
        this.maintainer = info.maintainer();
        this.modIcon = info.modIcon();
        this.modFamily = info.modFamily();
    }

    /**
     * Applies a blank set of info to this module.
     * @param name The name generated for this module.
     * @deprecated As of 2.2.0, all modules must now specify an {@link com.yukiemeralis.blogspot.eden.module.EdenModule.ModInfo} annotation, rendering this method useless.
     */
    @Deprecated(forRemoval = true, since = "2.2.0")
    public void setBlankInfo(String name)
    {
        this.modName = name;
        this.version = "1.0";
        this.description = "This module has no information.";
        this.maintainer = "Unknown";
        this.modIcon = Material.BARRIER;
        this.modFamily = "Unknown";
    }

    /**
     * Annotation to notify Eden that this module has a configuration file.
     * @author Yuki_emeralis
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface EdenConfig {}

    /**
     * Required annotation for all Eden modules to set themselves apart from other modules.
     * @author Yuki_emeralis
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface ModInfo
    {
    	/** This module's name */
        String modName(); 
        /** This module's version */
        String version();
        /** The material used as an icon for this module */
        Material modIcon();
        /** This module's description */
        String description();
        /** This module's maintainer */
        String maintainer();
        /** The family this module is associated with */
        String modFamily() default "Not part of a family";
        String[] supportedApiVersions();
    }

    /**
     * Loads this module's configuration from a file.
     */
    @SuppressWarnings("unchecked")
    public void loadConfig()
    {
        File file = new File("./plugins/Eden/configs/" + this.modName + ".json");

        if (!file.exists())
        {
            if (!this.getClass().isAnnotationPresent(DefaultConfig.class))
            {
                PrintUtils.log("(Module \")[" + this.modName + "](\" requests a configuration file, but one doesn't exist nor is a default config specified!)", InfoType.ERROR);
                return;
            }

            DefaultConfig defaultconfig = this.getClass().getAnnotation(DefaultConfig.class);
            Map<String, String> dc = new HashMap<>();

            for (int i = 0; i < defaultconfig.keys().length; i++)
                dc.put(defaultconfig.keys()[i], defaultconfig.values()[i]);

            JsonUtils.toJsonFile("./plugins/Eden/configs/" + this.modName + ".json", dc);
        }

        try {
            this.config = (HashMap<String, String>) JsonUtils.fromJsonFile("./plugins/Eden/configs/" + this.modName + ".json", HashMap.class);

            if (this.config == null)
                throw new ClassCastException();
        } catch (Exception e) {
            PrintUtils.log("(Configuration file for module \")<" + this.modName + ">(\" is corrupt! Moving to lost and found...)", InfoType.ERROR);
            FileUtils.moveToLostAndFound(file);

            DefaultConfig defaultconfig = this.getClass().getAnnotation(DefaultConfig.class);
            Map<String, String> dc = new HashMap<>();

            for (int i = 0; i < defaultconfig.keys().length; i++)
                dc.put(defaultconfig.keys()[i], defaultconfig.values()[i]);

            JsonUtils.toJsonFile("./plugins/Eden/configs/" + this.modName + ".json", dc);
        }
        

        // Then do a quick double check to make sure that the config has all the expected values, specified in @DefaultConfig
        if (this.getClass().isAnnotationPresent(DefaultConfig.class))
        {
            if (this.config.size() < this.getClass().getAnnotation(DefaultConfig.class).keys().length)
            {
                DefaultConfig defaultconfig = this.getClass().getAnnotation(DefaultConfig.class);
                PrintUtils.log("<Local config file is missing configuration values. Filling in from default config, please review these new values.>", InfoType.WARN);

                for (int i = 0; i < defaultconfig.keys().length; i++)
                    if (!this.config.containsKey(defaultconfig.keys()[i]))
                    {
                        config.put(defaultconfig.keys()[i], defaultconfig.values()[i]);
                        PrintUtils.log("<>", InfoType.WARN);
                    }

                this.saveConfig();
            }
        }
    }

    /**
     * Saves this module's configuration to a file.
     */
    public void saveConfig()
    {
        JsonUtils.toJsonFile("./plugins/Eden/configs/" + this.modName + ".json", config);
    }

    /**
     * Annotation to notify Eden that this module depends on other modules.
     * @author Yuki_emeralis
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface LoadBefore
    {
    	/**
    	 * An array of module names that a module depends on.
    	 */
        String[] loadBefore();
    }
}
