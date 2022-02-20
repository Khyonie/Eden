package com.yukiemeralis.blogspot.eden.projectiles.debug;

import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.event.entity.ProjectileHitEvent;

import com.yukiemeralis.blogspot.eden.projectiles.EdenProjectile;
import com.yukiemeralis.blogspot.eden.projectiles.EdenProjectile.BlockDetonator;
import com.yukiemeralis.blogspot.eden.projectiles.ParticleProjectile;
import com.yukiemeralis.blogspot.eden.projectiles.ProjectileFlag;

public class DebugProjectile extends EdenProjectile implements BlockDetonator, ParticleProjectile
{
    public DebugProjectile()
    {
        super(Arrow.class, ProjectileFlag.DESTROY_ON_COLLISION, ProjectileFlag.INVISIBLE);
    }

    @Override
    public void onBlockCollision(ProjectileHitEvent event)
    {
        
    }

    @Override
    public void refreshEffect()
    {
        this.getRealProjectile().getWorld().spawnParticle(Particle.EXPLOSION_HUGE, this.getRealProjectile().getLocation(), 1);
    }
}
