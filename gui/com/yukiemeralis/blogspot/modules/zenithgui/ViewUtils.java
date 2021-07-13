package com.yukiemeralis.blogspot.modules.zenithgui;

import com.yukiemeralis.blogspot.modules.zenithgui.base.InventoryGui;

import org.bukkit.inventory.InventoryView;

public class ViewUtils 
{
    public static void paintView(InventoryView view)
    {
        for (int i = 0; i < view.getTopInventory().getSize(); i++)
            view.setItem(i, InventoryGui.getBlankIcon());
    }
}
