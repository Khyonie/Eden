package com.yukiemeralis.blogspot.eden.ench;

import com.yukiemeralis.blogspot.eden.gui.base.DynamicGui;
import com.yukiemeralis.blogspot.eden.module.java.annotations.HideFromCollector;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@HideFromCollector
public class EnchantGui extends DynamicGui
{
    public EnchantGui(Player player)
    {
        super(27, "Custom enchanting table", player, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF);
    }

    @Override
    public void init()
    {
        paint();

        getInventory().setItem(11, new ItemStack(Material.AIR));
        getInventory().setItem(12, new ItemStack(Material.AIR));
        for (int i = 0; i < 3; i++)
            getInventory().setItem((i * 9) + 6, new ItemStack(Material.AIR));
    }

    @Override
    @EventHandler
    public void onInteract(InventoryClickEvent event)
    {
        if (!event.getView().getTitle().equals(this.getInventoryName()))
            return;

        if (event.getClickedInventory() instanceof PlayerInventory)
        {
            if (!(event.isShiftClick() && event.getClickedInventory().getItem(event.getRawSlot()) != null))
                return;

            if (!event.getClickedInventory().getItem(event.getRawSlot()).getType().equals(Material.LAPIS_LAZULI))
                return;
            if (event.getView().getTopInventory().getItem(12).getType().equals(Material.LAPIS_LAZULI)) // Merge stacks
            {
                int total = event.getView().getTopInventory().getItem(12).getAmount() + event.getClickedInventory().getItem(event.getRawSlot()).getAmount();
                int remainder = total % 64;

                if (total == 128) // Both stacks are full
                {
                    event.setCancelled(true);
                    return;
                }

                if (total > 64)
                {
                    event.getView().getTopInventory().getItem(12).setAmount(64);
                } else {
                    event.getView().getTopInventory().getItem(12).setAmount(remainder);
                }

                event.getClickedInventory().getItem(event.getRawSlot()).setAmount(remainder);
                event.setCancelled(true);
            } else {
                event.getView().getTopInventory().setItem(12, event.getClickedInventory().getItem(event.getRawSlot()));
                event.getClickedInventory().setItem(event.getRawSlot(), new ItemStack(Material.AIR));
                event.setCancelled(true);
            }
                
            return;
        }

        switch (event.getRawSlot())
        {
            case 12: // Lapis slot
                if (event.getCursor().getType().equals(Material.LAPIS_LAZULI))
                return;
            default:
                event.setCancelled(true);
                return;
        }
    }
}
