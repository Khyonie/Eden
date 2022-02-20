package com.yukiemeralis.blogspot.eden.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yukiemeralis.blogspot.eden.events.gui.ScriptEditGui;
import com.yukiemeralis.blogspot.eden.events.triggers.EventTrigger;
import com.yukiemeralis.blogspot.eden.gui.GuiComponent;
import com.yukiemeralis.blogspot.eden.gui.GuiItemStack;
import com.yukiemeralis.blogspot.eden.utils.ChatUtils;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;
import com.yukiemeralis.blogspot.eden.utils.Option;
import com.yukiemeralis.blogspot.eden.utils.PrintUtils;
import com.yukiemeralis.blogspot.eden.utils.Result;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EventScript implements GuiComponent
{
    private String name;
    private List<EventTrigger<? extends Event>> events = new ArrayList<>();
    private Map<Player, Integer> attendees = new HashMap<>(); // Keeps track of player event script progression
    private int maxAttendees = 0;
    private boolean 
        allowJoinAfterStart = false,
        started = false;

    public EventScript(String name)
    {
        this.name = name;
    }

    public void start()
    {
        events.get(0).runEvent(null);
        started = false;
    }

    public void finish()
    {
        events.forEach(event -> event.clean());
        attendees.clear();
    }

    public Map<Player, Integer> getAttendeesAndProgress()
    {
        return this.attendees;
    }

    public Collection<Player> getAttendees()
    {
        return attendees.keySet();
    }

    public List<EventTrigger<? extends Event>> getTriggers()
    {
        return this.events;
    }

    /**
     * Performs a quick check to see if all triggers in this script have at least one action associated with them.
     * @return An option where a contained value repesents all the step values of invald events.  
     */
    public Option<List<Integer>> isValid()
    {
        Option<List<Integer>> result = new Option<>(List.class);
        List<Integer> invalidActions = new ArrayList<>();

        for (EventTrigger<?> trigger : events)
            if (trigger.getAssociatedEvents().size() == 0)
                invalidActions.add(trigger.getStep());

        if (invalidActions.size() > 0)
            result.some(invalidActions);

        return result;
    }

    public Result<Boolean, String> addAttendee(Player player)
    {
        Result<Boolean, String> result = new Result<>(Boolean.class, String.class);

        if (attendees.size() >= maxAttendees && maxAttendees != 0)
        {
            result.err("This event has reached its maximum number of players");
            return result;
        }

        if (started && !allowJoinAfterStart)
        {
            result.err("Cannot join this event, as it has already started");
            return result;
        }

        result.ok(true);
        attendees.put(player, 0);
        EdenEvents.addAttendee(player, this, true);

        return result;
    }

    public void step(Player player)
    {
        attendees.replace(player, attendees.get(player), attendees.get(player) + 1);
    }

    public void synchronizeProgress(int step)
    {
        if (events.size() - 1 > step)
        {
            PrintUtils.printPrettyStacktrace(new IndexOutOfBoundsException("Attempted to synchronize script progress to step beyond script length. (Step " + (step + 1) + ", max " + events.size() + ")"));
            return;
        }

        attendees.replaceAll((player, step_) -> step_ = step);
    }

    @Override
    public GuiItemStack toIcon()
    {
        GuiItemStack item = new GuiItemStack(Material.FILLED_MAP)
        {
            @Override
            public void onIconInteract(InventoryClickEvent event)
            {
                String scriptName = ItemUtils.readFromNamespacedKey(this, "scriptName");
                new ScriptEditGui((Player) event.getWhoClicked(), EdenEvents.getScripts().get(scriptName)).display(event.getWhoClicked());
            }
        };

        ItemUtils.applyName(item, "§r§e§lScript: \"" + this.name + "\"");

        List<String> eventLore = new ArrayList<>();
        eventLore.add(ChatUtils.of("CCCCCC") + "Triggers:");

        int index = 0;
        for (EventTrigger<?> t : this.events)
        {
            eventLore.add("§r§7- " + t.getClass().getSimpleName() + " (" + t.getAssociatedEvents().size() + " action(s))");

            if (index > 14)
            {
                eventLore.add("§r§7§o" + (this.events.size() - 15) + " more...");
                break;
            }
            index++;
        }

        ItemUtils.applyLore(item, eventLore.toArray(new String[eventLore.size()]));
        ItemUtils.saveToNamespacedKey(item, "scriptName", this.name);
        return item;
    }

    @Override
    public void onIconInteract(InventoryClickEvent event)
    {
        toIcon().onIconInteract(event);
    }
}
