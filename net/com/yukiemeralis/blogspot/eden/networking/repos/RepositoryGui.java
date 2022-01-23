package com.yukiemeralis.blogspot.eden.networking.repos;

import java.util.Arrays;
import com.yukiemeralis.blogspot.eden.gui.GuiItemStack;
import com.yukiemeralis.blogspot.eden.gui.special.PagedDynamicGui;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RepositoryGui extends PagedDynamicGui 
{
    public RepositoryGui(Player player, EdenRepository repo) 
    {
        super(5, repo.getName() + ": " + repo.getEntries().size(), player, 0, repo.getEntries(), Arrays.asList(new GuiItemStack[] {getDlAllButton()}), InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);
    }

    private static GuiItemStack getDlAllButton()
    {
        return new GuiItemStack(ItemUtils.build(Material.ENDER_CHEST, "§r§a§lDownload all", "§r§7§oDownloads all modules inside", "§r§7§othis repository.", "", "§r§bClick to perform this action.")) 
        {
            @Override
            public void onIconInteract(InventoryClickEvent event) 
            {
                
            }
        };       
    }
}
