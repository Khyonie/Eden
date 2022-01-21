package com.yukiemeralis.blogspot.eden.projectiles;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EdenProjectileHitEvent extends Event
{
    private static final HandlerList handlerList = new HandlerList();

    public static enum CollisionType
    {
        BLOCK,
        ENTITY,
        NONSPECIFIC
        ;
    }

    private final EdenProjectile projectile;
    private final CollisionType type;

    public EdenProjectileHitEvent(EdenProjectile projectile, CollisionType type)
    {
        this.projectile = projectile;
        this.type = type;
    }

    public EdenProjectile getProjectile()
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
