package com.yukiemeralis.blogspot.zenith.events.gui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yukiemeralis.blogspot.modules.zenithgui.GuiComponent;
import com.yukiemeralis.blogspot.modules.zenithgui.GuiItemStack;
import com.yukiemeralis.blogspot.modules.zenithgui.special.PagedDynamicGui;
import com.yukiemeralis.blogspot.zenith.events.EventScript;
import com.yukiemeralis.blogspot.zenith.events.WrappedType;
import com.yukiemeralis.blogspot.zenith.events.ZenithEvent;
import com.yukiemeralis.blogspot.zenith.events.triggers.BlockInteractTrigger;
import com.yukiemeralis.blogspot.zenith.events.triggers.EventTrigger;
import com.yukiemeralis.blogspot.zenith.events.triggers.PlayerReachLocationTrigger;
import com.yukiemeralis.blogspot.zenith.events.triggers.TimeTrigger;
import com.yukiemeralis.blogspot.zenith.events.triggers.TrueTrigger;
import com.yukiemeralis.blogspot.zenith.utils.ItemUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class AddTriggerGui extends PagedDynamicGui
{
    public AddTriggerGui(Player player, EventScript script)
    {
        super(
            5, 
            "... > Edit script > Add trigger", 
            player, 
            0, 
            genTriggerList(script),
            Arrays.asList(new GuiItemStack[] {GlobalScriptListGui.closeButton, genBackButton(script)}),
            InventoryAction.PICKUP_ALL,
            InventoryAction.PICKUP_HALF
        );
    }

    private static List<GuiComponent> genTriggerList(EventScript script)
    {
        List<GuiComponent> triggers = new ArrayList<>();

        triggers.add(new TriggerAdapter(script, PlayerReachLocationTrigger.class, false, -1, (Location) new Location(null, 0, 0, 0), 0.0d, new ZenithEvent[0])); // boolean, int, Location, double, ZenithEvent...
        triggers.add(new TriggerAdapter(script, TimeTrigger.class, false, -1, new ArrayList<Entity>(), 0l, new ZenithEvent[0])); // boolean, int, List<Entity>, long, ZenithEvent...
        triggers.add(new TriggerAdapter(script, BlockInteractTrigger.class, false, -1, new WrappedType<Block>(Block.class, null), new ZenithEvent[0]));
        triggers.add(new TriggerAdapter(script, TrueTrigger.class, false, -1, new ArrayList<Entity>(), new ZenithEvent[0]));

        return triggers;
    }

    private static GuiItemStack genBackButton(EventScript script)
    {
        GuiItemStack item = new GuiItemStack(Material.RED_CONCRETE)
        {
            @Override
            public void onIconInteract(InventoryClickEvent event)
            {
                new ScriptEditGui((Player) event.getWhoClicked(), script).display(event.getWhoClicked());
            }
        };

        ItemUtils.applyName(item, "§r§c§lBack");
        ItemUtils.applyLore(item, "§r§7§oGo back to the previous menu.");

        return item;
    }
    
    private static class TriggerAdapter implements GuiComponent
    {
        private final EventScript script;
        private final Class<? extends EventTrigger<?>> trigger;
        private final Object[] defaultValues;

        private final static Map<Class<?>, Class<?>> primitiveObjectMappings = new HashMap<>()
        {{
            put(Byte.class, Byte.TYPE);
            put(Short.class, Short.TYPE);
            put(Integer.class, Integer.TYPE);
            put(Long.class, Long.TYPE);
            put(Float.class, Float.TYPE);
            put(Double.class, Double.TYPE);
            put(Boolean.class, Boolean.TYPE);

            // Not primitives but 
            put(ArrayList.class, List.class);
            put(HashMap.class, Map.class);
        }};

        public TriggerAdapter(EventScript script, Class<? extends EventTrigger<?>> trigger, Object... defaultValues)
        {
            this.script = script;
            this.trigger = trigger;
            this.defaultValues = defaultValues;
        }

        @Override
        public GuiItemStack toIcon()
        {
            GuiItemStack item = new GuiItemStack(Material.BOOK)
            {
                @Override
                @SuppressWarnings("unchecked")
                public void onIconInteract(InventoryClickEvent event)
                {
                    // Copy over default argument classes
                    Class<?>[] params = new Class<?>[defaultValues.length];
                    for (int i = 0; i < defaultValues.length; i++)
                    {
                        // If a wrapped type is being used, handle accordingly
                        if (defaultValues[i] instanceof WrappedType) 
                        {
                            params[i] = ((WrappedType<?>) defaultValues[i]).getType();
                            defaultValues[i] = ((WrappedType<?>) defaultValues[i]).getWrappedObject();
                            continue;
                        }

                        if (primitiveObjectMappings.containsKey(defaultValues[i].getClass()))
                        {
                            params[i] = primitiveObjectMappings.get(defaultValues[i].getClass());
                            continue;
                        }
                        params[i] = defaultValues[i].getClass();
                    }

                    // Instantiate
                    EventTrigger<?> triggerFinal;
                    try {
                        Constructor<EventTrigger<?>> constructor = (Constructor<EventTrigger<?>>) trigger.getConstructor(params);
                        triggerFinal = constructor.newInstance(defaultValues);

                        triggerFinal.setStep(script.getTriggers().size());
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
                        PrintUtils.printPrettyStacktrace(e);
                        PrintUtils.sendMessage(event.getWhoClicked(), "An internal Java error has occurred. Please contact an administrator.");
                        return;
                    }

                    triggerFinal.setScript(script);

                    script.getTriggers().add(triggerFinal);
                    PrintUtils.sendMessage(event.getWhoClicked(), "Successfully added a new trigger.");
                    ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    new ScriptEditGui((Player) event.getWhoClicked(), script).display(event.getWhoClicked());
                } 
            };

            ItemUtils.applyName(item, "§r§e§l" + trigger.getSimpleName());
            ItemUtils.applyLore(item, 
                "§r§7§o" + trigger.getPackageName(),
                "",
                "§r§aClick to add trigger to this script."
            );

            return item;
        }

        @Override
        public void onIconInteract(InventoryClickEvent event)
        {
            toIcon().onIconInteract(event);
        }
    }
}
