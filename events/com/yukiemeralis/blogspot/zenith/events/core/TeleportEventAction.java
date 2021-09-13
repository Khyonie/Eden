package com.yukiemeralis.blogspot.zenith.events.core;

import com.yukiemeralis.blogspot.modules.zenithgui.GuiItemStack;
import com.yukiemeralis.blogspot.zenith.events.ZenithEvent;
import com.yukiemeralis.blogspot.zenith.utils.ChatUtils;
import com.yukiemeralis.blogspot.zenith.utils.ItemUtils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TeleportEventAction extends ZenithEvent
{
    Location loc;

    public TeleportEventAction(Location loc)
    {
        super("Teleport entity", "Teleports an entity to a location.", Material.ENDER_PEARL);
        this.loc = loc;
    }

    @Override
    public GuiItemStack toIcon()
    {
        GuiItemStack item = new GuiItemStack(this.getMaterial())
        {
            @Override
            public void onIconInteract(InventoryClickEvent event)
            {
                
            }
        };

        ItemUtils.applyName(item, "§r§e§l" + this.getName());

        String locString = "Not set. Click to set a location.";
        if (loc != null)
            locString = loc.getWorld().getName() + ", (" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")";
        ItemUtils.applyLore(item, 
            "§r§7§o" + this.getDescription(), 
            "", 
            "§r" + ChatUtils.of("CCCCCC") + "Location:",
            "§r§7§o" + locString
        );

        return item;
    }

    @Override
    public void trigger(Entity... targets)
    {
        for (Entity e : targets)
            e.teleport(loc);
    }
    
}
