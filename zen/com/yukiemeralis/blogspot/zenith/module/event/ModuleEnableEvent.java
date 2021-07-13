package com.yukiemeralis.blogspot.zenith.module.event;

import com.yukiemeralis.blogspot.zenith.module.ZenithModule;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ModuleEnableEvent extends Event
{
    private static final HandlerList handlerList = new HandlerList();

    private final ZenithModule module;
    private final String filepath;

    public ModuleEnableEvent(ZenithModule module, String filepath)
    {
        this.module = module;
        this.filepath = filepath;
    }

    public ZenithModule getModule()
    {
        return this.module;
    }

    public String getFilepath()
    {
        return this.filepath;
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
