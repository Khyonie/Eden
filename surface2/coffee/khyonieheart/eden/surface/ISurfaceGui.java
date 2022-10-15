package fish.yukiemeralis.eden.surface2;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;

public interface ISurfaceGui 
{
    /**
     * Initializes a new GUI with data pertaining to the given GUI.
     * @param e The human target.
     * @param view The view being initialized.
     */
    public void init(HumanEntity e, InventoryView view);

    /**
     * Runs once when the GUI is opened.
     * @param e The entity that opened this view.
     * @param view The view being opened.
     */
    public default void onGuiOpen(HumanEntity e, InventoryView view)
    {

    }

    /**
     * Runs once when the GUI is closed.
     * @param e The entity closing this view.
     * @param view The view being closed.
     */
    public default void onGuiClose(HumanEntity e, InventoryView view)
    {

    }
}
