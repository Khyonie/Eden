package com.yukiemeralis.blogspot.eden.events.gui;

import java.util.ArrayList;
import java.util.Arrays;

import com.yukiemeralis.blogspot.eden.events.EdenEvents;
import com.yukiemeralis.blogspot.eden.gui.GuiItemStack;
import com.yukiemeralis.blogspot.eden.gui.special.PagedDynamicGui;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GlobalScriptListGui extends PagedDynamicGui
{
    protected final static GuiItemStack closeButton = new GuiItemStack(Material.BARRIER)
    {
        @Override
        public void onIconInteract(InventoryClickEvent event)
        {
            event.getWhoClicked().closeInventory();
        }
    };

    static {
        ItemUtils.applyName(closeButton, "§r§c§lClose");
    }

    public GlobalScriptListGui(Player player)
    {
        super(5, "All scripts", player, 0, new ArrayList<>(EdenEvents.getScripts().values()), Arrays.asList(new GuiItemStack[] {GlobalScriptListGui.closeButton}), InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);
    }
}
