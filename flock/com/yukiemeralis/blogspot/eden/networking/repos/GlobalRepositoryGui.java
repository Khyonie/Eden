package com.yukiemeralis.blogspot.eden.networking.repos;

import java.util.ArrayList;
import java.util.Arrays;

import com.yukiemeralis.blogspot.eden.gui.GuiItemStack;
import com.yukiemeralis.blogspot.eden.gui.special.PagedDynamicGui;
import com.yukiemeralis.blogspot.eden.networking.NetworkingModule;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GlobalRepositoryGui extends PagedDynamicGui 
{
    public GlobalRepositoryGui(Player player) 
    {
        super(5, "All repositories", player, 0, new ArrayList<>(NetworkingModule.getKnownRepositories().values()), Arrays.asList(new GuiItemStack[] {getCloseButton()}), InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);
    }

    public static GuiItemStack getCloseButton()
    {
        return new GuiItemStack(ItemUtils.build(Material.BARRIER, "§r§c§lClose", "§r§7§oClose this menu."))
        {
            @Override
            public void onIconInteract(InventoryClickEvent event)
            {
                event.getWhoClicked().closeInventory();
            }  
        };
    }
}
