package fish.yukiemeralis.eden.surface2.component;

import fish.yukiemeralis.eden.surface2.GuiUtils;

import org.bukkit.event.inventory.InventoryClickEvent;

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
