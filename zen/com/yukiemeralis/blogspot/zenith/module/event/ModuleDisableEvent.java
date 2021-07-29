package com.yukiemeralis.blogspot.zenith.module.event;

import com.yukiemeralis.blogspot.zenith.module.ZenithModule;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a Zenith module is disabled.
 * @author Yuki_emeralis
 */
public class ModuleDisableEvent extends Event
{
    private static final HandlerList handlerList = new HandlerList();

    private final ZenithModule module;
    private final String filepath;

    /**
     * @param module
     * @param filepath  
     */
    public ModuleDisableEvent(ZenithModule module, String filepath)
    {
        this.module = module;
        this.filepath = filepath;
    }

    /** 
     * Obtains the module involved in this event.
     * @return The module that has been disabled.
     */
    public ZenithModule getModule()
    {
        return this.module;
    }

    /**
     * Obtains the filepath for the module involved.
     * @return The filepath to the disabled module.
     */
    public String getFilepath()
    {
        return this.filepath;
    }

    /** @return HandlerList */
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
