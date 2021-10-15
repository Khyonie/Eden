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

/**
 * A collection of utilities to change an itemstack's metadata.
 * @Author Yuki_emeralis
 */
public class ItemUtils 
{
	/**
	 * Applies a name to the target itemstack.
	 * @param target
	 * @param name
	 */
    public static void applyName(ItemStack target, String name)
    {
        ItemMeta meta = target.getItemMeta();
        meta.setDisplayName(name);
        target.setItemMeta(meta);
    }

    /**
     * Applies lore to the target itemstack. Lore is variable-argument, each new string is a seperate line.
     * @param target
     * @param lore
     */
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

    /**
     * Applies an unsafe enchantment to an itemstack.
     * @param target
     * @param enchant
     * @param level
     */
    public static void applyEnchantment(ItemStack target, Enchantment enchant, int level)
    {
        target.addUnsafeEnchantment(enchant, level);
    }

    /**
     * Removes an enchantment from an itemstack.
     * @param target
     * @param enchant
     */
    public static void removeEnchantment(ItemStack target, Enchantment enchant)
    {
        target.removeEnchantment(enchant);
    }

    /**
     * Stores a string in an itemstack's persistent data container.
     * @param target
     * @param key
     * @param value
     */
    public static void saveToNamespacedKey(ItemStack target, String key, String value)
    {
        NamespacedKey nskey = new NamespacedKey((Plugin) Zenith.getInstance(), key);

        ItemMeta meta = target.getItemMeta();
        meta.getPersistentDataContainer().set(nskey, PersistentDataType.STRING, value);

        target.setItemMeta(meta);
    }

    /**
     * Reads a string from an itemstack's persistent data container.
     * @param target
     * @param key
     * @return
     */
    public static String readFromNamespacedKey(ItemStack target, String key)
    {
        NamespacedKey nskey = new NamespacedKey((Plugin) Zenith.getInstance(), key);

        ItemMeta meta = target.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        return container.has(nskey, PersistentDataType.STRING) ? container.get(nskey, PersistentDataType.STRING) : null;
    }

    public static PersistentDataContainer getDataContainer(ItemStack target)
    {
        return target.getItemMeta().getPersistentDataContainer();
    }

    /**
     * Obtains whether or not an itemstack has a specific key in a persistent data container.
     * @param target
     * @param key
     * @return
     */
    public static boolean hasNamespacedKey(ItemStack target, String key)
    {
        NamespacedKey nskey = new NamespacedKey((Plugin) Zenith.getInstance(), key);

        ItemMeta meta = target.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        return container.has(nskey, PersistentDataType.STRING);
    }

    public static List<String> getNamespacedKeysOfType(ItemStack target, PersistentDataType<?, ?> type)
    {
        return getNamespacedKeysOfType(target, type, "");
    }

    public static List<String> getNamespacedKeysOfType(ItemStack target, PersistentDataType<?, ?> type, String prefix)
    {
        List<String> buffer = new ArrayList<>();

        PersistentDataContainer container = getDataContainer(target);

        for (NamespacedKey key : container.getKeys())
        {
            if (!key.getKey().startsWith(prefix))
                continue;

            if (!container.has(key, type))
                continue;

            buffer.add(key.getKey());
        }

        return buffer;
    }

    /**
     * Convinience method that combines both {@link ItemUtils#applyName(ItemStack, String)} and {@link ItemUtils#applyLore(ItemStack, String...)}.
     * @param material
     * @param name
     * @param lore
     * @return An itemstack with the given material, and its metadata set to the given values.
     */
    public static ItemStack build(Material material, String name, String... lore)
    {
        ItemStack item = new ItemStack(material);

        applyName(item, name);
        applyLore(item, lore);

        return item;
    }
}
