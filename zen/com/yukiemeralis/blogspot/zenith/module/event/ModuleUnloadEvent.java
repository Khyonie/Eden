package com.yukiemeralis.blogspot.zenith.module.event;

import com.yukiemeralis.blogspot.zenith.module.ZenithModule;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ModuleUnloadEvent extends Event
{
    private static final HandlerList handlerList = new HandlerList();

    private final String filepath, name;
    private final ZenithModule module;

    public ModuleUnloadEvent(String name, ZenithModule module, String filepath)
    {
        this.filepath = filepath;
        this.module = module;
        this.name = name;
    }

    public String getFilepath()
    {
        return this.filepath;
    }

    public String getName()
    {
        return this.name;
    }

    public ZenithModule getModule()
    {
        return this.module;
    }

    public static HandlerList getHandlerList()
    {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() 
    {
        return handlerList;
    }
}
