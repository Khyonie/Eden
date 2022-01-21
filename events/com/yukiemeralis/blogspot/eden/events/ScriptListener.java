package com.yukiemeralis.blogspot.eden.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.yukiemeralis.blogspot.eden.events.triggers.EventTrigger;

public class ScriptListener implements Listener
{
    @EventHandler
    @SuppressWarnings("unchecked") // We sort triggers by event type, which does our checking for us
    public void onMove(PlayerMoveEvent event)
    {
        if (!EdenEvents.getActiveTriggers().containsKey(event.getClass()))
            return;
        EdenEvents.getActiveTriggers().get(event.getClass()).removeIf(trigger -> {
            if (!((EventTrigger<PlayerMoveEvent>) trigger).shouldTrigger(event))
                return false;
            
            ((EventTrigger<PlayerMoveEvent>) trigger).runEvent(event);
            return true;
        });
    }
}
