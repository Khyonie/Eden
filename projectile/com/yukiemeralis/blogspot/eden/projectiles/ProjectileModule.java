package com.yukiemeralis.blogspot.eden.projectiles;

import org.bukkit.Material;

import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.module.EdenModule.ModInfo;
import com.yukiemeralis.blogspot.eden.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.eden.module.java.enums.PreventUnload;

@ModInfo(
    modName = "EdenProjectiles",
    description = "Handler for custom projectiles.",
    version = "1.0",
    maintainer = "Yuki_emeralis",
    modIcon = Material.SPECTRAL_ARROW,
    modFamily = "Eden base modules",
    supportedApiVersions = {"v1_16_R3", "v1_17_R1", "v1_18_R1"}
)
@PreventUnload(CallerToken.EDEN)
public class ProjectileModule extends EdenModule
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
