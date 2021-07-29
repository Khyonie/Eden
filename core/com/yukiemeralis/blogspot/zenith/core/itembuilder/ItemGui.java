package com.yukiemeralis.blogspot.zenith.core.itembuilder;

import java.util.List;

import com.yukiemeralis.blogspot.modules.zenithgui.GuiComponent;
import com.yukiemeralis.blogspot.modules.zenithgui.special.PagedDynamicGui;
import com.yukiemeralis.blogspot.zenith.module.java.annotations.HideFromCollector;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;

@HideFromCollector
public class ItemGui extends PagedDynamicGui
{
    public ItemGui(int rowCount, String invName, Player player, int page, List<? extends GuiComponent> elements, InventoryAction[] allowedActions) 
    {
        super(rowCount, invName, player, page, elements, allowedActions);
    }
}
