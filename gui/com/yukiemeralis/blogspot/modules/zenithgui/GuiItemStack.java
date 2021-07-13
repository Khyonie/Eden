package com.yukiemeralis.blogspot.modules.zenithgui;

import com.yukiemeralis.blogspot.zenith.utils.ItemUtils;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class GuiItemStack extends ItemStack implements GuiComponent
{
    public GuiItemStack(Material material)
    {
        super(material);
        ItemUtils.saveToNamespacedKey(this, "isGuiItemstack", "true");
    }

    public GuiItemStack(ItemStack itemstack)
    {
        super(itemstack);
        ItemUtils.saveToNamespacedKey(this, "isGuiItemstack", "true");
    }

    public GuiItemStack(Material material, int amount)
    {
        super(material, amount);
        ItemUtils.saveToNamespacedKey(this, "isGuiItemstack", "true");
    }

    @Override
    public abstract void onIconInteract(InventoryClickEvent event);

    @Override
    public GuiItemStack toIcon()
    {
        return this;
    }
}
