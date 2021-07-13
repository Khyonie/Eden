package com.yukiemeralis.blogspot.zenith.module;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;

public class ModuleFamily 
{
    private static final Map<String, Material> familyIcons = new HashMap<>();
    private static final Map<String, String> familyDescs = new HashMap<>();

    public static void registerFamily(String name, Material material, String description)
    {
        familyIcons.put(name, material);
        familyDescs.put(name, description);
    }

    public static Material getIcon(String familyName)
    {
        return familyIcons.get(familyName);
    }

    public static String getDescription(String familyName)
    {
        return familyDescs.get(familyName);
    }

    public static Set<String> getAllFamilies()
    {
        return familyDescs.keySet();
    }
}
