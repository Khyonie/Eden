package com.yukiemeralis.blogspot.eden.core.itembuilder;

import java.util.List;

import com.yukiemeralis.blogspot.eden.gui.GuiComponent;
import com.yukiemeralis.blogspot.eden.gui.special.PagedDynamicGui;
import com.yukiemeralis.blogspot.eden.module.java.annotations.HideFromCollector;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;

@HideFromCollector
public class ItemGui extends PagedDynamicGui
{
    // TODO This feature
    public ItemGui(int rowCount, String invName, Player player, int page, List<? extends GuiComponent> elements, InventoryAction[] allowedActions) 
    {
        super(rowCount, invName, player, page, elements, allowedActions);
    }
}
