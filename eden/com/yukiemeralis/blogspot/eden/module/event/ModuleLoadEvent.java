package com.yukiemeralis.blogspot.eden.module.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.yukiemeralis.blogspot.eden.module.EdenModule;

/**
 * Called when an Eden module is loaded into memory from its file.
 * @author Yuki_emeralis
 */
public class ModuleLoadEvent extends Event
{
    private static final HandlerList handlerList = new HandlerList();

    private final EdenModule module;
    private final String filepath;

    /**
     * @param module
     * @param filepath  
     */
    public ModuleLoadEvent(EdenModule module, String filepath)
    {
        this.module = module;
        this.filepath = filepath;
    }

    /** 
     * Obtains the module involved in this event.
     * @return The module that has been loaded.
     */
    public EdenModule getModule()
    {
        return this.module;
    }

    /**
     * Obtains the filepath for the module involved.
     * @return The filepath to the loaded module.
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
