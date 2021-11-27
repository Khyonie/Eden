package com.yukiemeralis.blogspot.zenith.rustls;

import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule.ModInfo;

import org.bukkit.Material;

@ModInfo
(
    modName = "RustLS",
    description = "Language server for inter-language calls.",
    version = "1.0",
    modIcon = Material.CHAIN_COMMAND_BLOCK, 
	maintainer = "Yuki_emeralis", 
	supportedApiVersions = {"v1_16_R3", "v1_17_R1"}
)
public class RustModule extends ZenithModule
{
    private LanguageServer currentLanguageServer = null;

    @Override
    public void onEnable()
    {
        
    }

    @Override
    public void onDisable()
    {
        
    }
}