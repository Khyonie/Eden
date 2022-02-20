package com.yukiemeralis.blogspot.eden.gui;

import org.bukkit.inventory.InventoryView;

import com.yukiemeralis.blogspot.eden.gui.base.InventoryGui;

public class ViewUtils 
{
    public static void paintView(InventoryView view)
    {
        for (int i = 0; i < view.getTopInventory().getSize(); i++)
            view.setItem(i, InventoryGui.getBlankIcon());
    }
}
