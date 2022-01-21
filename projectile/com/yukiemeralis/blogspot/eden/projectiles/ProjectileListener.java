package com.yukiemeralis.blogspot.eden.projectiles;

import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import com.yukiemeralis.blogspot.eden.projectiles.EdenProjectile.BlockDetonator;
import com.yukiemeralis.blogspot.eden.projectiles.EdenProjectile.EntityDetonator;
import com.yukiemeralis.blogspot.eden.projectiles.EdenProjectile.NonspecificDetonator;

public class ProjectileListener implements Listener
{
    @EventHandler
    public void projectileHitBlock(ProjectileHitEvent event)
    {
        if (!ProjectileManager.isEdenProjectile(event.getEntity()))
            return;
        EdenProjectile projectile = ProjectileManager.getActiveProjectile(event.getEntity());

        if (projectile instanceof ParticleProjectile)
            projectile.getParticleTimer().cancel();

        if (projectile instanceof NonspecificDetonator  && event.getHitEntity() == null)
        {
            ((NonspecificDetonator) projectile).onCollision(event);

            if (projectile.getLifetime() != -1)
                projectile.getLifetimeTimer().cancel();

            if (projectile.destroyOnCollision())
                projectile.getRealProjectile().remove();
            return;
        }

        if (projectile instanceof BlockDetonator && event.getHitEntity() == null)
        {
            ((BlockDetonator) projectile).onBlockCollision(event);

            if (projectile.getLifetime() != -1)
                projectile.getLifetimeTimer().cancel();

            if (projectile.destroyOnCollision())
                projectile.getRealProjectile().remove();
            return;
        }
    }

    @EventHandler
    public void projectileHitEntity(EntityDamageByEntityEvent event)
    {
        if (!(event.getDamager() instanceof Projectile))
            return;
        Projectile p = (Projectile) event.getDamager();

        if (!ProjectileManager.isEdenProjectile(p))
            return;

        EdenProjectile projectile = ProjectileManager.getActiveProjectile(p);

        if (projectile instanceof NonspecificDetonator)
        {
            ((NonspecificDetonator) projectile).onCollision(event);

            if (projectile.getLifetime() != -1)
                projectile.getLifetimeTimer().cancel();

            if (projectile.destroyOnCollision())
                projectile.getRealProjectile().remove();
            return;
        }

        if (projectile instanceof EntityDetonator)
        {
            ((EntityDetonator) projectile).onEntityCollision(event);

            if (projectile instanceof ParticleProjectile)
                projectile.getParticleTimer().cancel();

            if (projectile.getLifetime() != -1)
                projectile.getLifetimeTimer().cancel();

            if (projectile.destroyOnCollision())
                projectile.getRealProjectile().remove();
            return;
        }
    }
}
