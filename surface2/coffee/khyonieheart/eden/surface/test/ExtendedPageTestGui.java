package coffee.khyonieheart.eden.surface.test;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;

import coffee.khyonieheart.eden.surface.SimpleComponentBuilder;
import coffee.khyonieheart.eden.surface.component.GuiComponent;
import coffee.khyonieheart.eden.surface.component.GuiItemStack;
import coffee.khyonieheart.eden.surface.enums.DefaultClickAction;
import coffee.khyonieheart.eden.surface.special.PagedSurfaceGui;

public class ExtendedPageTestGui extends PagedSurfaceGui 
{
    private static GuiItemStack component = SimpleComponentBuilder.build(Material.BOOK, "Component", (event) -> {});

    public ExtendedPageTestGui(HumanEntity target) 
    {
        super(54, "Page test", target, 0, generateListOfSize(200), DefaultClickAction.CANCEL, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);
    }
    
    private static List<? extends GuiComponent> generateListOfSize(int size)
    {
        List<GuiComponent> list = new ArrayList<>(size); 

        for (int i = 0; i < size; i++)
            list.add(component);

        return list;
    }
}
