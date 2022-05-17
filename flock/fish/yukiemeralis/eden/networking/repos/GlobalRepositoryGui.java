package fish.yukiemeralis.eden.networking.repos;

import fish.yukiemeralis.eden.networking.NetworkingModule;
import fish.yukiemeralis.eden.surface2.GuiUtils;
import fish.yukiemeralis.eden.surface2.component.GuiItemStack;
import fish.yukiemeralis.eden.surface2.enums.DefaultClickAction;
import fish.yukiemeralis.eden.surface2.special.PagedSurfaceGui;
import fish.yukiemeralis.eden.utils.ItemUtils;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GlobalRepositoryGui
{
    public void display(HumanEntity e)
    {
        new PagedSurfaceGui(54, "All repositories", e, 0, GuiUtils.of(NetworkingModule.getKnownRepositories().values()), GuiUtils.of(getCloseButton()), DefaultClickAction.CANCEL, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF).display(e);
    }

    public static GuiItemStack getCloseButton()
    {
        return new GuiItemStack(ItemUtils.build(Material.BARRIER, "§r§c§lClose", "§r§7§oClose this menu."))
        {
            @Override
            public void onInteract(InventoryClickEvent event)
            {
                event.getWhoClicked().closeInventory();
            }  
        };
    }
}
