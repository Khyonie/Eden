package com.yukiemeralis.blogspot.zenith.ench.enchantments.base;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yukiemeralis.blogspot.zenith.utils.ItemUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.Result;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;
import com.yukiemeralis.blogspot.zenith.utils.Result.UndefinedResultException;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ZenithEnchant 
{
    private final String name, description;
    private final Class<? extends Event> applicableEvent;
    private final CompatibleItem[] compatibleWith;
    private int level;
    private int loreLine;

    public static enum CompatibleItem
    {
        SWORDS(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD),
        TRIDENTS(Material.TRIDENT),
        BOWS(Material.BOW),
        CROSSBOWS(Material.CROSSBOW),
        CROSSBOW_WITH_FIREWORKS(Material.CROSSBOW),
        AXES(Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE),
        PICKAXES(Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE),
        SHOVELS(Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL),
        HOES(Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE),
        HELMETS(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET),
        CHESTPLATES(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE),
        LEGGINGS(Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS),
        BOOTS(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS),
        SHIELDS(Material.SHIELD)
        ;

        CompatibleItem(Material... materials)
        {
            this.materials = materials;
        }

        Material[] materials;

        public Material[] getMaterials()
        {
            return this.materials;
        }

        public boolean isCompatible(Material input)
        {
            for (Material m : materials)
                if (m.equals(input))
                    return true;
            return false;
        }
    }

    public static enum CompatiblePreset
    {
        MELEE(CompatibleItem.SWORDS, CompatibleItem.AXES, CompatibleItem.TRIDENTS),
        ALL_ARMOR(CompatibleItem.HELMETS, CompatibleItem.CHESTPLATES, CompatibleItem.LEGGINGS, CompatibleItem.BOOTS),
        ALL(CompatibleItem.values()),
        ALL_TOOLS(CompatibleItem.PICKAXES, CompatibleItem.AXES, CompatibleItem.SHOVELS, CompatibleItem.HOES)
        ;

        CompatiblePreset(CompatibleItem... data)
        {
            this.data = data;
        }

        final CompatibleItem[] data;

        public CompatibleItem[] getData()
        {
            return this.data;
        }
    }

    @SafeVarargs
    public ZenithEnchant(String name, String description, Class<? extends Event> applicableEvent, CompatibleItem... compatibleWith)
    {
        this.name = name;
        this.description = description;
        this.compatibleWith = compatibleWith;
        this.applicableEvent = applicableEvent;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public int getLoreLine()
    {
        return this.loreLine;
    }

    public void setLoreLine(int loreLine)
    {
        this.loreLine = loreLine;
    }

    public int getLevel()
    {
        return this.level;
    }

    public String getName()
    {
        return this.name;
    }

    public String getDescription()
    {
        return this.description;
    }

    public CompatibleItem[] getCompatibleItems()
    {
        return this.compatibleWith;
    }

    public Class<? extends Event> getEvent()
    {
        return this.applicableEvent;
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + "," + level + "," + loreLine;
    }

    public static List<ZenithEnchant> getEnchantments(ItemStack item, Class<? extends Event> event)
    {
        if (!ItemUtils.hasNamespacedKey(item, event.getSimpleName()))
            return null;

        return csvToEnchList(ItemUtils.readFromNamespacedKey(item, event.getSimpleName()));
    } 

    public static Result<Boolean, String> apply(ItemStack item, ZenithEnchant enchant, int level)
    {
        // Enchantments are formatted as such:
        // Key - "EntityDamageByEntityEvent"
        // Value - "enchClassName,tier,loreLine:enchClassName,tier,loreLine"

        // Items with custom enchants will contain a custom "lore stack pointer" number, that shows how many enchants are applied (n-1),
        // and thus how much to offset new lore lines

        Result<Boolean, String> result = new Result<>(Boolean.class, String.class);

        enchant.setLevel(level);

        if (!ItemUtils.hasNamespacedKey(item, "hasZenithEnchant"))
            ItemUtils.saveToNamespacedKey(item, "hasZenithEnchant", "0"); // Lore stack pointer

        List<String> lore = item.getItemMeta().getLore();

        // Apply the enchant to the item
        if (!ItemUtils.hasNamespacedKey(item, enchant.getEvent().getSimpleName()))
        {
            ItemUtils.saveToNamespacedKey(item, enchant.getEvent().getSimpleName(), enchant.toString());
        } else {
            List<ZenithEnchant> enchants = csvToEnchList(ItemUtils.readFromNamespacedKey(item, enchant.getEvent().getSimpleName()));

            // Prime the itemstack
            List<String> loreEnclosing = new ArrayList<>(lore);

            // We don't want to add an enchant that already exists on the item, or overwrite a higher level enchant with a lower one
            enchants.removeIf(e -> {
                if (enchant.getClass().equals(e.getClass()) && e.getLevel() < enchant.getLevel())
                {
                    ItemUtils.saveToNamespacedKey(item, "hasZenithEnchant", (Integer.parseInt(ItemUtils.readFromNamespacedKey(item, "hasZenithEnchant")) - 1) + "");
                    PrintUtils.log("Replacing lore line " + e.getLoreLine());
                    loreEnclosing.remove(e.getLoreLine());
                    return true;
                }
                return false;
            });

            lore = loreEnclosing;

            for (ZenithEnchant e : enchants)
            {
                if (e.getClass().equals(enchant.getClass()) && e.getLevel() > enchant.getLevel())
                {
                    result.err("Cannot overwrite higher level enchantment.");
                    return result;
                }

                if (e.getClass().equals(enchant.getClass()) && e.getLevel() == enchant.getLevel())
                {
                    result.err("Item already has this exact enchant; assignment has no effect.");
                    return result;
                }
            }

            enchants.add(enchant);
            ItemUtils.saveToNamespacedKey(item, enchant.getEvent().getSimpleName(), enchListToCsv(enchants));
        }

        // Lore settings
        enchant.setLoreLine(Integer.parseInt(ItemUtils.readFromNamespacedKey(item, "hasZenithEnchant")));
        ItemMeta meta = item.getItemMeta();
        if (lore == null) 
            lore = new ArrayList<>();
        lore.add(enchant.getLoreLine(), "§r§7" + enchant.getName() + " " + enchant.getLevel());
        meta.setLore(lore);
        item.setItemMeta(meta);

        // Update lore stack pointer
        ItemUtils.saveToNamespacedKey(item, "hasZenithEnchant", (Integer.parseInt(ItemUtils.readFromNamespacedKey(item, "hasZenithEnchant")) + 1) + "");

        result.ok(true);
        return result;
    }

    private static String enchListToCsv(List<ZenithEnchant> enchants)
    {
        StringBuilder builder = new StringBuilder();
        enchants.forEach(ench -> {
            builder.append(":" + ench.toString());
        });
        builder.deleteCharAt(0);
        return builder.toString();
    }

    private static List<ZenithEnchant> csvToEnchList(String csv)
    {
        List<ZenithEnchant> enchBuffer = new ArrayList<>();

        if (csv.length() == 0)
            return enchBuffer;

        String[] allData = csv.split(":");
        Map<String, String[]> data = new HashMap<>();

        String[] buffer;
        for (String str : allData)
        {
            PrintUtils.log("Parsing enchant string: \"" + str + "\".");
            buffer = str.split(",");
            data.put(buffer[0], new String[] {buffer[1], buffer[2]});
        }

        
        data.forEach((clazz, level) -> {
            ZenithEnchant ench = null;

            Result<ZenithEnchant, String> result = getEnchantByClassname(clazz);
            switch (result.getState())
            {
                case OK:
                    try {
                        ench = (ZenithEnchant) result.unwrap();
                        ench.setLevel(Integer.parseInt(data.get(clazz)[0]));
                        ench.setLoreLine(Integer.parseInt(data.get(clazz)[1]));
                    } catch (UndefinedResultException e) {
                        PrintUtils.log("Failed to generate enchant object by classname \"" + clazz + "\".", InfoType.ERROR);
                        return;
                    }
                    break;
                case ERR:
                    try {
                        PrintUtils.log("Failed to generate enchant object by classname \"" + clazz + "\".", InfoType.ERROR);
                        PrintUtils.log("Reason: \"" + (String) result.unwrap() + "\".");
                        return;
                    } catch (UndefinedResultException e) {}  
                    break;
            }

            enchBuffer.add(ench);
        });

        return enchBuffer;
    }

    @SuppressWarnings("unchecked") // Everything in the given package will extend ZenithEnchant
    public static Result<ZenithEnchant, String> getEnchantByClassname(String className)
    {
        Result<ZenithEnchant, String> result = new Result<>(ZenithEnchant.class, String.class);

        ZenithEnchant enchant;
        try {
            Class<? extends ZenithEnchant> enchClass = (Class<? extends ZenithEnchant>) Class.forName("com.yukiemeralis.blogspot.zenith.ench.enchantments." + className);
            enchant = enchClass.getConstructor().newInstance();

            if (enchant != null)
                result.ok(enchant);
        } catch (ClassNotFoundException e) {
            result.err("Could not find enchantment.");
        } catch (NoSuchMethodException e) {
            result.err("Enchantment class is invalid.");
        } catch (InstantiationException | SecurityException | IllegalAccessException | InvocationTargetException e) {
            result.err("General java error: " + e.getCause().getClass().getSimpleName());
        }

        return result;
    }

    public boolean isApplicable(Class<? extends Event> event)
    {
        return applicableEvent.equals(event);
    }

    public static interface EntityMeleeTrigger // Also contains shield blocking
    {
        public void onEntityMelee(EntityDamageByEntityEvent event);
    }

    public static interface BlockBreakTrigger
    {
        public void onBlockBreak(BlockBreakEvent event);
    }

    public static interface ProjectileShootTrigger
    {
        public void onProjectileShoot(ProjectileLaunchEvent event);
    }
}
