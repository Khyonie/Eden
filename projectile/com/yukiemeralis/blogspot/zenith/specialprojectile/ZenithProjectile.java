package com.yukiemeralis.blogspot.zenith.specialprojectile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.utils.PacketUtils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ZenithProjectile
{
    private String uuid;
    private Projectile projectile;

    private final Class<? extends Projectile> projectileClass;
    private final List<ProjectileFlag> flags;

    private BukkitTask particleTimer;
    private BukkitTask lifeTimer;

    private boolean destroyOnCollision = false;
    private long lifetime = -1;

    public ZenithProjectile(Class<? extends Projectile> projectileClass, ProjectileFlag... flags)
    {
        this.projectileClass = projectileClass;
        this.flags = Arrays.asList(flags);
    }

    public ZenithProjectile(Class<? extends Projectile> projectileClass, long lifetime, ProjectileFlag... flags)
    {
        this(projectileClass, flags);
        this.lifetime = lifetime;
    }

    public void launch(ProjectileSource source)
    {
        this.projectile = source.launchProjectile(projectileClass);
        this.uuid = UUID.randomUUID().toString();

        applyUuid();
        ProjectileManager.registerProjectile(this);

        // Apply flags
        if (flags.contains(ProjectileFlag.NO_GRAVITY))
            this.projectile.setGravity(false);
        if (flags.contains(ProjectileFlag.INVISIBLE))
            Bukkit.getOnlinePlayers().forEach(player -> {
                PacketUtils.hideEntity(this.projectile, player);
            });
        if (flags.contains(ProjectileFlag.BOUNCY))
            this.projectile.setBounce(true);
        if (flags.contains(ProjectileFlag.DESTROY_ON_COLLISION))
            this.destroyOnCollision = true;
        if (flags.contains(ProjectileFlag.NULL_SHOOTER))
            this.projectile.setShooter(null);

        // Generic flags
        this.projectile.setSilent(true);
        if (this.projectile instanceof AbstractArrow)
            ((AbstractArrow) this.projectile).setPickupStatus(PickupStatus.DISALLOWED);

        ZenithProjectile instance = this;

        // Start particle timer
        if (this instanceof ParticleProjectile)
        {
            particleTimer = new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    ((ParticleProjectile) instance).refreshEffect();
                }
            }.runTaskTimer(Zenith.getInstance(), 2, 1);
        }
    

        // Start lifetime timer, if applicable
        if (lifetime != -1)
            lifeTimer = new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (this instanceof LifeExpiredDetonator)
                        ((LifeExpiredDetonator) this).onExpire();
                    if (this instanceof ParticleProjectile)
                        particleTimer.cancel();
                    if (destroyOnCollision)
                        projectile.remove();
                }
            }.runTaskLater(Zenith.getInstance(), lifetime);
    }

    private void applyUuid()
    {
        PersistentDataContainer pdc = ((PersistentDataHolder) this.projectile).getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(Zenith.getInstance(), "zenithProjectileIdentity");
        pdc.set(key, PersistentDataType.STRING, uuid);
    }

    public String getUuid()
    {
        return this.uuid;
    }

    public Projectile getRealProjectile()
    {
        return this.projectile;
    }

    public boolean destroyOnCollision()
    {
        return this.destroyOnCollision;
    }

    public BukkitTask getParticleTimer()
    {
        return this.particleTimer;
    }

    public BukkitTask getLifetimeTimer()
    {
        return this.lifeTimer;
    }

    public long getLifetime()
    {
        return this.lifetime;
    }

    public static interface BlockDetonator
    {
        public void onBlockCollision(ProjectileHitEvent event);
    }

    public static interface EntityDetonator
    {
        public void onEntityCollision(EntityDamageByEntityEvent event);
    }

    public static interface NonspecificDetonator
    {
        public void onCollision(EntityEvent event);
    }

    public static interface LifeExpiredDetonator
    {
        public void onExpire();
    }
}
