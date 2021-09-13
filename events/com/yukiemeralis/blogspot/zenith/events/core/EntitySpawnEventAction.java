package com.yukiemeralis.blogspot.zenith.events.core;

import com.yukiemeralis.blogspot.modules.zenithgui.GuiItemStack;
import com.yukiemeralis.blogspot.zenith.events.ZenithEvent;
import com.yukiemeralis.blogspot.zenith.utils.ChatUtils;
import com.yukiemeralis.blogspot.zenith.utils.ItemUtils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EntitySpawnEventAction extends ZenithEvent
{
    Entity entity;
    Location location;

    public EntitySpawnEventAction(Location loc, Entity entity)
    {
        super("Spawn entity", "Spawns an entity at a location.", Material.ZOMBIE_HEAD);
    }

    public EntitySpawnEventAction(Location loc, EntityType type)
    {
        super("Spawn entity", "Spawns an entity at a location.", Material.ZOMBIE_HEAD);
        entity = loc.getWorld().spawnEntity(loc, type);
        entity.remove();
    }

    @Override
    public void trigger(Entity... targets)
    {
        //Entity ent = location.getWorld().spawn(location, entity.getClass());
        //EntityEquipment eq = ((LivingEntity) ent).getEquipment();
    }

    @Override
    public GuiItemStack toIcon()
    {
        GuiItemStack item = new GuiItemStack(Material.ZOMBIE_HEAD)
        {
            @Override
            public void onIconInteract(InventoryClickEvent event)
            {
                return;
            }   
        };

        ItemUtils.applyName(item, "§r§e§l" + this.getName());
        String locString = "None, right click to set.";
        if (this.location != null)
            locString = location.getWorld().getName() + ", (" + Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ()) + ")";

        String entString = "None, left click to set.";
        if (this.entity != null)
            entString = entity.getType().name();

        ItemUtils.applyLore(item, 
            "§r§7§o" + this.getDescription(),
            "",
            "§r" + ChatUtils.of("CCCCCC") + "Location:",
            "§r§7§o" + locString,
            "§r" + ChatUtils.of("CCCCCC") + "Type:",
            "§r§7§o" + entString
        );

        return item;
    }
}
