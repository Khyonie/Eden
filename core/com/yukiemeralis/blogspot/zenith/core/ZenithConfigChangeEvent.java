package com.yukiemeralis.blogspot.zenith.core;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.yukiemeralis.blogspot.zenith.module.ZenithModule;

public class ZenithConfigChangeEvent extends Event
{
    private static final HandlerList handlerList = new HandlerList();

    private final String key, value, oldValue;
    private final ZenithModule module;

    public ZenithConfigChangeEvent(ZenithModule module, String key, String oldValue, String value)
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
    
    public ZenithModule getModule()
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
