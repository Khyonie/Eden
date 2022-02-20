package com.yukiemeralis.blogspot.eden.gui.base;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yukiemeralis.blogspot.eden.gui.GuiComponent;
import com.yukiemeralis.blogspot.eden.module.java.annotations.HideFromCollector;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

@HideFromCollector
public abstract class DynamicGui extends InventoryGui
{
    private static Map<Player, DynamicGui> openedGuis = new HashMap<>();
    private final Player player;

    private final List<InventoryAction> allowedActions;

    private final Map<Integer, GuiComponent> components = new HashMap<>();

    public DynamicGui(int invSize, String invName, Player player, InventoryAction... allowedActions) 
    {
        super(invSize, invName);
        this.player = player;

        this.allowedActions = Arrays.asList(allowedActions);
    }

    public void addComponent(int slot, GuiComponent component)
    {
        this.components.put(slot, component);
    }

    public Map<Integer, GuiComponent> getComponents()
    {
        return this.components;
    }

    public void paintComponents()
    {
        components.forEach((slot, component) -> {
            try {
                getInventory().setItem(slot, component.toIcon());
            } catch (ArrayIndexOutOfBoundsException e) {}
        });
        
    }

    @Override
    public abstract void init();

    @Override
    public abstract void onInteract(InventoryClickEvent event);

    @Override
    public void display(HumanEntity target)
    {
        target.openInventory(this.getInventory());
        openedGuis.put((Player) target, this);
    }

    public Player getPlayer()
    {
        return player;
    }

    public List<InventoryAction> getAllowedActions()
    {
        return this.allowedActions;
    }

    public static Map<Player, DynamicGui> getOpenedGuis()
    {
        return openedGuis;
    }

    protected boolean isEventApplicable(InventoryClickEvent event, boolean cancel)
    {
        if (event.getClickedInventory() == null)
            return false;

        if (!event.getView().getTitle().equals(this.getInventoryName()))
            return false;

        if (cancel)
            event.setCancelled(true);

        if (!this.getAllowedActions().contains(event.getAction()))
            return false;

        return true;
    }
}
