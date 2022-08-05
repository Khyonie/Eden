package fish.yukiemeralis.eden.surface2;

import fish.yukiemeralis.eden.module.EdenModule;
import fish.yukiemeralis.eden.module.EdenModule.ModInfo;
import fish.yukiemeralis.eden.module.annotation.ModuleFamily;
import fish.yukiemeralis.eden.module.annotation.PreventUnload;
import fish.yukiemeralis.eden.module.java.enums.CallerToken;

import org.bukkit.Material;

@ModInfo (
    modName = "Surface2",
    description = "Centralized system for inventory-based GUIs.",
    maintainer = "Yuki_emeralis",
    version = "1.1.1",
    modIcon = Material.OBSERVER,
    supportedApiVersions = { "v1_16_R3", "v1_17_R1", "v1_18_R1", "v1_18_R2", "v1_19_R1" }
)
@ModuleFamily(name = "Eden core modules", icon = Material.ENDER_EYE)
@PreventUnload(CallerToken.EDEN)
public class Surface extends EdenModule
{
    @Override
    public void onEnable() 
    {
        
    }

    @Override
    public void onDisable() 
    {
        
    }   
}
