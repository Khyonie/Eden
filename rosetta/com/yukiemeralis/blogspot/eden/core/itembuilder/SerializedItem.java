package com.yukiemeralis.blogspot.eden.core.itembuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.yukiemeralis.blogspot.eden.utils.ItemUtils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SerializedItem 
{
    @Expose
    private String name;

    @Expose
    private String[] lore;

    @Expose
    private int 
        count,
        durability;

    @Expose
    private Material material;

    @Expose
    private boolean unbreakable = false;

    @Expose
    public Map<String, String> persistentData = new HashMap<>();

    @Expose
    public Map<Enchantment, Integer> enchantments = new HashMap<>(); 

    private ItemStack item = null;

    public SerializedItem() {}

    public ItemStack toItemStack()
    {
        if (item != null)
            return item;

        item = new ItemStack(material, count);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        
        meta.setUnbreakable(unbreakable);

        persistentData.forEach((key, value) -> {
            ItemUtils.saveToNamespacedKey(item, key, value);
        });

        return item;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setLore(String... lore)
    {
        this.lore = lore;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public void setDurability(int durability)
    {
        this.durability = durability;
    }

    public void setMaterial(Material material)
    {
        this.material = material;
    }

    public void setUnbreakable(boolean unbreakable)
    {
        this.unbreakable = unbreakable;
    }

    public void addPersistentData(String key, String value)
    {
        persistentData.put(key, value);
    }

    public String getName()
    {
        return this.name;
    }

    public String[] getLore()
    {
        return this.lore;
    }

    public int getCount()
    {
        return this.count;
    }

    public int getDurability()
    {
        return this.durability;
    }

    public boolean getUnbreakable()
    {
        return this.unbreakable;
    }

    public Map<String, String> getPersistentData()
    {
        return this.persistentData;
    }
}
