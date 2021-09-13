package com.yukiemeralis.blogspot.zenith.events;

import com.yukiemeralis.blogspot.zenith.events.triggers.EventTrigger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ScriptListener implements Listener
{
    @EventHandler
    @SuppressWarnings("unchecked") // We sort triggers by event type, which does our checking for us
    public void onMove(PlayerMoveEvent event)
    {
        if (!ZenithEvents.getActiveTriggers().containsKey(event.getClass()))
            return;
        ZenithEvents.getActiveTriggers().get(event.getClass()).removeIf(trigger -> {
            if (!((EventTrigger<PlayerMoveEvent>) trigger).shouldTrigger(event))
                return false;
            
            ((EventTrigger<PlayerMoveEvent>) trigger).runEvent(event);
            return true;
        });
    }
}
