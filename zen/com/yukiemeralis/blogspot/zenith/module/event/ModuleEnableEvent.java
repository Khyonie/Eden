package com.yukiemeralis.blogspot.zenith.module.event;

import com.yukiemeralis.blogspot.zenith.module.ZenithModule;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a Zenith module is enabled.
 * @author Yuki_emeralis
 */
public class ModuleEnableEvent extends Event
{
    private static final HandlerList handlerList = new HandlerList();

    private final ZenithModule module;
    private final String filepath;

    /**
     * @param module
     * @param filepath  
     */
    public ModuleEnableEvent(ZenithModule module, String filepath)
    {
        this.module = module;
        this.filepath = filepath;
    }

    /** 
     * Obtains the module involved in this event.
     * @return The module that has been enabled.
     */
    public ZenithModule getModule()
    {
        return this.module;
    }

    /**
     * Obtains the filepath for the module involved.
     * @return The filepath to the enabled module.
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
