package com.yukiemeralis.blogspot.eden.networking.repos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.yukiemeralis.blogspot.eden.Eden;
import com.yukiemeralis.blogspot.eden.gui.GuiComponent;
import com.yukiemeralis.blogspot.eden.gui.GuiItemStack;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EdenRepositoryEntry implements GuiComponent
{
    @Expose
    private String name, url, version, uploader;    
    @Expose
    private String[] supportedApiVersions;

    private static final String MODULE_FOLDER = "./plugins/Eden/mods/";
    private static final String DOWNLOAD_FOLDER = "./plugins/Eden/dlcache/";

    public EdenRepositoryEntry(String name, String url, String version, String uploader, String... supportedApiVersions)
    {
        this.supportedApiVersions = supportedApiVersions;
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

                if (Eden.getModuleManager().isModulePresent(name))
                {
                    // Attempt to update module

                    // Check if we already have the same version
                    if (Eden.getModuleManager().getModuleByName(name).getVersion().equals(version))
                    {
                        PrintUtils.sendMessage(event.getWhoClicked(), "§cThis module is already installed!");
                        return;
                    }

                    // Check if the new version is compatible with the server
                    if (!Arrays.asList(supportedApiVersions).contains(Eden.getNMSVersion())) 
                    {
                        PrintUtils.sendMessage(event.getWhoClicked(), "§cThis version of " + name + " is not compatible with the server version! (Needed: " + Arrays.toString(supportedApiVersions) + ", running: " + Eden.getNMSVersion() + ")");
                        PrintUtils.sendMessage(event.getWhoClicked(), "§oUpdate Eden, Spigot, or ask this repository's maintainer to add an entry for a compatible version.");
                        return;
                    }

                    PrintUtils.sendMessage(event.getWhoClicked(), "§aAttempting to update " + name + "... (" + Eden.getModuleManager().getModuleByName(name).getVersion() + " -> " + version + ")");
                    // TODO This

                    // For both update and install new, we can safely unload modules as EDEN, download the file overtop the old one (if applicable), and then attempt to load it

                    return;
                }

                // Check if the new version is compatible with the server
                if (!Arrays.asList(supportedApiVersions).contains(Eden.getNMSVersion())) 
                {
                    PrintUtils.sendMessage(event.getWhoClicked(), "§cThis version of \"" + name + "\" is not compatible with the server version! (Needed: " + Arrays.toString(supportedApiVersions) + ", running: " + Eden.getNMSVersion() + ")");
                    PrintUtils.sendMessage(event.getWhoClicked(), "§oUpdate Eden, Spigot, or ask this repository's maintainer to add an entry for a compatible version.");
                    return;
                }

                PrintUtils.sendMessage(event.getWhoClicked(), "§aAttempting to install " + name + " " + version + "...");

                // TODO This
            }
        };

        return item;
    }

    @Override
    public void onIconInteract(InventoryClickEvent event) 
    {
        toIcon().onIconInteract(event);
    }  

    private String[] generateLore()
    {
        List<String> lore = new ArrayList<>();

        lore.add("§r§7§o" + url);
        lore.add("§r§7§o" + version);
        lore.add("§r§7§o" + uploader);
        lore.add("§r§7§o" + Arrays.toString(supportedApiVersions));
        lore.add("");

        if (Eden.getModuleManager().isModulePresent(name))
        {
            lore.add("§r§bInstalled version: " + Eden.getModuleManager().getModuleByName(name).getVersion());

            if (Eden.getModuleManager().getModuleByName(name).getVersion().equals(version))
            {
                lore.add("§r§cThis module is already installed!");
            } else {
                lore.add("§r§aClick to update module. §7§oTODO Not done");
            }
        } else {
            if (!Arrays.asList(supportedApiVersions).contains(Eden.getNMSVersion()))
            {
                lore.add("§r§cModule is not supported by server version!");
            } else {
                lore.add("§r§aClick to install module. §7§oTODO Not done");
            }
        }

        return lore.toArray(new String[0]);
    }
}
