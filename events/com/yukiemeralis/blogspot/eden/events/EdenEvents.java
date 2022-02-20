package com.yukiemeralis.blogspot.eden.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yukiemeralis.blogspot.eden.events.triggers.EventTrigger;
import com.yukiemeralis.blogspot.eden.module.EdenModule;
import com.yukiemeralis.blogspot.eden.module.EdenModule.ModInfo;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

@ModInfo
(
    modName = "EdenEvents",
    description = "Handler for cutscene-like events involving players.",
    maintainer = "Yuki_emeralis",
    modFamily = "Eden extra modules",
    modIcon = Material.ANVIL,
    version = "1.0",
    supportedApiVersions = {"v1_16_R3", "v1_17_R1", "v1_18_R1"}
)
public class EdenEvents extends EdenModule
{
    private static Map<String, EventScript> ALL_SCRIPTS = new HashMap<>(); 
    private static Map<Player, EventScript> ACTIVE_EVENTS = new HashMap<>();
    private static Map<Class<? extends Event>, List<EventTrigger<?>>> ACTIVE_TRIGGERS = new HashMap<>();

    @Override
    public void onEnable()
    {
        
    }

    @Override
    public void onDisable()
    {
        
    }

    public static EventScript getEventAttendedBy(Player player)
    {
        return ACTIVE_EVENTS.get(player);
    }

    public static boolean isAttendingAnEvent(Player player)
    {
        return ACTIVE_EVENTS.containsKey(player);
    }

    public static void addAttendee(Player player, EventScript event, boolean allowOverwrite)
    {
        if (ACTIVE_EVENTS.containsKey(player) && !allowOverwrite)
            return;
        ACTIVE_EVENTS.put(player, event);
    }

    public static Map<Class<? extends Event>, List<EventTrigger<? extends Event>>> getActiveTriggers()
    {
        return ACTIVE_TRIGGERS;
    }

    public static Map<String, EventScript> getScripts()
    {
        return ALL_SCRIPTS;
    }
}
