package com.yukiemeralis.blogspot.eden.core.eventtasks;

import org.bukkit.event.Event;

import com.yukiemeralis.blogspot.eden.module.java.annotations.Unimplemented;

@Unimplemented
public abstract class EventScheduledTask<T extends Event> implements Runnable
{
    public abstract void onTrigger(T event);
    public abstract boolean isApplicable(Event event);

    private T event;
    private Class<? extends Event> eventClass;
    private boolean isCancelled = false;

    public EventScheduledTask(Class<T> eventClass)
    {
        this.eventClass = eventClass;
    }

    @Override
    public void run()
    {
        if (!isCancelled)
            this.onTrigger(event);
    }

    public void cancel()
    {
        this.isCancelled = true;
    }

    // We can ignore the unchecked cast as a situation where a ClassCastException will never occur.
    // T must be an event of some kind, and any potential issues would be caught long before this.
    @SuppressWarnings("unchecked")
    synchronized void setEvent(Event event)
    {
        if (!eventClass.isAssignableFrom(event.getClass()))
            return;
        this.event = (T) event;
    }

    public synchronized void schedule()
    {
        EventScheduler.schedule(this, eventClass);
    }
}
