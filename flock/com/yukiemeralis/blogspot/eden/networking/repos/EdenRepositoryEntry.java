package com.yukiemeralis.blogspot.eden.networking.repos;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.yukiemeralis.blogspot.eden.Eden;
import com.yukiemeralis.blogspot.eden.gui.GuiComponent;
import com.yukiemeralis.blogspot.eden.gui.GuiItemStack;
import com.yukiemeralis.blogspot.eden.gui.base.DynamicGui;
import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.module.java.ModuleDisableFailureData;
import com.yukiemeralis.blogspot.eden.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.eden.module.java.enums.ModuleDisableFailure;
import com.yukiemeralis.blogspot.eden.networking.NetworkingModule;
import com.yukiemeralis.blogspot.eden.networking.NetworkingUtils;
import com.yukiemeralis.blogspot.eden.networking.enums.DefaultDownloadBehavior;
import com.yukiemeralis.blogspot.eden.utils.FileUtils;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;
import com.yukiemeralis.blogspot.eden.utils.Option;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;
import com.yukiemeralis.blogspot.eden.utils.Result;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils.InfoType;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("unused")
public class EdenRepositoryEntry implements GuiComponent
{
    @Expose
    private String name, url, description, version, uploader, filename;    
    @Expose
    private String[] supportedApiVersions, tags;
    @Expose
    private long timestamp;

    private EdenRepository hostRepo;

    /**
     * Static location of the Eden modules folder.
     */
    public static final String MODULE_FOLDER = "./plugins/Eden/mods/";
    public static final String DOWNLOAD_FOLDER = "./plugins/Eden/dlcache/";

    public EdenRepositoryEntry(String name, String url, String version, String uploader, long timestamp, String... supportedApiVersions)
    {
        this.supportedApiVersions = supportedApiVersions;
        this.timestamp = timestamp;
        this.uploader = uploader;
        this.version = version;
        this.name = name;
        this.url = url;
    }

    public void setHostRepo(EdenRepository repo)
    {
        this.hostRepo = repo;
    }

    public String getName() 
    {
        return name;
    }

    public String getUrl() 
    {
        return url;
    }

    public String getVersion() 
    {
        return version;
    }

    public String getUploader() 
    {
        return uploader;
    }

    public long getTimestamp()
    {
        return this.timestamp;
    }

    public String[] getSupportedApiVersions() 
    {
        return supportedApiVersions;
    }

    public EdenRepository getHostRepo()
    {
        return this.hostRepo;
    }

    public String getDescription()
    {
        return this.description;
    }

    public String[] getTags()
    {
        return this.tags;
    }

    private GuiItemStack item;

