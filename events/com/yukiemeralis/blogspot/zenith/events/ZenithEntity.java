package com.yukiemeralis.blogspot.zenith.events;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// TODO Move this to a dedicated module
// TODO Create a subclass for RPGs, with level ranges, stats, and stat scaling
/**
 * An instance of an entity, with equipment, potion effects, etc.
 */
public class ZenithEntity
{
    private EntityEquipment equipment;
    private List<Map<PotionEffectType, Integer[]>> potionEffects;
    private String name;
    private EntityType type;

    public void spawn(Location loc)
    {
        Entity entity = loc.getWorld().spawnEntity(loc, type);

        if (equipment != null && entity instanceof LivingEntity)
        {
            new SerializedEntityEquipment(equipment).toEquipment((LivingEntity) entity);
        }

        if (potionEffects.size() > 1 && entity instanceof LivingEntity)
            potionEffects.forEach(effect -> {
                effect.forEach((type, data) -> {
                    ((LivingEntity) entity).addPotionEffect(new PotionEffect(type, data[0], data[1]));
                });
            });

        if (name != null && entity instanceof LivingEntity)
        {
            entity.setCustomName(name);
            entity.setCustomNameVisible(true);
        }
    }

    public static class SerializedEntityEquipment
    {
        private Material
            mainHand,
            offHand,
            helmet,
            chestplate,
            leggings,
            boots
            ;

        public SerializedEntityEquipment(EntityEquipment equipment)
        {
            if (!equipment.getItemInMainHand().getType().isAir())
                mainHand = equipment.getItemInMainHand().getType();
            if (!equipment.getItemInOffHand().getType().isAir())
                offHand = equipment.getItemInOffHand().getType();
            if (!equipment.getHelmet().getType().isAir())
                helmet = equipment.getHelmet().getType();
            if (!equipment.getChestplate().getType().isAir())
                chestplate = equipment.getChestplate().getType();
            if (!equipment.getLeggings().getType().isAir())
                leggings = equipment.getLeggings().getType();
            if (!equipment.getBoots().getType().isAir())
                boots = equipment.getBoots().getType();
        }

        public EntityEquipment toEquipment(LivingEntity entity)
        {
            if (mainHand != null)
                entity.getEquipment().setItemInMainHand(new ItemStack(mainHand));
            if (offHand != null)
                entity.getEquipment().setItemInOffHand(new ItemStack(offHand));
            if (helmet != null)
                entity.getEquipment().setHelmet(new ItemStack(helmet));
            if (chestplate != null)
                entity.getEquipment().setChestplate(new ItemStack(chestplate));
            if (leggings != null)
                entity.getEquipment().setLeggings(new ItemStack(leggings));
            if (boots != null)
                entity.getEquipment().setBoots(new ItemStack(boots));

            return entity.getEquipment();
        }
    }
}
