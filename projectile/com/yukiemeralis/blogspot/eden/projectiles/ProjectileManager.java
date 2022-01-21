package com.yukiemeralis.blogspot.eden.projectiles;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Projectile;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import com.yukiemeralis.blogspot.eden.Eden;

public class ProjectileManager 
{
    private static final Map<String, EdenProjectile> ACTIVE_PROJECTILES = new HashMap<>();

    static void registerProjectile(EdenProjectile projectile)
    {
        ACTIVE_PROJECTILES.put(projectile.getUuid(), projectile);
    }

    public static EdenProjectile getActiveProjectile(Projectile projectile)
    {
        if (!isEdenProjectile(projectile))
            return null;

        PersistentDataContainer pdc = ((PersistentDataHolder) projectile).getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(Eden.getInstance(), "edenProjectileIdentity");

        return pdc.has(key, PersistentDataType.STRING) ? getActiveProjectile(pdc.get(key, PersistentDataType.STRING)) : null;
    }

    public static EdenProjectile getActiveProjectile(String uuid)
    {
        return ACTIVE_PROJECTILES.get(uuid);
    }

    public static boolean isEdenProjectile(Projectile projectile)
    {
        NamespacedKey key = new NamespacedKey(Eden.getInstance(), "edenProjectileIdentity");
        return ((PersistentDataHolder) projectile).getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }
}
