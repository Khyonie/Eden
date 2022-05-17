package fish.yukiemeralis.eden.module.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EdenFinishLoadingEvent extends Event
{
    private static final HandlerList handlerList = new HandlerList();

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
