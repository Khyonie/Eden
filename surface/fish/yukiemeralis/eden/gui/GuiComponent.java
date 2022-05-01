package fish.yukiemeralis.eden.gui;

import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Allows a class to be included as an itemstack in an Eden GUI.
 * @author Yuki_emeralis
 * @since 1.0
 * 
 * @deprecated Deprecated in favor of Surface 2 implementation.
 */
@Deprecated
public interface GuiComponent
{
    /**
     * Turn this object into an itemstack icon to be displayed inside a GUI.
     * @return This object, as an itemstack
     */
    public GuiItemStack toIcon();

    /**
     * Code to be executed when this itemstack is interacted with.
     * @param event The inventory click event
     */
    public void onIconInteract(InventoryClickEvent event);
}
