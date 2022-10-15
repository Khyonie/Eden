package fish.yukiemeralis.eden.surface2.test;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;

import fish.yukiemeralis.eden.surface2.SimpleComponentBuilder;
import fish.yukiemeralis.eden.surface2.component.GuiComponent;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;
import fish.yukiemeralis.eden.surface2.special.PagedSurfaceGui;

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
