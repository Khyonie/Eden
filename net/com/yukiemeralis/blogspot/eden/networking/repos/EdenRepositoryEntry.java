package com.yukiemeralis.blogspot.eden.networking.repos;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.yukiemeralis.blogspot.eden.Eden;
import com.yukiemeralis.blogspot.eden.gui.GuiComponent;
import com.yukiemeralis.blogspot.eden.gui.GuiItemStack;
import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.networking.NetworkingModule;
import com.yukiemeralis.blogspot.eden.networking.NetworkingUtils;
import com.yukiemeralis.blogspot.eden.networking.enums.DefaultDownloadBehavior;
import com.yukiemeralis.blogspot.eden.utils.FileUtils;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;
import com.yukiemeralis.blogspot.eden.utils.Result;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils.InfoType;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("unused")
public class EdenRepositoryEntry implements GuiComponent
{
    @Expose
    private String name, url, version, uploader, filename;    
    @Expose
    private String[] supportedApiVersions;
    @Expose
    private long timestamp;

    private static final String MODULE_FOLDER = "./plugins/Eden/mods/";
    private static final String DOWNLOAD_FOLDER = "./plugins/Eden/dlcache/";

    public EdenRepositoryEntry(String name, String url, String version, String uploader, long timestamp, String... supportedApiVersions)
    {
        this.supportedApiVersions = supportedApiVersions;
        this.timestamp = timestamp;
        this.uploader = uploader;
        this.version = version;
        this.name = name;
        this.url = url;
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

    private GuiItemStack item;

    @Override
    public GuiItemStack toIcon() 
    {
        if (item != null)
            return this.item;

        item = new GuiItemStack(ItemUtils.build(Material.BOOK, "§r§e§l" + this.name, generateLore())) 
        {
            @Override
            public void onIconInteract(InventoryClickEvent event) 
            {
                // TODO This
                // Also maybe show dependencies?

                switch (NetworkingModule.getModuleUpgradeStatus(getName(), getInstance()))
                {
                    case INCOMPATIBLE_SERVER:
                        PrintUtils.sendMessage(event.getWhoClicked(), "§cThis version of " + name + " is not compatible with the server version! (Needed: " + Arrays.toString(supportedApiVersions) + ", running: " + Eden.getNMSVersion() + ")");
                        PrintUtils.sendMessage(event.getWhoClicked(), "§oUpdate Eden, Spigot, or ask this repository's maintainer to add an entry for a compatible version.");
                        return;
                    case NOT_INSTALLED:
                        PrintUtils.sendMessage(event.getWhoClicked(), "§aAttempting to install " + name + " " + version + "...");

                        // Attempt to fix filename if needed
                        String upstreamFilename = NetworkingUtils.getFinalURLPortion(url);
                        if (!upstreamFilename.endsWith(".jar"))
                        {
                            PrintUtils.sendMessage(event.getWhoClicked(), "§eAttempting to resolve filename...");

                            if (filename == null)
                            {
                                PrintUtils.sendMessage(event.getWhoClicked(), "§cFailed to resolve filename. Aborting...");
                                return;
                            }

                            upstreamFilename = filename;

                            if (!upstreamFilename.endsWith(".jar"))
                                upstreamFilename = upstreamFilename + ".jar";
                        }

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
                            }
                        });    
                        return;
                    case SAME_VERSION:
                        PrintUtils.sendMessage(event.getWhoClicked(), "§cThis module is already installed!");
                        return;
                    case UPGRADABLE:
                        PrintUtils.sendMessage(event.getWhoClicked(), "§aAttempting to update " + name + "... (" + Eden.getModuleManager().getModuleByName(name).getVersion() + " -> " + version + ")");
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

        lore.add("§r§7§o" + url);
        lore.add("§r§7§o" + version);
        lore.add("§r§7§o" + uploader);
        lore.add("§r§7§o" + Arrays.toString(supportedApiVersions));
        lore.add("");

        switch (NetworkingModule.getModuleUpgradeStatus(this.name, this))
        {
            case INCOMPATIBLE_SERVER:
                lore.add("§r§cModule is not supported by server version!");
                break;
            case NOT_INSTALLED:
                lore.add("§r§aClick to install module. §7§oTODO Not done");
                break;
            case SAME_VERSION:
                lore.add("§r§bInstalled version: " + Eden.getModuleManager().getModuleByName(name).getVersion());
                lore.add("§r§cThis module is already installed!");
                break;
            case UPGRADABLE:
                lore.add("§r§bInstalled version: " + Eden.getModuleManager().getModuleByName(name).getVersion());
                lore.add("§r§aClick to update module. §7§oTODO Not done");
                break;
            case UPGRADABLE_INCOMPATIBLE_SERVER:
                lore.add("§r§bInstalled version: " + Eden.getModuleManager().getModuleByName(name).getVersion());
                break;
        }

        return lore.toArray(new String[0]);
    }
}
