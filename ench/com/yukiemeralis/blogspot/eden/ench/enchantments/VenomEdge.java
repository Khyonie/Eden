package com.yukiemeralis.blogspot.eden.ench.enchantments;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.yukiemeralis.blogspot.eden.ench.enchantments.base.EdenEnchant;
import com.yukiemeralis.blogspot.eden.ench.enchantments.base.EdenEnchant.EntityMeleeTrigger;

public class VenomEdge extends EdenEnchant implements EntityMeleeTrigger
{
    public VenomEdge()
    {
        super("Venom edge", "Inflicts poison on the enemy.", EntityDamageByEntityEvent.class, CompatibleItem.SWORDS);
    }

    @Override
    public void onEntityMelee(EntityDamageByEntityEvent event)
    {
        ((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40*getLevel(), 1));
    }
}
