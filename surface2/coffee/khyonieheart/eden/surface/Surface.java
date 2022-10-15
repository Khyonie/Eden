package coffee.khyonieheart.eden.surface;

import org.bukkit.Material;

import coffee.khyonieheart.eden.module.EdenModule;
import coffee.khyonieheart.eden.module.EdenModule.ModInfo;
import coffee.khyonieheart.eden.module.annotation.ModuleFamily;
import coffee.khyonieheart.eden.module.annotation.PreventUnload;
import coffee.khyonieheart.eden.module.java.enums.CallerToken;

@ModInfo (
    modName = "Surface",
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
