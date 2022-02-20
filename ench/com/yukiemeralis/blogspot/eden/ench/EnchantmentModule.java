package com.yukiemeralis.blogspot.eden.ench;

import org.bukkit.Material;

import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.module.EdenModule.ModInfo;

@ModInfo
(
    modName = "EdenEnchants",
    description = "Handler for custom enchantments.",
    modFamily = "Eden extra modules",
    modIcon = Material.ENCHANTED_BOOK,
    maintainer = "Yuki_emeralis",
    version = "1.0",
    supportedApiVersions = {"v1_17_R1", "v1_18_R1"}
)
public class EnchantmentModule extends EdenModule
{
    public EnchantmentModule()
    {
        addListener(new EnchantGui(null));
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
