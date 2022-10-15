package coffee.khyonieheart.eden.surface.component;

import org.bukkit.event.inventory.InventoryClickEvent;

import coffee.khyonieheart.eden.surface.GuiUtils;

public interface GuiComponent 
{
    public default GuiItemStack generate()
    {
        if (GuiUtils.hasCachedComponent(this))
            return GuiUtils.getCachedComponent(this);
        return null;
    }

    public default void onInteract(InventoryClickEvent event)
    {
        // Do nothing
    }
}
