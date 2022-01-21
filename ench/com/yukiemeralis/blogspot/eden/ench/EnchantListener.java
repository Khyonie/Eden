package com.yukiemeralis.blogspot.eden.ench;

import java.util.List;

import com.yukiemeralis.blogspot.eden.ench.enchantments.base.EdenEnchant;
import com.yukiemeralis.blogspot.eden.ench.enchantments.base.EdenEnchant.BlockBreakTrigger;
import com.yukiemeralis.blogspot.eden.ench.enchantments.base.EdenEnchant.EntityMeleeTrigger;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

public class EnchantListener implements Listener
{
    @EventHandler
    public void onEntityVsEntityDamage(EntityDamageByEntityEvent event)
    {
        if (!(event.getDamager() instanceof Player))
            return;

        ItemStack held = ((Player) event.getDamager()).getInventory().getItemInMainHand();
        if (held.getType().isAir())
            return;

        if (!ItemUtils.hasNamespacedKey(held, event.getClass().getSimpleName()))
            return;

        List<EdenEnchant> enchants = EdenEnchant.getEnchantments(held, event.getClass());
        enchants.forEach(ench -> {
            if (!(ench instanceof EntityMeleeTrigger))
                return;
            ((EntityMeleeTrigger) ench).onEntityMelee(event);
        });
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event)
    {
        if (!(event.getEntity().getShooter() instanceof Player))
            return;

        
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        ItemStack held = event.getPlayer().getInventory().getItemInMainHand();
        
        if (held.getType().isAir())
            return;
        
        if (!ItemUtils.hasNamespacedKey(held, event.getClass().getSimpleName()))
            return;

        List<EdenEnchant> enchants = EdenEnchant.getEnchantments(held, event.getClass());
        enchants.forEach(ench -> {
            if (!(ench instanceof BlockBreakTrigger))
                return;
            ((BlockBreakTrigger) ench).onBlockBreak(event);
        });
    }
}
