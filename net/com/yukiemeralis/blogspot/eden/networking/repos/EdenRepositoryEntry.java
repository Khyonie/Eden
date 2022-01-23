package com.yukiemeralis.blogspot.eden.networking.repos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.yukiemeralis.blogspot.eden.Eden;
import com.yukiemeralis.blogspot.eden.gui.GuiComponent;
import com.yukiemeralis.blogspot.eden.gui.GuiItemStack;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EdenRepositoryEntry implements GuiComponent
{
    @Expose
    private String name, url, version, uploader;    
    @Expose
    private String[] supportedApiVersions;

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

    @Override
    public GuiItemStack toIcon() 
    {
        return new GuiItemStack(ItemUtils.build(Material.BOOK, "§r§e§l" + this.name, generateLore())) 
        {
            @Override
            public void onIconInteract(InventoryClickEvent event) 
            {
                // TODO This
                // Also maybe show dependencies?
            }
        };
    }

    @Override
    public void onIconInteract(InventoryClickEvent event) 
    {

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
