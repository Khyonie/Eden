package com.yukiemeralis.blogspot.zenith.ench.enchantments;

import java.util.ArrayList;
import java.util.List;

import com.yukiemeralis.blogspot.zenith.ench.enchantments.base.ZenithEnchant;
import com.yukiemeralis.blogspot.zenith.ench.enchantments.base.ZenithEnchant.EntityMeleeTrigger;

import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Waterhunter extends ZenithEnchant implements EntityMeleeTrigger
{
    public Waterhunter()
    {
        super("Waterhunter", "Increases damage dealt to aquatic mobs.", EntityDamageByEntityEvent.class, CompatibleItem.SWORDS);
    }

    private static final List<EntityType> AQUATIC_MOBS = new ArrayList<>() {{
        add(EntityType.DOLPHIN);
        add(EntityType.COD);
        add(EntityType.PUFFERFISH);
        add(EntityType.SALMON);
        add(EntityType.SQUID);
        add(EntityType.TROPICAL_FISH);
        add(EntityType.TURTLE);
        add(EntityType.DROWNED);
        add(EntityType.ELDER_GUARDIAN);
        add(EntityType.GUARDIAN);
    }};

    @Override
    public void onEntityMelee(EntityDamageByEntityEvent event)
    {
        if (AQUATIC_MOBS.contains(event.getEntityType()))
            event.setDamage(event.getDamage() * (1.25 * getLevel()));
    }
}
