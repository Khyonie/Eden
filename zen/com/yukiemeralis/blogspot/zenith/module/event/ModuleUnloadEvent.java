package com.yukiemeralis.blogspot.zenith.module.event;

import com.yukiemeralis.blogspot.zenith.module.ZenithModule;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a Zenith module is unloaded.
 * @author Yuki_emeralis
 */
public class ModuleUnloadEvent extends Event
{
    private static final HandlerList handlerList = new HandlerList();

    private final String filepath, name;
    private final ZenithModule module;

    /**
     * @param name 
     * @param module
     * @param filepath  
     */
    public ModuleUnloadEvent(String name, ZenithModule module, String filepath)
    {
        this.filepath = filepath;
        this.module = module;
        this.name = name;
    }

    /**
     * Obtains the filepath for the module involved.
     * @return The filepath to the unloaded module.
     */
    public String getFilepath()
    {
        return this.filepath;
    }

    /**
     * Obtains the name of the unloaded module.
     * @return The name of the unloaded module.
     */
    public String getName()
    {
        return this.name;
    }

    /** 
     * Obtains the module involved in this event.
     * @return The module that has been unloaded.
     */
    public ZenithModule getModule()
    {
        return this.module;
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
