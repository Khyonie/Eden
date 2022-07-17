package fish.yukiemeralis.eden.module;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.command.EdenCommand;
import fish.yukiemeralis.eden.module.ModuleFamilyRegistry.ModuleFamilyEntry;
import fish.yukiemeralis.eden.module.annotation.ModuleFamily;
import fish.yukiemeralis.eden.module.java.annotations.DefaultConfig;
import fish.yukiemeralis.eden.module.java.annotations.DefaultConfig.DefaultConfigWrapper;
import fish.yukiemeralis.eden.module.java.enums.DefaultConfigFailure;
import fish.yukiemeralis.eden.module.java.enums.PreventUnload;
import fish.yukiemeralis.eden.utils.ChatUtils;
import fish.yukiemeralis.eden.utils.FileUtils;
import fish.yukiemeralis.eden.utils.ItemUtils;
import fish.yukiemeralis.eden.utils.JsonUtils;
import fish.yukiemeralis.eden.utils.PrintUtils;
import fish.yukiemeralis.eden.utils.PrintUtils.InfoType;
import fish.yukiemeralis.eden.utils.Result;

/**
 * Represents an Eden module.
 * @author Yuki_emeralis
 */
@SuppressWarnings("unused")
public abstract class EdenModule
{
    protected String modName, version, description, maintainer;
    protected Material modIcon;
    private boolean isEnabled = false;

    private List<Listener> listeners = new ArrayList<>();
    private List<EdenCommand> commands = new ArrayList<>();

    //protected Map<String, String> config;
    protected ModuleConfig config;

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

    public Material getModIcon()
    {
        return this.modIcon;
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

    public String getDescription()
    {
        return this.description;
    }

    public String getMaintainer()
    {
        return this.maintainer;
    }

    /**
     * Obtains this module's version.
     * @return This module's version.
     */
    public String getVersion()
    {
        return this.version;
    }

    public ModuleFamilyEntry getFamily()
    {
        return ModuleFamilyRegistry.getFamily(this);
    }

    /**
     * @deprecated Legacy families are no longer supported. Method call will return a blank string.
     * @return A blank string. Use EdenModule#getFamily() instead.
     */
    @Deprecated
    public String getLegacyFamily()
    {
        return "";
    }

    /**
     * Obtains this module's configuration.
     * @return This module's configuration.
     */
    public ModuleConfig getConfig()
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
        PrintUtils.log("<Method> [" + current.getMethodName() + "] <in> [" + current.getClassName() + "] <requested instance from a module, however this module does not hide EdenModule#getInstance\\\\(\\\\)!>", InfoType.ERROR);
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
     * @deprecated This implementation is inefficient. Use this#generateIcon() instead.
     * @return This module represented by an itemstack.
     */
    @Deprecated
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
            "§r" + ChatUtils.of(secondary) + "Family: " + (this.getClass().isAnnotationPresent(ModuleFamily.class) ? this.getClass().getAnnotation(ModuleFamily.class).name() : "Unknown"),
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
    }

    /**
     * Applies a blank set of info to this module.
     * @param name The name generated for this module.
     * @deprecated As of 2.2.0, all modules must now specify an {@link fish.yukiemeralis.eden.module.EdenModule.ModInfo} annotation, rendering this method useless.
     */
    @Deprecated(forRemoval = true, since = "2.2.0")
    public void setBlankInfo(String name)
    {
        this.modName = name;
        this.version = "1.0";
        this.description = "This module has no information.";
        this.maintainer = "Unknown";
        this.modIcon = Material.BARRIER;
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
        String[] supportedApiVersions();
    }

    /**
     * Attempts to obtain the default config for this module. Result states:<p>
     * <b>Ok</b> - Success. Unwrap to obtain config.<p>
     * <b>NO_DEFAULT_CONFIG_ANNOTATION</b> - Class does not contain a DefaultConfig annotation.<p>
     * <b>NO_DEFAULT_CONFIG_FOUND</b> - Field name given by DefaultConfig annotation does not match an existing field.<p>
     * <i>- Field name can be specified inside annotation, otherwise default is "EDEN_DEFAULT_CONFIG".<p>
     * <b>INVALID_DEFAULT_CONFIG</b> - Data is not presented in valid format. Config must be a Map of strings and objects.<p>
     * <b>EMPTY_DEFAULT_CONFIG</b> - Config does not have any entries. Consider removing the DefaultConfig annotation and data.
     * @return A result containing a DefaultConfigWrapper, or an enum describing the error.
     */
    @SuppressWarnings("unchecked") // Cast is checked
    public Result<DefaultConfigWrapper, DefaultConfigFailure> getDefaultConfig()
    {
        Result<DefaultConfigWrapper, DefaultConfigFailure> data = new Result<>(DefaultConfigWrapper.class, DefaultConfigFailure.class);
        Class<? extends EdenModule> clazz = this.getClass();
        
        if (!clazz.isAnnotationPresent(DefaultConfig.class))
            return data.err(DefaultConfigFailure.NO_DEFAULT_CONFIG_ANNOTATION);

        DefaultConfig annotation = clazz.getAnnotation(DefaultConfig.class);
        String fieldName = annotation.value();
        Field field;
        Map<String, Object> config;

        try {
            field = clazz.getDeclaredField(fieldName);
            config = (Map<String, Object>) field.get(this);
        } catch (NoSuchFieldException e) { // Map does not exist
            return data.err(DefaultConfigFailure.NO_DEFAULT_CONFIG_FOUND);
        } catch (ClassCastException e) { // Map is not of <String, Object>
            return data.err(DefaultConfigFailure.INVALID_DEFAULT_CONFIG);
        } catch (SecurityException | IllegalAccessException e) { // Other generic errors
            return data.err(DefaultConfigFailure.UNKNOWN_ERROR);
        }
        
        if (config.isEmpty()) // No keys in map
            return data.err(DefaultConfigFailure.EMPTY_DEFAULT_CONFIG);

        return data.ok(new DefaultConfigWrapper(config));
    }

