package com.yukiemeralis.blogspot.eden.gui;

import java.util.HashMap;

import com.yukiemeralis.blogspot.eden.gui.special.GuiTab;
import com.yukiemeralis.blogspot.eden.gui.special.PagedDynamicGui;
import com.yukiemeralis.blogspot.eden.gui.special.TabbedDynamicGui;
import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.module.EdenModule.ModInfo;
import com.yukiemeralis.blogspot.eden.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.eden.module.java.enums.PreventUnload;

import org.bukkit.Material;

@ModInfo(
    modName = "Surface",
    description = "Centralized system for static and dynamic inventory GUIs.",
    modFamily = "Eden base modules",
    version = "1.0.1",
    maintainer = "Yuki_emeralis",
    modIcon = Material.CHEST,
    supportedApiVersions = {"v1_16_R3", "v1_17_R1", "v1_18_R1", "v1_18_R2"}
)
@PreventUnload(CallerToken.EDEN)
public class GuiModule extends EdenModule
{
    public GuiModule() 
    {
        addListener(
            new PagedDynamicGui(0, "Invalid GUI", null, 0, null),
            new TabbedDynamicGui(0, "Invalid GUI", null, new HashMap<String, GuiTab>(), null)
        );
    }

    @Override
    public void onEnable() 
    {
        
    }

    @Override
    public void onDisable() 
    {
        
    }
}