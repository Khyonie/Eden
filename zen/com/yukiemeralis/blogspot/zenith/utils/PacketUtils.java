package com.yukiemeralis.blogspot.zenith.utils;

import com.yukiemeralis.blogspot.zenith.Zenith;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface PacketUtils
{
    public static void hideEntity(Entity e, Player player)
    {
        if (Zenith.getNMSVersion().equals("v1_16_R3")) {
            new PacketUtils_v1_16_R3().hideEntityInternal(e, player);
        } else if (Zenith.getNMSVersion().equals("v1_17_R1")) {
            new PacketUtils_v1_17_R1().hideEntityInternal(e, player);
        }
    }

    public void hideEntityInternal(Entity e, Player player);

    public static class PacketUtils_v1_16_R3 implements PacketUtils
    {
        @Override
        public void hideEntityInternal(Entity e, Player player)
        {
            net.minecraft.server.v1_16_R3.PlayerConnection connection_1_16 = ((org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer) player).getHandle().playerConnection;
            net.minecraft.server.v1_16_R3.Entity entity_1_16 = ((org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity) e).getHandle();
            connection_1_16.sendPacket(new net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy(entity_1_16.getId()));
        }
    }

    public static class PacketUtils_v1_17_R1 implements PacketUtils
    {

        @Override
        public void hideEntityInternal(Entity e, Player player)
        {
            net.minecraft.server.network.PlayerConnection connection_1_17 = ((org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer) player).getHandle().b;
            net.minecraft.world.entity.Entity entity_1_17 = ((org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity) e).getHandle();
            connection_1_17.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy(entity_1_17.getId()));
        }
        
    }
}
