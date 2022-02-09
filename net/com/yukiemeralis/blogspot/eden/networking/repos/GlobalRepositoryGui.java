package com.yukiemeralis.blogspot.eden.networking.repos;

import java.util.ArrayList;
import com.yukiemeralis.blogspot.eden.gui.special.PagedDynamicGui;
import com.yukiemeralis.blogspot.eden.networking.NetworkingModule;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;

public class GlobalRepositoryGui extends PagedDynamicGui 
{
    public GlobalRepositoryGui(Player player) 
    {
        super(5, "All repositories", player, 0, new ArrayList<>(NetworkingModule.getKnownRepositories().values()), InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);
    }
}
