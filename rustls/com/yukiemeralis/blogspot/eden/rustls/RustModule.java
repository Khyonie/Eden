package com.yukiemeralis.blogspot.eden.rustls;

import org.bukkit.Material;

import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.module.EdenModule.ModInfo;

@ModInfo
(
    modName = "RustLS",
    description = "Language server for inter-language calls.",
    version = "1.0",
    modIcon = Material.CHAIN_COMMAND_BLOCK, 
	maintainer = "Yuki_emeralis", 
	supportedApiVersions = {"v1_16_R3", "v1_17_R1", "v1_18_R1"},
    modFamily = "Eden extra modules"
)
@SuppressWarnings("unused")
public class RustModule extends EdenModule
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