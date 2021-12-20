package com.yukiemeralis.blogspot.zenith.specialprojectile;

import org.bukkit.Material;

import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule.ModInfo;
import com.yukiemeralis.blogspot.zenith.module.java.enums.CallerToken;
import com.yukiemeralis.blogspot.zenith.module.java.enums.PreventUnload;

@ModInfo(
    modName = "ZenithProjectiles",
    description = "Handler for custom projectiles.",
    version = "1.0",
    maintainer = "Yuki_emeralis",
    modIcon = Material.SPECTRAL_ARROW,
    modFamily = "Zenith base modules",
    supportedApiVersions = {"v1_16_R3", "v1_17_R1", "v1_18_R1"}
)
@PreventUnload(CallerToken.ZENITH)
public class ProjectileModule extends ZenithModule
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
