package coffee.khyonieheart.eden.surface;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import coffee.khyonieheart.eden.surface.component.GuiItemStack;
import coffee.khyonieheart.eden.utils.ItemUtils;

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