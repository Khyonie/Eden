package com.yukiemeralis.blogspot.eden.ench.enchantments;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.yukiemeralis.blogspot.eden.ench.enchantments.base.EdenEnchant;
import com.yukiemeralis.blogspot.eden.ench.enchantments.base.EdenEnchant.EntityMeleeTrigger;

public class Explosion extends EdenEnchant implements EntityMeleeTrigger
{
    public Explosion()
    {
        super("Explosion", "Causes a small explosion on hit", EntityDamageByEntityEvent.class, CompatiblePreset.MELEE.getData());
    }

    @Override
    public void onEntityMelee(EntityDamageByEntityEvent event)
    {
        event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(), 1);
    }
}
