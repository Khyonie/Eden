package fish.yukiemeralis.eden.gui;

import java.util.HashMap;

import fish.yukiemeralis.eden.gui.special.GuiTab;
import fish.yukiemeralis.eden.gui.special.PagedDynamicGui;
import fish.yukiemeralis.eden.gui.special.TabbedDynamicGui;
import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.EdenModule.ModInfo;
import fish.yukiemeralis.eden.module.java.enums.CallerToken;
import fish.yukiemeralis.eden.module.java.enums.PreventUnload;
import fish.yukiemeralis.eden.utils.PrintUtils;

import org.bukkit.Material;

@ModInfo(
    modName = "Surface",
    description = "Centralized system for static and dynamic inventory GUIs.",
    version = "1.0.1",
    maintainer = "Yuki_emeralis",
    modIcon = Material.CHEST,
    supportedApiVersions = {"v1_16_R3", "v1_17_R1", "v1_18_R1", "v1_18_R2"}
)
@PreventUnload(CallerToken.EDEN)
@Deprecated
public class GuiModule extends EdenModule
{
    public GuiModule() 
    {
        PrintUtils.log("Surface 1 has been deprecated (3/23/2022 HJG). It may still be maintained as a legacy module. The Eden devs assume no responibility and issues concerning this module will be ignored. Use with caution.");

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