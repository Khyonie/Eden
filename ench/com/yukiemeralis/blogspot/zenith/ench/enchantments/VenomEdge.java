package com.yukiemeralis.blogspot.zenith.ench.enchantments;

import com.yukiemeralis.blogspot.zenith.ench.enchantments.base.ZenithEnchant;
import com.yukiemeralis.blogspot.zenith.ench.enchantments.base.ZenithEnchant.EntityMeleeTrigger;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VenomEdge extends ZenithEnchant implements EntityMeleeTrigger
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
