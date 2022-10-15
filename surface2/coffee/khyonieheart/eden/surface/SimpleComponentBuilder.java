package fish.yukiemeralis.eden.surface2;

import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.utils.ItemUtils;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SimpleComponentBuilder 
{
    public static GuiItemStack build(Material m, String name, SimpleComponentAction action, String... lore)
    {
        return build(ItemUtils.build(m, name, lore), action);
    }

    public static GuiItemStack build(ItemStack item, SimpleComponentAction action)
    {
        return new GuiItemStack(item)
        {
            public void onInteract(InventoryClickEvent event)
            {
                action.onInteract(event);
            }
        };
    }

    public static interface SimpleComponentAction
    {
        public void onInteract(InventoryClickEvent event);
    }
}