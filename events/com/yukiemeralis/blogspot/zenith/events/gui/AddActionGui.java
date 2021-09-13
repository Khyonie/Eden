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
import com.yukiemeralis.blogspot.zenith.events.WrappedType;
import com.yukiemeralis.blogspot.zenith.events.ZenithEvent;
import com.yukiemeralis.blogspot.zenith.events.core.ChatEventAction;
import com.yukiemeralis.blogspot.zenith.events.core.EditBlockEventAction;
import com.yukiemeralis.blogspot.zenith.events.core.EntitySpawnEventAction;
import com.yukiemeralis.blogspot.zenith.events.core.TeleportEventAction;
import com.yukiemeralis.blogspot.zenith.events.triggers.EventTrigger;
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

public class AddActionGui extends PagedDynamicGui
{
    public AddActionGui(Player player, EventTrigger<?> trigger)
    { 
        super(5, "... > Edit trigger > Add action" , player, 0, genActionList(trigger), Arrays.asList(new GuiItemStack[] {GlobalScriptListGui.closeButton, genBackButton(trigger)}), InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);
    }

    private static List<GuiComponent> genActionList(EventTrigger<?> trigger)
    {
        List<GuiComponent> actions = new ArrayList<>(); 

        actions.add(new ActionAdapter(ChatEventAction.class, trigger, new WrappedType<String>(String.class, null)));
        actions.add(new ActionAdapter(EntitySpawnEventAction.class, trigger, new WrappedType<Location>(Location.class, null), new WrappedType<Entity>(Entity.class, null)));
        actions.add(new ActionAdapter(EditBlockEventAction.class, trigger, new WrappedType<Block>(Block.class, null), new WrappedType<Material>(Material.class, null)));
        actions.add(new ActionAdapter(TeleportEventAction.class, trigger, new WrappedType<Location>(Location.class, null)));

        return actions;
    }

    private static GuiItemStack genBackButton(EventTrigger<?> trigger)
    {
        GuiItemStack item = new GuiItemStack(Material.RED_CONCRETE)
        {
            @Override
            public void onIconInteract(InventoryClickEvent event)
            {
                new TriggerEditGui((Player) event.getWhoClicked(), trigger).display(event.getWhoClicked());
            }
        };

        ItemUtils.applyName(item, "§r§c§lBack");
        ItemUtils.applyLore(item, "§r§7§oGo back to the previous menu.");

        return item;
    }

    public static class ActionAdapter implements GuiComponent
    {
        private final Class<? extends ZenithEvent> action;
        private final EventTrigger<?> trigger;
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

        public ActionAdapter(Class<? extends ZenithEvent> action, EventTrigger<?> trigger, Object... defaultValues)
        {
            this.defaultValues = defaultValues;
            this.trigger = trigger;
            this.action = action;
        }

        @Override
        public GuiItemStack toIcon()
        {
            GuiItemStack item = new GuiItemStack(Material.PAPER)
            {
                @Override
                public void onIconInteract(InventoryClickEvent event)
                {
                    Class<?>[] params = new Class<?>[defaultValues.length];
                    for (int i = 0; i < defaultValues.length; i++)
                    {
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

                    ZenithEvent actionFinal;
                    try {
                        Constructor<?> constructor = action.getConstructor(params);
                        actionFinal = (ZenithEvent) constructor.newInstance(defaultValues);
                    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        PrintUtils.printPrettyStacktrace(e);
                        PrintUtils.sendMessage(event.getWhoClicked(), "An internal Java error has occurred. Please contact an administrator.");
                        return;
                    }

                    trigger.getAssociatedEvents().add(actionFinal);
                    PrintUtils.sendMessage(event.getWhoClicked(), "Successfully added a new action.");
                    ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    new TriggerEditGui((Player) event.getWhoClicked(), trigger).display(event.getWhoClicked());
                } 
            };

            ItemUtils.applyName(item, "§r§e§l" + action.getSimpleName());
            ItemUtils.applyLore(item, 
                "§r§7§o" + action.getPackageName(),
                "",
                "§r§aClick to add action to this trigger."
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
