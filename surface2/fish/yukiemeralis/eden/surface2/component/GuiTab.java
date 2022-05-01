package fish.yukiemeralis.eden.surface2.component;

import java.util.List;

import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.SurfaceGui;
import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder.SimpleComponentAction;
import fish.yukiemeralis.eden.surface2.special.TabbedSurfaceGui;
import fish.yukiemeralis.eden.utils.ItemUtils;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GuiTab implements GuiComponent
{
    private final GuiItemStack host;
    private final List<GuiComponent> guests;

    private final SimpleComponentAction action = (e) -> {
        TabbedSurfaceGui gui = (TabbedSurfaceGui) SurfaceGui.getOpenGui(e.getWhoClicked()).unwrap();
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

    @Override
    public void onInteract(InventoryClickEvent event)
    {
        host.onInteract(event);
    }
}
