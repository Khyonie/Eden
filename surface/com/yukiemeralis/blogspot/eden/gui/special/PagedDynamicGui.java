package com.yukiemeralis.blogspot.eden.gui.special;

import java.util.ArrayList;
import java.util.List;

import com.yukiemeralis.blogspot.eden.gui.GuiComponent;
import com.yukiemeralis.blogspot.eden.gui.GuiItemStack;
import com.yukiemeralis.blogspot.eden.gui.base.DynamicGui;
import com.yukiemeralis.blogspot.eden.module.java.annotations.HideFromCollector;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

@HideFromCollector
public class PagedDynamicGui extends DynamicGui
{
    private final int rowCount, page;
    private List<? extends GuiComponent> elements, topBar;

    private final static GuiItemStack next_page, back_page;
    static {
        next_page = new GuiItemStack(Material.PAPER) {
            @Override
            public void onIconInteract(InventoryClickEvent event) 
            {
                PagedDynamicGui oldgui = (PagedDynamicGui) DynamicGui.getOpenedGuis().get((Player) event.getWhoClicked());
                PagedDynamicGui newgui = new PagedDynamicGui(
                    oldgui.getRowCount(), 
                    oldgui.getInventoryName(), 
                    oldgui.getPlayer(), 
                    oldgui.getPage() + 1, 
                    oldgui.getListedElements(), 
                    oldgui.getAllowedActions().toArray(new InventoryAction[] {})
                );

                newgui.display(oldgui.getPlayer());
            }
        };

        back_page = new GuiItemStack(Material.MAP) {
            @Override
            public void onIconInteract(InventoryClickEvent event)
            {
                PagedDynamicGui oldgui = (PagedDynamicGui) DynamicGui.getOpenedGuis().get((Player) event.getWhoClicked());
                PagedDynamicGui newgui = new PagedDynamicGui(
                    oldgui.getRowCount(), 
                    oldgui.getInventoryName(), 
                    oldgui.getPlayer(), 
                    oldgui.getPage() - 1, 
                    oldgui.getListedElements(), 
                    oldgui.getAllowedActions().toArray(new InventoryAction[] {})
                );

                newgui.display(oldgui.getPlayer());
            }
        };

        ItemUtils.applyName(next_page, "§r§7§lNext page");
        ItemUtils.applyName(back_page, "§r§7§lBack page");
    }

    public PagedDynamicGui(int rowCount, String invName, Player player, int page, List<? extends GuiComponent> elements, InventoryAction... allowedActions) 
    {
        super(9 + (9 * rowCount), invName, player, allowedActions);
        this.rowCount = rowCount;
        this.page = page;
        
        this.elements = elements;

        init();
    }

    public PagedDynamicGui(int rowCount, String invName, Player player, int page, List<? extends GuiComponent> elements, List<? extends GuiComponent> topBar, InventoryAction... allowedActions) 
    {
        super(9 + (9 * rowCount), invName, player, allowedActions);
        this.rowCount = rowCount;
        this.page = page;
        
        this.elements = elements;

        if (topBar.size() > 7)
            throw new IllegalArgumentException("Top bar cannot contain more than 7 items.");

        this.topBar = topBar;

        init();
    }

    @Override
    public void init()
    {
        if (elements == null)  
            elements = new ArrayList<>();

        paint();

        if (page > 0)
            addComponent(7, back_page);
        if (((page + 1) * (9 * rowCount)) < elements.size()) 
            addComponent(8, next_page);

        for (int i = 9; i < 9 + (rowCount * 9);  i++)
        {
            try {
                GuiComponent component = elements.get((i - 9) + (page * (rowCount * 9)));
                addComponent(i, component);
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }

        if (topBar != null)
            for (int i = 0; i < topBar.size(); i++)
                addComponent(i, topBar.get(i));

        paintComponents();
    }

    @Override
    protected boolean isEventApplicable(InventoryClickEvent event, boolean cancel)
    {
        if (event.getClickedInventory() == null)
            return false;

        if (!DynamicGui.getOpenedGuis().containsKey((Player) event.getWhoClicked()))
            return false;

        DynamicGui gui = DynamicGui.getOpenedGuis().get((Player) event.getWhoClicked());

        if (!gui.getInventoryName().equals(event.getView().getTitle()))
            return false;

        if (!(gui instanceof PagedDynamicGui))
            return false;

        if (cancel)
            event.setCancelled(true);

        if (!gui.getAllowedActions().contains(event.getAction()))
            return false;

        return true;
    }

    @Override
    @EventHandler
    public void onInteract(InventoryClickEvent event)
    {
        if (!isEventApplicable(event, true))
            return;

        PagedDynamicGui gui = (PagedDynamicGui) DynamicGui.getOpenedGuis().get((Player) event.getWhoClicked());

        // In a GUI like this, we only care about components
        if (gui.getComponents().containsKey(event.getRawSlot()))
            gui.getComponents().get(event.getRawSlot()).onIconInteract(event);
    }

     public int getRowCount()
    {
        return this.rowCount;
    }

    public int getPage()
    {
        return this.page;
    }

    public List<? extends GuiComponent> getListedElements()
    {
        return this.elements;
    }
}
