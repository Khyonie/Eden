package com.yukiemeralis.blogspot.eden.core;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.yukiemeralis.blogspot.eden.module.EdenModule;

public class EdenConfigChangeEvent extends Event
{
    private static final HandlerList handlerList = new HandlerList();

    private final String key, value, oldValue;
    private final EdenModule module;

    public EdenConfigChangeEvent(EdenModule module, String key, String oldValue, String value)
    {
        this.key = key;
        this.value = value;
        this.module = module;
        this.oldValue = oldValue;
    }

    public String getKey()
    {
        return key;
    }

    public String getValue()
    {
        return value;
    }

    public String getOldValue()
    {
        return oldValue;
    }
    
    public EdenModule getModule()
    {
        return module;
    }

    @Override
    public HandlerList getHandlers() 
    {
        return handlerList;
    }

    public static HandlerList getHandlerList()
    {
        return handlerList;
    }
}
