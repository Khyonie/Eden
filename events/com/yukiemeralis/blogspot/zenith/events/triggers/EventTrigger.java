package com.yukiemeralis.blogspot.zenith.events.triggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.yukiemeralis.blogspot.modules.zenithgui.GuiComponent;
import com.yukiemeralis.blogspot.modules.zenithgui.GuiItemStack;
import com.yukiemeralis.blogspot.zenith.events.EventScript;
import com.yukiemeralis.blogspot.zenith.events.ZenithEvent;
import com.yukiemeralis.blogspot.zenith.events.gui.TriggerEditGui;
import com.yukiemeralis.blogspot.zenith.utils.ChatUtils;
import com.yukiemeralis.blogspot.zenith.utils.ItemUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;

public abstract class EventTrigger<T extends Event> implements GuiComponent
{
    private final List<ZenithEvent> event;
    private final boolean synchronizeAttendees;
    private List<Player> triggeredAttendees = new ArrayList<>();
    private int step;
    private EventScript script;

    public static enum TriggerType
    {
        RUN_ONCE_FOR_ALL_ATTENDEES,
        RUN_ONCE_FOR_EACH_ATTENDEE
        ;
    } 

    public EventTrigger(boolean synchronizeAttendees, int step, ZenithEvent... events)
    {
        this.synchronizeAttendees = synchronizeAttendees;
        this.event = new ArrayList<>(Arrays.asList(events));
        this.step = step;
    }

    public abstract boolean shouldTrigger(T event);
    public abstract void runEvent(T event);
    public abstract void clean();

    public void addTriggeredAttendee(Player... attendees)
    {
        if (attendees.length == 0)
        {
            PrintUtils.printPrettyStacktrace(new ArrayIndexOutOfBoundsException("Attempted to add 0 players to the triggered attendees list."));
            return;
        }

        triggeredAttendees.addAll(Arrays.asList(attendees));
    }

    public List<Player> getTriggeredAttendees()
    {
        return this.triggeredAttendees;
    }

    public List<ZenithEvent> getAssociatedEvents()
    {
        return this.event;
    }

    public boolean willSynchronizeAttendees()
    {
        return this.synchronizeAttendees;
    }

    public int getStep()
    {
        return this.step;
    }

    public void setStep(int step)
    {
        this.step = step;
    }

    private EventTrigger<T> getInstance()
    {
        return this;
    }

    public void setScript(EventScript script)
    {
        this.script = script;
    }

    public EventScript getAssociatedScript()
    {
        return this.script;
    }

    @Override
    public GuiItemStack toIcon()
    {
        GuiItemStack item = new GuiItemStack(Material.PAPER)
        {
            @Override
            public void onIconInteract(InventoryClickEvent event)
            {
                new TriggerEditGui((Player) event.getWhoClicked(), getInstance()).display(event.getWhoClicked());
            }
        };

        ItemUtils.applyName(item, "§r§e§l" + this.getClass().getSimpleName());

        List<String> lore = new ArrayList<>();
        lore.add(ChatUtils.of("CCCCCC") + "Actions:");
        for (ZenithEvent events : this.event)
            lore.add("§r§7" + events.getName());

        if (this.event.size() == 0)
            lore.add("§r§7§oNone, click to add actions.");

        ItemUtils.applyLore(item, lore.toArray(new String[lore.size()]));
        return item;
    }

    @Override
    public void onIconInteract(InventoryClickEvent event)
    {
        toIcon().onIconInteract(event);
    }
}