package coffee.khyonieheart.eden.surface.component;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import coffee.khyonieheart.eden.surface.SimpleComponentBuilder;
import coffee.khyonieheart.eden.surface.SurfaceGui;
import coffee.khyonieheart.eden.surface.SimpleComponentBuilder.SimpleComponentAction;
import coffee.khyonieheart.eden.surface.special.TabbedSurfaceGui;
import coffee.khyonieheart.eden.utils.ItemUtils;

public class GuiTab implements GuiComponent
{
    private final GuiItemStack host;
    private List<GuiComponent> guests;

    private final SimpleComponentAction action = (e) -> {
        TabbedSurfaceGui gui = (TabbedSurfaceGui) SurfaceGui.getOpenGui(e.getWhoClicked()).unwrap(SurfaceGui.class);
        gui.changeTab(e.getWhoClicked(), this);
    };

    public GuiTab(ItemStack host, List<GuiComponent> guests)
    {
        this.host = SimpleComponentBuilder.build(host, action);
        this.guests = guests;
    }

    public GuiTab(Material mat, String name, List<GuiComponent> guests, String... lore)
    {
        this(ItemUtils.build(mat, name, lore), guests);
    }

    public List<GuiComponent> getData()
    {
        return this.guests;
    }

    @Override
    public GuiItemStack generate()
    {
        return this.host;
    }

    public GuiTab regenerate()
    {
        List<GuiComponent> data = new ArrayList<>();

        for (GuiComponent comp : guests)
            data.add(comp.generate());

        guests = data;
        return this;
    }

    @Override
    public void onInteract(InventoryClickEvent event)
    {
        host.onInteract(event);
    }
}
