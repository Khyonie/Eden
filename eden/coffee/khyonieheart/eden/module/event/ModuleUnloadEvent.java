package coffee.khyonieheart.eden.module.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import coffee.khyonieheart.eden.module.EdenModule;

/**
 * Called when an Eden module is unloaded.
 * @author Yuki_emeralis
 */
public class ModuleUnloadEvent extends Event
{
    private static final HandlerList handlerList = new HandlerList();

    private final String filepath, name;
    private final EdenModule module;

    /**
     * @param name 
     * @param module
     * @param filepath  
     */
    public ModuleUnloadEvent(String name, EdenModule module, String filepath)
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
    public EdenModule getModule()
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
