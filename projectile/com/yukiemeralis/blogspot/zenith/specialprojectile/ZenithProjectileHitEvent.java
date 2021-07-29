package com.yukiemeralis.blogspot.zenith.specialprojectile;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ZenithProjectileHitEvent extends Event
{
    private static final HandlerList handlerList = new HandlerList();

    public static enum CollisionType
    {
        BLOCK,
        ENTITY,
        NONSPECIFIC
        ;
    }

    private final ZenithProjectile projectile;
    private final CollisionType type;

    public ZenithProjectileHitEvent(ZenithProjectile projectile, CollisionType type)
    {
        this.projectile = projectile;
        this.type = type;
    }

    public ZenithProjectile getProjectile()
    {
        return this.projectile;
    }

    public CollisionType getType()
    {
        return this.type;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlerList;
    }
    
    public static HandlerList getHandlerList()
    {
        return handlerList;
    }
}
