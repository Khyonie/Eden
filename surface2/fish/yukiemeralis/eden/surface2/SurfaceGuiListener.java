package fish.yukiemeralis.eden.surface2;

import fish.yukiemeralis.eden.surface2.component.GuiComponent;
import fish.yukiemeralis.eden.surface2.enums.TimeSpaceDistortionException;
import fish.yukiemeralis.eden.utils.Option;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class SurfaceGuiListener implements Listener 
{
    @EventHandler
    public void onClose(InventoryCloseEvent event)
    {
        Option<SurfaceGui> option = SurfaceGui.getOpenGui(event.getPlayer());
        SurfaceGui gui;

        switch (option.getState())
        {
            case NONE:
                return;
            case SOME:
                gui = option.unwrap();
                break;
            default:
                throw new TimeSpaceDistortionException();
        }

        gui.onGuiClose(event.getPlayer(), event.getView());

        SurfaceGui.setClosed(event.getPlayer());
    }

    @EventHandler
    public void onInteract(InventoryClickEvent event)
    {
        try {
            Option<SurfaceGui> option = SurfaceGui.getOpenGui(event.getWhoClicked());
            SurfaceGui gui;

            switch (option.getState())
            {
                case NONE:
                    return;
                case SOME:
                    gui = option.unwrap();
                    break;
                default:
                    throw new TimeSpaceDistortionException();
            }

            if (event.getRawSlot() >= gui.getSize())
                return; // Player inventory

            ItemStack clicked = gui.getViewItemAt(event.getWhoClicked(), event.getRawSlot());

            switch (gui.getDefaultClickAction())
            {
                case CANCEL:
                    event.setCancelled(true);
                    break;
                case NO_CANCEL:
                default:
                    break;
            }

            if (!gui.getAllowedClickActions().contains(event.getAction()))
                return; // Allowed click types

            if (clicked == null)
                return; // ItemStack is null

            if (gui.getData(event.getWhoClicked()).containsKey(event.getSlot()))
                ((GuiComponent) gui.getData(event.getWhoClicked()).get(event.getSlot())).onInteract(event);
        } catch (Exception e) {
            event.setCancelled(true);
        }
    }
}
