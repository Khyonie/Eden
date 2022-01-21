package com.yukiemeralis.blogspot.eden.ench.enchantments;

import java.util.HashMap;
import java.util.Map;

import com.yukiemeralis.blogspot.eden.Eden;
import com.yukiemeralis.blogspot.eden.ench.enchantments.base.EdenEnchant;
import com.yukiemeralis.blogspot.eden.ench.enchantments.base.EdenEnchant.BlockBreakTrigger;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class AutoSmelt extends EdenEnchant implements BlockBreakTrigger
{
    public AutoSmelt()
    {
        super("Autosmelt", "Smelts ore and raw materials.", BlockBreakEvent.class, CompatibleItem.PICKAXES);
    }

    Map<Material, Material> MATERIALS_AUTOSMELT = new HashMap<>()
    {{
        if (Eden.getNMSVersion().equals("v1_16_R3")) {
            put(Material.IRON_ORE, Material.IRON_INGOT);
            put(Material.GOLD_ORE, Material.GOLD_INGOT);
        } else if (Eden.getNMSVersion().equals("v1_17_R1") || Eden.getNMSVersion().equals("v1_18_R1")) {
            put(Material.IRON_ORE, Material.IRON_INGOT);
            put(Material.GOLD_ORE, Material.GOLD_INGOT);
            put(Material.COPPER_ORE, Material.COPPER_INGOT);
        }
    }};

    @Override
    public void onBlockBreak(BlockBreakEvent event)
    {
        ItemStack held = event.getPlayer().getInventory().getItemInMainHand();
        
        if (MATERIALS_AUTOSMELT.containsKey(event.getBlock().getType()))
        {
            event.setDropItems(false);
            event.getBlock().getWorld().dropItemNaturally(
                event.getBlock().getLocation(), 
                new ItemStack(
                    MATERIALS_AUTOSMELT.get(event.getBlock().getType()), 
                    event.getBlock().getDrops(held, event.getPlayer()).iterator().next().getAmount()
                )
            );
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }
}
