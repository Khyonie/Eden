package com.yukiemeralis.blogspot.modules.zenithgui.test;

import com.yukiemeralis.blogspot.modules.zenithgui.GuiComponent;
import com.yukiemeralis.blogspot.modules.zenithgui.GuiItemStack;
import com.yukiemeralis.blogspot.zenith.utils.ItemUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ComponentTest implements GuiComponent
{
    private final Material mat;
    private final String name;

    private GuiItemStack item = null;;

    public ComponentTest(Material mat, String name)
    {
        this.mat = mat;
        this.name = "§r§e§l" + name;
    }

    @Override
    public GuiItemStack toIcon() 
    {
        if (item == null)
        {
            item = new GuiItemStack(mat)
            {
                @Override
                public void onIconInteract(InventoryClickEvent event) 
                {
                    PrintUtils.sendMessage(event.getWhoClicked(), "Clicked button!");
                }
            };

            ItemUtils.applyName(item, name);
        }

        return item;
    }

    @Override
    public void onIconInteract(InventoryClickEvent event) 
    {
        this.toIcon().onIconInteract(event);
    }
    
}