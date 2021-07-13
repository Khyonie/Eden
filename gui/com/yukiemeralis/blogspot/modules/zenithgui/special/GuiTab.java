package com.yukiemeralis.blogspot.modules.zenithgui.special;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.yukiemeralis.blogspot.modules.zenithgui.GuiComponent;
import com.yukiemeralis.blogspot.modules.zenithgui.GuiItemStack;
import com.yukiemeralis.blogspot.modules.zenithgui.base.DynamicGui;
import com.yukiemeralis.blogspot.zenith.utils.ItemUtils;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiTab implements GuiComponent
{
    private final String label, prefix;
    private final Material material;

    private GuiItemStack icon = null;

    private List<GuiComponent> components;

    public GuiTab(String label, String prefix, Material material, GuiComponent... components)
    {
        this.label = label;
        this.prefix = prefix;
        this.material = material;

        List<GuiComponent> buffer = new ArrayList<>();
        buffer.addAll(Arrays.asList(components));
        this.components = buffer;

        toIcon();
    }

    public GuiTab(String label, Material material, GuiComponent... components)
    {
        this(label, "", material, components);
    }

    @Override
    public GuiItemStack toIcon() 
    {
        if (icon == null) {
            icon = new GuiItemStack(material)
            {
                @Override
                public void onIconInteract(InventoryClickEvent event) 
                {
                    DynamicGui gui = DynamicGui.getOpenedGuis().get(event.getWhoClicked());

                    TabbedDynamicGui oldtdg = (TabbedDynamicGui) gui;
                    TabbedDynamicGui newtdg = new TabbedDynamicGui(
                        oldtdg.getRowCount(),
                        oldtdg.getInventoryName(),
                        oldtdg.getPlayer(),
                        oldtdg.getTabData(),
                        ItemUtils.readFromNamespacedKey(this, "tabName"),
                        oldtdg.getAllowedActions().toArray(new InventoryAction[] {})
                    );

                    newtdg.display(event.getWhoClicked());
                }
            };

            ItemUtils.applyName(icon, prefix + label);
            ItemUtils.saveToNamespacedKey(icon, "tabName", label);
        };
        return icon;
    }

    @Override
    public void onIconInteract(InventoryClickEvent event) 
    {
        toIcon().onIconInteract(event);
    }

    public List<GuiComponent> getComponents()
    {
        return components;
    }

    public void removeComponent(GuiComponent component)
    {
        if (component == null)
            return;

        components.remove(component);
    }

    public void removeComponent(String componentName)
    {
        removeComponent(getComponentByExactName(componentName));
    }

    public GuiComponent getComponentByExactName(String name)
    {
        for (GuiComponent component : components)
        {
            if (component.toIcon().getItemMeta().getDisplayName().equals(name))
                return component;
        }

        return null;
    }

    public void addComponent(GuiComponent component)
    {
        components.add(component);
    }

    public void removeAllComponents()
    {
        this.components.clear();
    }
}
