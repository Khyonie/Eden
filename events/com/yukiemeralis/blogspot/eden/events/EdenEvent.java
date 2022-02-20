package com.yukiemeralis.blogspot.eden.events;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.yukiemeralis.blogspot.eden.gui.GuiComponent;

public abstract class EdenEvent implements GuiComponent
{
    private final String name, description;
    private final Material icon;
    private EventScript script;

    public EdenEvent(String name, String description, Material icon)
    {
        this.description = description;
        this.name = name;
        this.icon = icon;
    }

    public abstract void trigger(Entity... targets);

    public String getName()
    {
        return this.name;
    }

    public String getDescription()
    {
        return this.description;
    }

    public Material getMaterial()
    {
        return this.icon;
    }

    public void setScript(EventScript script)
    {
        this.script = script;
    }

    public EventScript getScript()
    {
        return this.script;
    }

    @Override
    public void onIconInteract(InventoryClickEvent event)
    {
        toIcon().onIconInteract(event);
    }
}
