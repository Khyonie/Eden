package com.yukiemeralis.blogspot.eden.events.triggers;

import java.util.List;

import com.yukiemeralis.blogspot.eden.Eden;
import com.yukiemeralis.blogspot.eden.events.EdenEvent;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TimeTrigger extends EventTrigger<Event>
{
    private final long timeInTicks;
    private List<Entity> targets; 
    private BukkitTask timerTask;

    public TimeTrigger(boolean synchronizeAttendees, int step, List<Entity> targets, long timeInTicks, EdenEvent... events)
    {
        super(synchronizeAttendees, step, events);
        this.timeInTicks = timeInTicks;
        this.targets = targets;
    }

    public void cancel()
    {
        if (timerTask != null)
            timerTask.cancel();
    }

    @Override
    public boolean shouldTrigger(Event event)
    {
        return true;
    }

    @Override
    public void runEvent(Event event)
    {
        timerTask = new BukkitRunnable() {
            @Override
            public void run()
            {
                if (this.isCancelled())
                    return;
                getAssociatedEvents().forEach(linkedEvent -> linkedEvent.trigger(targets.toArray(new Entity[targets.size()])));
            }
        }.runTaskLater(Eden.getInstance(), timeInTicks);
    }

    @Override
    public void clean()
    {
        timerTask.cancel();
        timerTask = null;
    }

    public void addTarget(Entity e)
    {
        targets.add(e);
    }
}
