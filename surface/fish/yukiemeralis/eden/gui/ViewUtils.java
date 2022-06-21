package fish.yukiemeralis.eden.gui;

import org.bukkit.inventory.InventoryView;

import fish.yukiemeralis.eden.gui.base.InventoryGui;

/**
 * @deprecated Deprecated in favor of Surface2's GuiUtils class.
 */
@Deprecated
public class ViewUtils 
{
    public static void paintView(InventoryView view)
    {
        for (int i = 0; i < view.getTopInventory().getSize(); i++)
            view.setItem(i, InventoryGui.getBlankIcon());
    }
}
