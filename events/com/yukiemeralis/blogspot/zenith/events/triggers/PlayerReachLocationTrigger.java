package com.yukiemeralis.blogspot.zenith.events.triggers;

import com.yukiemeralis.blogspot.zenith.events.ZenithEvent;
import com.yukiemeralis.blogspot.zenith.events.ZenithEvents;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerReachLocationTrigger extends EventTrigger<PlayerMoveEvent>
{
    private final double radius;
    private final Location location;

    public PlayerReachLocationTrigger(boolean synchronizeAttendees, int step, Location location, double radius, ZenithEvent... events)
    {
        super(synchronizeAttendees, step, events);
        this.location = location;
        this.radius = radius;
    }

    @Override
    public boolean shouldTrigger(PlayerMoveEvent event)
    {
        if (!event.getPlayer().getWorld().equals(location.getWorld())) // Can't compare two different worlds
            return false;
        if (!this.getAssociatedEvents().get(0).getScript().equals(ZenithEvents.getEventAttendedBy(event.getPlayer()))) // Ensure the player is attending this script
            return false;
        if (event.getTo().distanceSquared(location) <= (radius * radius) && !this.getTriggeredAttendees().contains(event.getPlayer())) // Compare distance
            return true;
        return false;
    }

    @Override
    public void runEvent(PlayerMoveEvent event)
    {
        if (willSynchronizeAttendees())
        {
            this.getAssociatedEvents().get(0).getScript().synchronizeProgress(this.getStep());
            this.getAssociatedEvents().forEach(triggeredEvent -> triggeredEvent.trigger(this.getAssociatedEvents().get(0).getScript().getAttendees().toArray(new Entity[] {})));
            this.addTriggeredAttendee(this.getAssociatedEvents().get(0).getScript().getAttendees().toArray(new Player[] {}));
            return;
        }
        
        this.getAssociatedEvents().get(0).getScript().step(event.getPlayer());
        this.getAssociatedEvents().forEach(triggeredEvent -> triggeredEvent.trigger(event.getPlayer()));
        this.addTriggeredAttendee(event.getPlayer());
    }

    @Override
    public void clean()
    {
        this.getTriggeredAttendees().clear();
    }
}
