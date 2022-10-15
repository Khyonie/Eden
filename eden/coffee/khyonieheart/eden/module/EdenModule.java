package coffee.khyonieheart.eden.module;

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

import coffee.khyonieheart.eden.Eden;
import coffee.khyonieheart.eden.command.EdenCommand;
import coffee.khyonieheart.eden.module.ModuleFamilyRegistry.ModuleFamilyEntry;
import coffee.khyonieheart.eden.module.annotation.EdenConfig;
import coffee.khyonieheart.eden.module.annotation.HideFromCollector;
import coffee.khyonieheart.eden.module.annotation.ModuleFamily;
import coffee.khyonieheart.eden.module.annotation.PreventUnload;
import coffee.khyonieheart.eden.module.annotation.EdenConfig.DefaultConfigWrapper;
import coffee.khyonieheart.eden.module.java.enums.DefaultConfigFailure;
import coffee.khyonieheart.eden.utils.ChatUtils;
import coffee.khyonieheart.eden.utils.FileUtils;
import coffee.khyonieheart.eden.utils.ItemUtils;
import coffee.khyonieheart.eden.utils.JsonUtils;
import coffee.khyonieheart.eden.utils.PrintUtils;
import coffee.khyonieheart.eden.utils.exception.TimeSpaceDistortionException;
import coffee.khyonieheart.eden.utils.logging.Logger.InfoType;
import coffee.khyonieheart.eden.utils.option.Option;
import coffee.khyonieheart.eden.utils.result.Result;
import net.md_5.bungee.api.ChatColor;

/**
 * Represents an Eden module.
 * @author Yuki_emeralis
 * @since 1.0
 */
@HideFromCollector
public abstract class EdenModule
{
    /** Module name */
    protected String modName;
    /** Module version */
    protected String version;
    /** Module description */
    protected String description;
    /** Module maintainer */
    protected String maintainer;
    /** Module material icon */
    protected Material modIcon;
    
    private boolean isEnabled = false;

    private List<Listener> listeners = new ArrayList<>();
    private List<EdenCommand> commands = new ArrayList<>();

    /** Module config data */
    protected ModuleConfig config;

    /** Modules that depend on this module */
    protected List<EdenModule> reliantModules = new ArrayList<>();

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
     * Obtains the module icon for this module.
     * @return This module's icon.
     */
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

    /**
     * Obtains this module's description.
     * @return This module's description.
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Obtains this module's maintainer.
     * @return This module's mantainer.
     */
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

    /**
     * Obtains this module's {@link ModuleFamilyEntry}.
     * @return This module's module family entry.
     */
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

    /**
     * Obtains this module's Object instance. Must be shadowed in order to not throw an {@link UnsupportedOperationException}.
     * @return This module's Object instance.
     * @throws UnsupportedOperationException When this method is not shadowed by a subclass 
     */
    public static EdenModule getModuleInstance()
    {
        // For some unholy reason this is faster than Thread.getCurrentThread().getCurrentStacktrace()
        StackTraceElement current = new Throwable().getStackTrace()[1];
        PrintUtils.log("<Method> [" + current.getMethodName() + "] <in> [" + current.getClassName() + "] <requested instance from a module, however this module does not hide EdenModule#getInstance\\\\(\\\\)!>", InfoType.ERROR);
        throw new UnsupportedOperationException();
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
        this.modName = ChatColor.stripColor(info.modName());
        this.version = info.version();
        this.description = info.description();
        this.maintainer = info.maintainer();
        this.modIcon = info.modIcon();

        if (!this.modIcon.isItem())
        {
            PrintUtils.log("Module \"" + this.modName + "\"'s declared module icon is \"" + this.modIcon.name() + "\" which cannot be displayed in a GUI! Changing to PUMPKIN...", InfoType.WARN);
            this.modIcon = Material.PUMPKIN;
        }
    }

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
        /** Supported NMS API versions */
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
    public Result getDefaultConfig()
    {
        Class<? extends EdenModule> clazz = this.getClass();
        
        // if (!clazz.isAnnotationPresent(EdenConfig.class))
        //     return data.err(DefaultConfigFailure.NO_DEFAULT_CONFIG_ANNOTATION);

        EdenConfig annotation = clazz.getAnnotation(EdenConfig.class);
        String fieldName = annotation.value();
        Field field;
        Map<String, Object> config;

        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            config = (Map<String, Object>) field.get(this);
        } catch (NoSuchFieldException e) { // Map does not exist
            return Result.err(DefaultConfigFailure.NO_DEFAULT_CONFIG_FOUND);
        } catch (ClassCastException e) { // Map is not of <String, Object>
            return Result.err(DefaultConfigFailure.INVALID_DEFAULT_CONFIG);
        } catch (SecurityException | IllegalAccessException e) { // Other generic errors
            return Result.err(DefaultConfigFailure.UNKNOWN_ERROR);
        }
        
