package com.yukiemeralis.blogspot.zenith.ench;

import java.util.List;

import com.yukiemeralis.blogspot.zenith.ench.enchantments.base.ZenithEnchant;
import com.yukiemeralis.blogspot.zenith.ench.enchantments.base.ZenithEnchant.BlockBreakTrigger;
import com.yukiemeralis.blogspot.zenith.ench.enchantments.base.ZenithEnchant.EntityMeleeTrigger;
import com.yukiemeralis.blogspot.zenith.utils.ItemUtils;

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

        List<ZenithEnchant> enchants = ZenithEnchant.getEnchantments(held, event.getClass());
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

        List<ZenithEnchant> enchants = ZenithEnchant.getEnchantments(held, event.getClass());
        enchants.forEach(ench -> {
            if (!(ench instanceof BlockBreakTrigger))
                return;
            ((BlockBreakTrigger) ench).onBlockBreak(event);
        });
    }
}
