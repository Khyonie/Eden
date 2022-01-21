package com.yukiemeralis.blogspot.eden.events.gui;

import java.util.ArrayList;
import java.util.Arrays;

import com.yukiemeralis.blogspot.eden.events.EventScript;
import com.yukiemeralis.blogspot.eden.gui.GuiComponent;
import com.yukiemeralis.blogspot.eden.gui.GuiItemStack;
import com.yukiemeralis.blogspot.eden.gui.special.PagedDynamicGui;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ScriptEditGui extends PagedDynamicGui
{
    public ScriptEditGui(Player player, EventScript script)
    {
        super(
            5, 
            "All scripts > Edit script", 
            player, 
            0, 
            new ArrayList<GuiComponent>(script.getTriggers()) {{
                GuiItemStack newTrigger = new GuiItemStack(Material.LIME_CONCRETE)
                {
                    @Override
                    public void onIconInteract(InventoryClickEvent event)
                    {
                        new AddTriggerGui(player, script).display(player);
                    }
                };

                ItemUtils.applyName(newTrigger, "§r§a§lNew trigger");
                ItemUtils.applyLore(newTrigger, "§r§7§oClick to add a trigger", "§r§7§oto this script.");
                
                add(newTrigger);
            }}, 
            Arrays.asList(
                new GuiItemStack[] 
                {
                    GlobalScriptListGui.closeButton,
                    genBackButton()
                }
            ),
            InventoryAction.PICKUP_ALL, 
            InventoryAction.PICKUP_HALF
        );
    }

    private static GuiItemStack genBackButton()
    {
        GuiItemStack item = new GuiItemStack(Material.RED_CONCRETE)
        {
            @Override
            public void onIconInteract(InventoryClickEvent event)
            {
                new GlobalScriptListGui((Player) event.getWhoClicked()).display(event.getWhoClicked());
            }
        };

        ItemUtils.applyName(item, "§r§c§lBack");
        ItemUtils.applyLore(item, "§r§7§oGo back to the previous menu.");

        return item;
    }
}