    /**
     * Loads this module's configuration from a file.
     */
    public void loadConfig()
    {
        File file = new File("./plugins/Eden/configs/" + this.modName + ".json");

        if (!file.exists())
        {
            if (!this.getClass().isAnnotationPresent(DefaultConfig.class))
            {
                PrintUtils.log("<Module \">[" + this.modName + "]<\" requests a configuration file, but one doesn't exist nor is a default config specified!>", InfoType.ERROR);
                return;
            }

            // DefaultConfig defaultconfig = this.getClass().getAnnotation(DefaultConfig.class);
            // Map<String, Object> dc = new HashMap<>();

            for (int i = 0; i < defaultconfig.keys().length; i++)
                dc.put(defaultconfig.keys()[i], defaultconfig.values()[i]);

            Result<DefaultConfigWrapper, DefaultConfigFailure> data = getDefaultConfig();
            switch (data.getState())
            {
                case OK:
                    break;
                case ERR:
                    switch ((DefaultConfigFailure) data.unwrap())
                    {
                        case EMPTY_DEFAULT_CONFIG:
                            PrintUtils.log("<Module \">[" + this.modName + "]<\" requests a configuration file, but the given default configuration is empty!>", InfoType.ERROR);
                            return;
                        case INVALID_DEFAULT_CONFIG:
                            PrintUtils.log("<Module \">[" + this.modName + "]<\"'s default configuration is invalid!>", InfoType.ERROR);
                            return;
                        case NO_DEFAULT_CONFIG_ANNOTATION:
                            PrintUtils.log("<Module \">[" + this.modName + "]<\" requests a configuration file, but one doesn't exist nor is a default config specified!>", InfoType.ERROR);
                            return;
                        case NO_DEFAULT_CONFIG_FOUND:
                            PrintUtils.log("<Module \">[" + this.modName + "]<\" requests a configuration file, but the given field name does not exist!>", InfoType.ERROR);
                            return;
                        case UNKNOWN_ERROR:
                        default:
                            PrintUtils.log("<Module \">[" + this.modName + "]<\" requests a configuration file, but an error occurred in accessing the default configuration.>", InfoType.ERROR);
                            break;
                    }
                    break;
            }

            Map<String, Object> dc = ((DefaultConfigWrapper) data.unwrap()).getData();
            for (int i = 0; i < defaultconfig.keys().length; i++)
                dc.put(defaultconfig.keys()[i], defaultconfig.values()[i]);


            ModuleConfig config = new ModuleConfig(dc);

            JsonUtils.toJsonFile("./plugins/Eden/configs/" + this.modName + ".json", config);
        }

        try {
            this.config = JsonUtils.fromJsonFile("./plugins/Eden/configs/" + this.modName + ".json", ModuleConfig.class);

            if (this.config == null)
                throw new ClassCastException();
        } catch (Exception e) {
            PrintUtils.log("<Configuration file for module \">(" + this.modName + ")<\" is corrupt! Moving to lost and found...>", InfoType.ERROR);
            FileUtils.moveToLostAndFound(file);

            DefaultConfig defaultconfig = this.getClass().getAnnotation(DefaultConfig.class);
            Map<String, Object> dc = new HashMap<>();

            for (int i = 0; i < defaultconfig.keys().length; i++)
                dc.put(defaultconfig.keys()[i], defaultconfig.values()[i]);

            ModuleConfig config = new ModuleConfig(dc);

            JsonUtils.toJsonFile("./plugins/Eden/configs/" + this.modName + ".json", config);
        }
        

        // Then do a quick double check to make sure that the config has all the expected values, specified in @DefaultConfig
        if (this.getClass().isAnnotationPresent(DefaultConfig.class))
        {
            if (this.config.getSize() < this.getClass().getAnnotation(DefaultConfig.class).keys().length)
            {
                DefaultConfig defaultconfig = this.getClass().getAnnotation(DefaultConfig.class);
                PrintUtils.log("<Local config file is missing configuration values. Filling in from default config, please review these new values.>", InfoType.WARN);

                for (int i = 0; i < defaultconfig.keys().length; i++)
                    if (!this.config.hasKey(defaultconfig.keys()[i]))
                    {
                        config.setKey(defaultconfig.keys()[i], defaultconfig.values()[i]);
                        PrintUtils.log("<" + defaultconfig.keys()[i] + " -\\> " + defaultconfig.values()[i] + ">", InfoType.WARN);
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
