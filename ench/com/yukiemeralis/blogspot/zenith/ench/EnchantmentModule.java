package com.yukiemeralis.blogspot.zenith.ench;

import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule.ModInfo;

import org.bukkit.Material;

@ModInfo
(
    modName = "ZenithEnchants",
    description = "Handler for custom enchantments.",
    modFamily = "Zenith extra modules",
    modIcon = Material.ENCHANTED_BOOK,
    maintainer = "Yuki_emeralis",
    version = "1.0",
    supportedApiVersions = {"v1_17_R1"}
)
public class EnchantmentModule extends ZenithModule
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
