package com.yukiemeralis.blogspot.zenith.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import com.yukiemeralis.blogspot.zenith.Zenith;

public class ItemUtils 
{
    public static void applyName(ItemStack target, String name)
    {
        ItemMeta meta = target.getItemMeta();
        meta.setDisplayName(name);
        target.setItemMeta(meta);
    }

    public static void applyLore(ItemStack target, String... lore)
    {
        ItemMeta meta = target.getItemMeta();
        List<String> buffer  = new ArrayList<>();
        for (String str : lore)
            for (String spl : str.split("\\\\n"))
                buffer.add(spl);

        meta.setLore(buffer);

        target.setItemMeta(meta);
    }

    public static void applyEnchantment(ItemStack target, Enchantment enchant, int level)
    {
        target.addUnsafeEnchantment(enchant, level);
    }

    public static void removeEnchantment(ItemStack target, Enchantment enchant)
    {
        target.removeEnchantment(enchant);
    }

    public static void saveToNamespacedKey(ItemStack target, String key, String value)
    {
        NamespacedKey nskey = new NamespacedKey((Plugin) Zenith.getInstance(), key);

        ItemMeta meta = target.getItemMeta();
        meta.getPersistentDataContainer().set(nskey, PersistentDataType.STRING, value);

        target.setItemMeta(meta);
    }

    public static String readFromNamespacedKey(ItemStack target, String key)
    {
        NamespacedKey nskey = new NamespacedKey((Plugin) Zenith.getInstance(), key);

        ItemMeta meta = target.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        return container.has(nskey, PersistentDataType.STRING) ? container.get(nskey, PersistentDataType.STRING) : null;
    }

    public static boolean hasNamespacedKey(ItemStack target, String key)
    {
        NamespacedKey nskey = new NamespacedKey((Plugin) Zenith.getInstance(), key);

        ItemMeta meta = target.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        return container.has(nskey, PersistentDataType.STRING);
    }

    public static ItemStack build(Material material, String name, String... lore)
    {
        ItemStack item = new ItemStack(material);

        applyName(item, name);
        applyLore(item, lore);

        return item;
    }
}