    @Override
    public GuiItemStack toIcon() 
    {
        // Fml
        // if (item != null)
        //     return this.item;

        item = new GuiItemStack(ItemUtils.build(Material.BOOK, "§r§e§l" + this.name, generateLore())) 
        {
            @Override
            public void onIconInteract(InventoryClickEvent event) 
            {
                String upstreamFilename = null;

                switch (NetworkingModule.getModuleUpgradeStatus(getName(), getInstance()))
                {
                    case INCOMPATIBLE_SERVER:
                        PrintUtils.sendMessage(event.getWhoClicked(), "§cThis version of " + name + " is not compatible with the server version! (Needed: " + Arrays.toString(supportedApiVersions) + ", running: " + Eden.getNMSVersion() + ")");
                        PrintUtils.sendMessage(event.getWhoClicked(), "§oUpdate Eden, Spigot, or ask this repository's maintainer to add an entry for a compatible version.");
                        return;
                    case NOT_INSTALLED:
                        event.getWhoClicked().closeInventory();
                        PrintUtils.sendMessage(event.getWhoClicked(), "§aAttempting to install " + name + " " + version + "...");

                        // Attempt to fix filename if needed
                        upstreamFilename = fixUpstreamFilename(event.getWhoClicked(), NetworkingUtils.getFinalURLPortion(url));

                        String upstreamFilenameCopy = upstreamFilename; // Pain

                        NetworkingUtils.downloadFileFromURLThreaded(url, MODULE_FOLDER + upstreamFilename, new Thread() {
                            @Override
                            public void run()
                            {
                                PrintUtils.sendMessage(event.getWhoClicked(), "§aDownloaded " + name + "!");
                                String configValue = NetworkingModule.getModuleInstance().getConfig().get("defaultDownloadBehavior");

                                // Check if the config value is valid to avoid any nasty errors
                                try {
                                    DefaultDownloadBehavior.valueOf(configValue);
                                } catch (IllegalArgumentException e) {
                                    PrintUtils.log("Invalid default download behavior in config! Valid options are: [ IGNORE, MOVE_TO_DLCACHE, LOAD_NO_ENABLE, LOAD_ENABLE ]. Falling back to LOAD_ENABLE...", InfoType.ERROR);
                                    // Set to a valid value
                                    NetworkingModule.getModuleInstance().getConfig().replace("defaultDownloadBehavior", "LOAD_ENABLE");
                                }

                                switch (DefaultDownloadBehavior.valueOf(configValue))
                                {
                                    case IGNORE: // File is downloaded, don't do anything
                                        break;
                                    case LOAD_ENABLE:
                                        // Run as sync
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() 
                                            {
                                                Result<EdenModule, String> result = Eden.getModuleManager().loadSingleModule(MODULE_FOLDER + upstreamFilenameCopy);
                                                load: switch (result.getState())
                                                {
                                                    case ERR:
                                                        PrintUtils.sendMessage(event.getWhoClicked(), "§cFailed to load module into memory! See console for details. (Error: " + result.unwrap() + ")");
                                                        return;
                                                    case OK:
                                                        PrintUtils.sendMessage(event.getWhoClicked(), "§aLoaded module into memory, enabling...");
                                                        break load;
                                                }

                                                EdenModule mod = (EdenModule) result.unwrap();
                                                Eden.getModuleManager().enableModule(mod);

                                                if (Eden.getModuleManager().getEnabledModuleByName(mod.getName()) != null)
                                                {
                                                    PrintUtils.sendMessage(event.getWhoClicked(), "§aEnabled " + mod.getName() + " " + mod.getVersion() + "!");
                                                    return;
                                                }

                                                PrintUtils.sendMessage(event.getWhoClicked(), "§cFailed to enable " + mod.getName() + "! See console for details.");
                                                
                                            }
                                        }.runTask(Eden.getInstance());
                                        
                                        break;
                                    case LOAD_NO_ENABLE:
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() 
                                            {
                                                Result<EdenModule, String> result = Eden.getModuleManager().loadSingleModule(MODULE_FOLDER + upstreamFilenameCopy);
                                                load: switch (result.getState())
                                                {
                                                    case ERR:
                                                        PrintUtils.sendMessage(event.getWhoClicked(), "§cFailed to load module into memory! See console for details. (Error: " + result.unwrap() + ")");
                                                        return;
                                                    case OK:
                                                        PrintUtils.sendMessage(event.getWhoClicked(), "§aLoaded module into memory!");
                                                        break load;
                                                }
                                                
                                            }
                                        }.runTask(Eden.getInstance());

                                        break;
                                    case MOVE_TO_DLCACHE:
                                        File original = new File(MODULE_FOLDER + upstreamFilenameCopy);
                                        FileUtils.copy(original, new File(DOWNLOAD_FOLDER + upstreamFilenameCopy));
                                        original.delete();
                                        break;
                                }

                                NetworkingModule.updateLastKnownSync(getInstance());

                                // And back to being synchronous
                                new BukkitRunnable() 
                                {
                                    @Override
                                    public void run() 
                                    {
                                        new RepositoryGui((Player) event.getWhoClicked(), getHostRepo()).display(event.getWhoClicked());  
                                    }
                                }.runTask(Eden.getInstance());
                            }
                        });    
                        return;
                    case UPGRADABLE:
                        event.getWhoClicked().closeInventory();
                        PrintUtils.sendMessage(event.getWhoClicked(), "§aAttempting to update " + name + "... (" + Eden.getModuleManager().getModuleByName(name).getVersion() + " -> " + version + ")");

                        upstreamFilename = fixUpstreamFilename(event.getWhoClicked(), NetworkingUtils.getFinalURLPortion(url));

                        if (upstreamFilename == null)
                            return;

                        // Disable and unload relevant module
                        EdenModule target = Eden.getModuleManager().getModuleByName(name);
                        boolean enabled = target.getIsEnabled(); // Keep enabled modules enabled and disabled modules disabled

                        if (enabled)
                        {
                            Option<ModuleDisableFailureData> result = Eden.getModuleManager().disableModule(target.getName(), CallerToken.EDEN);

                            data: switch (result.getState())
                            {
                                case NONE: // Don't need to do anything
                                    break data;
                                case SOME: // Disable failed, reload given modules
                                    PrintUtils.sendMessage(event.getWhoClicked(), "§cFailed to disable module! Attempting to perform rollback on " + result.unwrap().getDownstreamModules().size() + " " + PrintUtils.plural(result.unwrap().getDownstreamModules().size(), "module", "modules") + "...");
                                    PrintUtils.sendMessage(event.getWhoClicked(), "§c§oTechnical failure reason: " + result.unwrap().getReason().name());

                                    if (result.unwrap().performRollback())
                                    {
                                        PrintUtils.sendMessage(event.getWhoClicked(), "§cRollback complete.");
                                        return;
                                    }

                                    PrintUtils.sendMessage(event.getWhoClicked(), "§cRollback failed.");
                                    return;
                            }
                        }

                        Eden.getModuleManager().removeModuleFromMemory(target.getName(), CallerToken.EDEN);

                        // Delete the module file
                        File f = new File(Eden.getModuleManager().getReferences().get(name));
                        
                        if (!f.delete())
                        {
                            PrintUtils.sendMessage(event.getWhoClicked(), "§cFailed to delete module file! Please delete manually.");
                            return;
                        }                        
                        
                        NetworkingUtils.downloadFileFromURLThreaded(url, MODULE_FOLDER + upstreamFilename, new Thread() {
                            @Override
                            public void run()
                            {
                                PrintUtils.sendMessage(event.getWhoClicked(), "§aDownloaded " + name + "!");
                                NetworkingModule.updateLastKnownSync(getInstance());

                                // Run as sync
                                new BukkitRunnable() {
                                    @Override
                                    public void run() 
                                    {
                                        Result<EdenModule, String> result = Eden.getModuleManager().loadSingleModule(f.getAbsolutePath());

                                        switch (result.getState())
                                        {
                                            case ERR:
                                                PrintUtils.sendMessage(event.getWhoClicked(), "§cFailed to reload module! (Error: " + result.unwrap() + ")");
                                                return;
                                            case OK:
                                                new RepositoryGui((Player) event.getWhoClicked(), getInstance().getHostRepo()).display(event.getWhoClicked());
                                                PrintUtils.sendMessage(event.getWhoClicked(), "§aUpdated " + name + "!");
                                                break;
                                        }

                                        if (enabled)
                                            Eden.getModuleManager().enableModule((EdenModule) result.unwrap());
                                    }
                                }.runTask(Eden.getInstance());
                            }
                        });
                        return;
                    case SAME_VERSION:
                        PrintUtils.sendMessage(event.getWhoClicked(), "§cThis module is already installed!");
                        return;
                    case UPGRADABLE_INCOMPATIBLE_SERVER:
                        PrintUtils.sendMessage(event.getWhoClicked(), "§cThis version of \"" + name + "\" is not compatible with the server version! (Needed: " + Arrays.toString(supportedApiVersions) + ", running: " + Eden.getNMSVersion() + ")");
                        PrintUtils.sendMessage(event.getWhoClicked(), "§oUpdate Eden, Spigot, or ask this repository's maintainer to add an entry for a compatible version.");
                        return;
                }
            }
        };

        return item;
    }

    public String fixUpstreamFilename(CommandSender feedbackViewer, String input)
    {
        if (input.endsWith(".jar"))
            return input;

        PrintUtils.sendMessage(feedbackViewer, "§eAttempting to resolve filename...");

        if (filename == null)
        {
            PrintUtils.sendMessage(feedbackViewer, "§cFailed to resolve filename. Aborting...");
            return null;
        }

        String upstreamFilename = filename;

        if (!upstreamFilename.endsWith(".jar"))
            upstreamFilename = upstreamFilename + ".jar";

        return upstreamFilename;
    }

    @Override
    public void onIconInteract(InventoryClickEvent event) 
    {
        toIcon().onIconInteract(event);
    }  

    protected EdenRepositoryEntry getInstance()
    {
        return this;
    }

    private String[] generateLore()
    {
        List<String> lore = new ArrayList<>();

        // Truncate URL to be smaller
        String changedUrl = url;
        if (changedUrl.length() > 37)
            changedUrl = changedUrl.substring(0, 38) + "...";

        StringBuilder builder = new StringBuilder("Tags: ");
        if (tags.length == 0)
            builder.append("none");
        for (String s : tags)
            builder.append(s + ", ");

        builder.replace(builder.length() - 2, builder.length() - 1, "");

        lore.add("§r§7§o" + changedUrl);
        lore.add("§r§7§o" + description);
        lore.add("§r§7§o" + version);
        lore.add("§r§7§o" + uploader);
        lore.add("§r§7§o" + Arrays.toString(supportedApiVersions));
        lore.add("§r§7§o" + builder.toString());
        lore.add("");

        switch (NetworkingModule.getModuleUpgradeStatus(this.name, this))
        {
            case INCOMPATIBLE_SERVER:
                lore.add("§r§cModule is not supported by server version!");
                break;
            case NOT_INSTALLED:
                lore.add("§r§aClick to install module.");
                break;
            case SAME_VERSION:
                lore.add("§r§bInstalled version: " + Eden.getModuleManager().getModuleByName(name).getVersion());
                lore.add("§r§cThis module is already installed!");
                break;
            case UPGRADABLE:
                lore.add("§r§bInstalled version: " + Eden.getModuleManager().getModuleByName(name).getVersion());
                lore.add("§r§aClick to update module.");
                break;
            case UPGRADABLE_INCOMPATIBLE_SERVER:
                lore.add("§r§bInstalled version: " + Eden.getModuleManager().getModuleByName(name).getVersion());
                lore.add("§r§cNew module is not supported by server version!");
                break;
        }

        return lore.toArray(new String[0]);
    }
}
