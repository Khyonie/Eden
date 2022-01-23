package com.yukiemeralis.blogspot.eden.networking.repos;

import java.util.ArrayList;
import java.util.List;

import com.yukiemeralis.blogspot.eden.gui.GuiComponent;
import com.yukiemeralis.blogspot.eden.gui.GuiItemStack;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EdenRepository implements GuiComponent
{
    private String name;
    private List<EdenRepositoryEntry> entries; 

    public EdenRepository(String name)
    {
        this.name = name;
        this.entries = new ArrayList<>();
    }

    public String getName()
    {
        return this.name;
    }

    public List<EdenRepositoryEntry> getEntries()
    {
        return this.entries;
    }

    private GuiItemStack item;

    @Override
    public GuiItemStack toIcon() 
    {
        if (item != null)
            return item;
        item = new GuiItemStack(Material.BOOKSHELF) 
        {
            @Override
            public void onIconInteract(InventoryClickEvent event) 
            {
                // TODO Open repository details GUI   
            }
        };

        return item;
    }

    @Override
    public void onIconInteract(InventoryClickEvent event) 
    {
        if (item != null)
        {
            item.onIconInteract(event);
            return;
        }

        toIcon().onIconInteract(event);
    }
}
