package com.yukiemeralis.blogspot.zenith.specialprojectile.debug;

import com.yukiemeralis.blogspot.zenith.specialprojectile.ParticleProjectile;
import com.yukiemeralis.blogspot.zenith.specialprojectile.ProjectileFlag;
import com.yukiemeralis.blogspot.zenith.specialprojectile.ZenithProjectile;
import com.yukiemeralis.blogspot.zenith.specialprojectile.ZenithProjectile.BlockDetonator;

import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.event.entity.ProjectileHitEvent;

public class DebugProjectile extends ZenithProjectile implements BlockDetonator, ParticleProjectile
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
