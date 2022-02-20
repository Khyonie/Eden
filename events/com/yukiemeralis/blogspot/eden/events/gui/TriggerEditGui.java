package com.yukiemeralis.blogspot.eden.events.gui;

import java.util.ArrayList;
import java.util.Arrays;

import com.yukiemeralis.blogspot.eden.events.EventScript;
import com.yukiemeralis.blogspot.eden.events.triggers.EventTrigger;
import com.yukiemeralis.blogspot.eden.gui.GuiComponent;
import com.yukiemeralis.blogspot.eden.gui.GuiItemStack;
import com.yukiemeralis.blogspot.eden.gui.special.PagedDynamicGui;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TriggerEditGui extends PagedDynamicGui
{
    public TriggerEditGui(Player player, EventTrigger<?> trigger)
    {
        super(
            5, 
            "... > Edit script > Edit trigger", 
            player, 
            0, 
            new ArrayList<GuiComponent>(trigger.getAssociatedEvents()) {{
                GuiItemStack item = new GuiItemStack(Material.LIME_CONCRETE)
                {
                    @Override
                    public void onIconInteract(InventoryClickEvent event)
                    {
                        new AddActionGui((Player) event.getWhoClicked(), trigger).display(event.getWhoClicked());
                    }
                };

                ItemUtils.applyName(item, "§r§a§lAdd action");
                ItemUtils.applyLore(item, "§r§7§oClick to add an action to", "§r§7§othis trigger.");
                add(item);
            }}, 
            Arrays.asList(new GuiItemStack[] {GlobalScriptListGui.closeButton, genBackButton(trigger.getAssociatedScript())}),
            InventoryAction.PICKUP_ALL, 
            InventoryAction.PICKUP_HALF
        );
    }   

    private static GuiItemStack genBackButton(EventScript script)
    {
        GuiItemStack item = new GuiItemStack(Material.RED_CONCRETE)
        {
            @Override
            public void onIconInteract(InventoryClickEvent event)
            {
                new ScriptEditGui((Player) event.getWhoClicked(), script).display(event.getWhoClicked());
            }
        };

        ItemUtils.applyName(item, "§r§c§lBack");
        ItemUtils.applyLore(item, "§r§7§oGo back to the previous menu.");

        return item;
    }
}
