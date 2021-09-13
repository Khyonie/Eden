package com.yukiemeralis.blogspot.zenith.events.triggers;

import java.util.List;

import com.yukiemeralis.blogspot.zenith.events.ZenithEvent;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

public class TrueTrigger extends EventTrigger<Event>
{
    private List<Entity> targets;

    public TrueTrigger(boolean synchronizeAttendees, int step, List<Entity> targets, ZenithEvent... events) 
    {
        super(synchronizeAttendees, step, events);
        this.targets = targets;
    }

    public void addTarget(Entity e)
    {
        targets.add(e);
    }

    @Override
    public boolean shouldTrigger(Event event)
    {
        return true;
    }

    @Override
    public void runEvent(Event event)
    {
        for (ZenithEvent e : this.getAssociatedEvents())
            e.trigger(targets.toArray(new Entity[targets.size()]));
    }

    @Override
    public void clean()
    {
        targets.clear();
    }
}
