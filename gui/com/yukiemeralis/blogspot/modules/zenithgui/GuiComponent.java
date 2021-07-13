package com.yukiemeralis.blogspot.modules.zenithgui;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface GuiComponent
{
    /**
     * Turn this object into an itemstack icon to be displayed inside a GUI.
     */
    public GuiItemStack toIcon();

    public void onIconInteract(InventoryClickEvent event);
}