        if (config.isEmpty()) // No keys in map
            return Result.err(DefaultConfigFailure.EMPTY_DEFAULT_CONFIG);

        return Result.ok(new DefaultConfigWrapper(config));
    }

    /**
     * Safely gets a defaultconfigwrapper. An OptionState of NONE indicates a failure, and an error describing the
     * issue will be printed.
     * @param file This module's configuration file.
     * @return An option containing a DefaultConfigWrapper.
     * @eden.optional {@link coffee.khyonieheart.eden.module.annotation.EdenConfig.DefaultConfigWrapper}
     */
    private Option getConfigSafe(File file)
    {
        // TODO Java 17 preview feature
        Result result = getDefaultConfig();
        switch (result.getState())
        {
            case OK:
                return Option.some(result.unwrapOk(DefaultConfigWrapper.class));
            case ERR:
                switch (result.unwrapErr(DefaultConfigFailure.class))
                {
                    case EMPTY_DEFAULT_CONFIG:
                        if (!file.exists())
                        {                            
                            PrintUtils.log("<Module \">[" + this.modName + "]<\" requested a new configuration file, but the supplied default configuration is empty! Cannot load module.>", InfoType.ERROR);
                            PrintUtils.log("§a- If you are a server owner: please contact this module's maintainer \\(\"" + this.maintainer + "\"\\).");
                            PrintUtils.log("§b- If you are a developer: please populate your default configuration, or consider removing the @EdenConfig annotation entirely.");
                            return Option.none();
                        }
                        PrintUtils.log("<Module \">[" + this.modName + "]<\"'s default configuration is empty! Cannot verify current stored configuration integrity.>", InfoType.ERROR);
                        PrintUtils.log("§a- If you are a server owner: please contact this module's maintainer \\(\"" + this.maintainer + "\"\\).");
                        PrintUtils.log("§b- If you are a developer: please populate your default configuration.");
                        return Option.none();
                    case INVALID_DEFAULT_CONFIG:
                        if (!file.exists())
                        {   
                            PrintUtils.log("<Module \">[" + this.modName + "]<\"requested a new configuration file, but the supplied default configuration is invalid! Cannot load module.>", InfoType.ERROR);
                            PrintUtils.log("§a- If you are a server owner: please contact this module's maintainer \\(\"" + this.maintainer + "\"\\).");
                            PrintUtils.log("§b- If you are a developer: please ensure your configuration is stored as a Map\\<String, Object\\>.");
                            return Option.none();
                        }
                        PrintUtils.log("<Module \">[" + this.modName + "]<\"'s default configuration is invalid! Cannot verify current stored configuration integrity.>", InfoType.ERROR);
                        PrintUtils.log("§a- If you are a server owner: please contact this module's maintainer \\(\"" + this.maintainer + "\"\\).");
                        PrintUtils.log("§b- If you are a developer: please ensure your configuration is stored as a Map\\<String, Object\\>.");
                        return Option.none();
                    case NO_DEFAULT_CONFIG_FOUND:
                        if (!file.exists())
                        {
                            PrintUtils.log("<Module \">[" + this.modName + "]<\" requested a new configuration file, but the given Object name \"" + this.getClass().getAnnotation(EdenConfig.class).value() + "\" does not exist! Cannot load module.>", InfoType.ERROR);
                            PrintUtils.log("§a- If you are a server owner: please contact this module's maintainer \\(\"" + this.maintainer + "\"\\).");
                            PrintUtils.log("§b- If you are a developer: please ensure your default configuration mapping is named as seen above. See the wiki for more info.");
                            return Option.none();
                        }
                        PrintUtils.log("<Module \">[" + this.modName + "]<\" requested a new configuration file, but the given Object name \"" + this.getClass().getAnnotation(EdenConfig.class).value() + "\" does not exist! Cannot load module.>", InfoType.ERROR);
                        PrintUtils.log("§a- If you are a server owner: please contact this module's maintainer \\(\"" + this.maintainer + "\"\\).");
                        PrintUtils.log("§b- If you are a developer: please ensure your default configuration mapping is named as seen above. See the wiki for more info.");
                        return Option.none();
                    case UNKNOWN_ERROR:
                    default:
                        PrintUtils.log("<Module \">[" + this.modName + "]<\" requested a configuration file, but an error occurred in accessing the default configuration.>", InfoType.ERROR);
                        return Option.none();
                }
            default:
                throw new TimeSpaceDistortionException(); // This shouldn't fire unless something exceptionally catastrophic happens
        }
    }

    /**
     * Loads this module's configuration from a file.
     * @return Whether or not loading was successful
     */
    public boolean loadConfig()
    {
        File file = new File("./plugins/Eden/configs/" + this.modName + ".json");
        PrintUtils.logVerbose("Attempting to load \"" + this.modName + "\"'s configuration...", InfoType.INFO);

        // Attempt to pull config data from @EdenConfig annotation
        // TODO Java 17 preview feature
        Map<String, Object> defaultConfig;
        Option opt = getConfigSafe(file);
        switch (opt.getState())
        {
            case SOME:
                defaultConfig = opt.unwrap(DefaultConfigWrapper.class).getData();
                break;
            default:
                return false;
        }

        // Generate config file if needed
        if (!file.exists())
        {
            ModuleConfig config = new ModuleConfig(new HashMap<>(defaultConfig));

            JsonUtils.toJsonFile("./plugins/Eden/configs/" + this.modName + ".json", config);
        }

        // Proceed to load config from file. If this is a fresh file, should be an exact copy of the default config
        try {
            this.config = JsonUtils.fromJsonFile("./plugins/Eden/configs/" + this.modName + ".json", ModuleConfig.class);

            if (this.config == null)
                throw new ClassCastException();
        } catch (Exception e) {
            PrintUtils.log("<Stored configuration file for module \">(" + this.modName + ")<\" is corrupt! Moving to lost and found...>", InfoType.ERROR);
            FileUtils.moveToLostAndFound(file);

            ModuleConfig config = new ModuleConfig(new HashMap<>(defaultConfig));

            JsonUtils.toJsonFile("./plugins/Eden/configs/" + this.modName + ".json", config);
        }

        // Check for missing keys
        boolean missingValueWarned = false;
        for (String key : defaultConfig.keySet())
        {
            if (config.hasKey(key))
                continue;
            
            if (!missingValueWarned)
            {
                PrintUtils.log("(Stored configuration file is missing configuration values. Filling in from default configuration, please review these new values.)", InfoType.WARN);
                missingValueWarned = true;
            }

            this.config.setKey(key, defaultConfig.get(key));
            PrintUtils.log("(" + key + " -\\> " + defaultConfig.get(key) + " \\(of type: " + (defaultConfig.get(key) != null ? defaultConfig.get(key).getClass().getSimpleName() : "null") + "\\))", InfoType.WARN);
        }

        // Then do the reverse, checking for keys that aren't needed
        for (String key : this.config.getKeys())
        {
            if (defaultConfig.containsKey(key))
                continue;

            PrintUtils.log("(Trimming unused key \"" + key + "\"...)");
            this.config.removeKey(key);
        }

        // Finally, attempt to update to the 1.6.0 standard of <String, Object>
        PrintUtils.logVerbose("Attempting to update old configuration data to the 1.6.0 standard...", InfoType.INFO);

        switch (updateOldConfigData(defaultConfig))
        {
            case FAILURE:
                PrintUtils.log("Failed to update configuration data. See stacktrace for details.", InfoType.ERROR);
                break;
            case NOTHING_TO_DO:
                PrintUtils.logVerbose("Nothing to do.", InfoType.INFO);
                break;
            case SUCCESS:
                PrintUtils.log("Successfully updated configuration for module \"" + this.modName + "\".");
                break;
        }

        this.saveConfig();

        return true;
    }

    private ConfigUpdateStatus updateOldConfigData(Map<String, Object> defaultConfig)
    {
        try {
            int updated = 0;
            loop: for (String key : this.config.getKeys())
            {
                Object stored = config.getKey(key);
                Object defaultVal = defaultConfig.get(key);

                if (stored == null)
                {
                    if (defaultVal == null)
                    {
                        PrintUtils.log("(Both the stored configuration and default configuration value for key \"" + key + "\" is null. Consider removing this key.)");
                        continue;
                    }

                    PrintUtils.log("(Stored value for key \"" + key + "\" is null, filling in from default value...)");
                    PrintUtils.log("(" + key + ": null -\\> " + defaultVal + " )");
                    this.config.setKey(key, defaultVal);

                    updated++;
                    continue;   
                }

                if (defaultVal.getClass().isAssignableFrom(stored.getClass()))
                    continue; // Key doesn't need to be updated

                PrintUtils.log("(Stored value and default value for key \"" + key + "\" are of different types! \\(stored: " + stored.getClass().getSimpleName() + " | default: " + defaultVal.getClass().getSimpleName() + "\\) " + (stored instanceof String ? "Attempting to convert..." : "") + ")"); 
                
                if (!(stored instanceof String))
                {
                    PrintUtils.log("<Cannot automatically convert \"" + key + "\". Filling in from default value...>");
                    PrintUtils.log("(" + key + ": " + stored + " \\(" + stored.getClass().getSimpleName() + "\\) -\\> " + defaultVal + " \\(" + defaultVal.getClass().getSimpleName() + "\\))");
                    this.config.setKey(key, defaultVal);

                    updated++;
                    continue;
                }

                update: switch (defaultVal.getClass().getName())
                {
                    case "java.lang.Integer":
                        try {
                            Integer i = Integer.parseInt((String) stored);
                            this.config.setKey(key, i);
                        } catch (NumberFormatException e) {
                            PrintUtils.log("<Cannot automatically convert \"" + key + "\". Filling in from default value...>");
                            PrintUtils.log("<" + key + ": " + stored + " -\\> " + defaultVal + " >");
                            this.config.setKey(key, defaultVal);

                            updated++;
                        }
                        break update;
                    case "java.lang.Byte":
                        try {
                            Byte i = Byte.parseByte((String) stored);
                            this.config.setKey(key, i);
                        } catch (NumberFormatException e) {
                            PrintUtils.log("<Cannot automatically convert \"" + key + "\". Filling in from default value...>");
                            PrintUtils.log("<" + key + ": " + stored + " -\\> " + defaultVal + " >");
                            this.config.setKey(key, defaultVal);

                            updated++;
                        }
                        break update;
                    case "java.lang.Short":
                        try {
                            Short i = Short.parseShort((String) stored);
                            this.config.setKey(key, i);
                        } catch (NumberFormatException e) {
                            PrintUtils.log("<Cannot automatically convert \"" + key + "\". Filling in from default value...>");
                            PrintUtils.log("<" + key + ": " + stored + " -\\> " + defaultVal + " >");
                            this.config.setKey(key, defaultVal);

                            updated++;
                        }
                        break update;
                    case "java.lang.Long":
                        try {
                            Long i = Long.parseLong((String) stored);
                            this.config.setKey(key, i);
                        } catch (NumberFormatException e) {
                            PrintUtils.log("<Cannot automatically convert \"" + key + "\". Filling in from default value...>");
                            PrintUtils.log("<" + key + ": " + stored + " -\\> " + defaultVal + " >");
                            this.config.setKey(key, defaultVal);

                            updated++;
                        }
                        break update;
                    case "java.lang.Float":
                        try {
                            Float i = Float.parseFloat((String) stored);
                            this.config.setKey(key, i);
                        } catch (NumberFormatException e) {
                            PrintUtils.log("<Cannot automatically convert \"" + key + "\". Filling in from default value...>");
                            PrintUtils.log("<" + key + ": " + stored + " -\\> " + defaultVal + " >");
                            this.config.setKey(key, defaultVal);

                            updated++;
                        }
                        break update;
                    case "java.lang.Double":
                        try {
                            Double i = Double.parseDouble((String) stored);
                            this.config.setKey(key, i);
                        } catch (NumberFormatException e) {
                            PrintUtils.log("<Cannot automatically convert \"" + key + "\". Filling in from default value...>");
                            PrintUtils.log("<" + key + ": " + stored + " -\\> " + defaultVal + " >");
                            this.config.setKey(key, defaultVal);

                            updated++;
                        }
                        break update;
                    case "java.lang.Boolean":
                        String out = ((String) stored).trim().toLowerCase();

                        if (!out.equals("true") && !out.equals("false"))
                        {
                            PrintUtils.log("<Cannot automatically convert \"" + key + "\". Filling in from default value...>");
                            PrintUtils.log("<" + key + ": " + stored + " -\\> " + defaultVal + " >");
                            this.config.setKey(key, defaultVal);

                            updated++;
                            break update;
                        }

                        this.config.setKey(key, Boolean.parseBoolean(out));
                        updated++;
                        break update;
                    default:
                        PrintUtils.log("<Cannot automatically convert \"" + key + "\". Filling in from default value...>");
                        PrintUtils.log("<" + key + ": " + stored + " -\\> " + defaultVal + " >");
                        this.config.setKey(key, defaultVal);

                        updated++;
                        continue loop;
                }

                PrintUtils.log("(" + key + ": " + stored + " \\(" + stored.getClass().getSimpleName() + "\\) -\\> " + this.config.getKey(key) + " \\(" + this.config.getKey(key).getClass().getSimpleName() + "\\))");
            }

            return updated == 0 ? ConfigUpdateStatus.NOTHING_TO_DO : ConfigUpdateStatus.SUCCESS;
        } catch (Exception e) {
            PrintUtils.printPrettyStacktrace(e);
            return ConfigUpdateStatus.FAILURE;
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

    private static enum ConfigUpdateStatus
    {
        SUCCESS,
        FAILURE,
        NOTHING_TO_DO
        ;
    }
}
