package fish.yukiemeralis.eden.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fish.yukiemeralis.eden.gui.base.DynamicGui;

@Deprecated
public class GuiListener implements Listener
{
    @EventHandler
    public void onClose(InventoryCloseEvent event)
    {
        if (DynamicGui.getOpenedGuis().containsKey((Player) event.getPlayer()))
        {
            DynamicGui.getOpenedGuis().remove((Player) event.getPlayer());
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event)
    {
        DynamicGui.getOpenedGuis().remove(event.getPlayer());
    }
}
