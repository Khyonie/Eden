package com.yukiemeralis.blogspot.zenith.ench.enchantments;

import com.yukiemeralis.blogspot.zenith.ench.enchantments.base.ZenithEnchant;
import com.yukiemeralis.blogspot.zenith.ench.enchantments.base.ZenithEnchant.EntityMeleeTrigger;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Explosion extends ZenithEnchant implements EntityMeleeTrigger
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
