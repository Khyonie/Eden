package com.yukiemeralis.blogspot.zenith.specialprojectile;

import java.util.HashMap;
import java.util.Map;

import com.yukiemeralis.blogspot.zenith.Zenith;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Projectile;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

public class ProjectileManager 
{
    private static final Map<String, ZenithProjectile> ACTIVE_PROJECTILES = new HashMap<>();

    static void registerProjectile(ZenithProjectile projectile)
    {
        ACTIVE_PROJECTILES.put(projectile.getUuid(), projectile);
    }

    public static ZenithProjectile getActiveProjectile(Projectile projectile)
    {
        if (!isZenithProjectile(projectile))
            return null;

        PersistentDataContainer pdc = ((PersistentDataHolder) projectile).getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(Zenith.getInstance(), "zenithProjectileIdentity");

        return pdc.has(key, PersistentDataType.STRING) ? getActiveProjectile(pdc.get(key, PersistentDataType.STRING)) : null;
    }

    public static ZenithProjectile getActiveProjectile(String uuid)
    {
        return ACTIVE_PROJECTILES.get(uuid);
    }

    public static boolean isZenithProjectile(Projectile projectile)
    {
        NamespacedKey key = new NamespacedKey(Zenith.getInstance(), "zenithProjectileIdentity");
        return ((PersistentDataHolder) projectile).getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }
}
