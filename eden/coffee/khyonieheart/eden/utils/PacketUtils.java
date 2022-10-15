package fish.yukiemeralis.eden.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fish.yukiemeralis.eden.Eden;
import fish.yukiemeralis.eden.utils.exception.VersionNotHandledException;

public interface PacketUtils
{
    public static void hideEntity(Entity e, Player player)
    {
        switch (Eden.getNMSVersion())
        {
            case "v1_16_R3":
                new PacketUtils_v1_16_R3().hideEntityInternal(e, player);
                break;
            case "v1_17_R1":
                new PacketUtils_v1_17_R1().hideEntityInternal(e, player);
                break;
            case "v1_18_R1":
                new PacketUtils_v1_18_R1().hideEntityInternal(e, player);
                break;
            case "v1_18_R2":
                new PacketUtils_v1_18_R2().hideEntityInternal(e, player);
                break;
            case "v1_19_R1":
                new PacketUtils_v1_19_R1().hideEntityInternal(e, player);
                break;
            default:
                throw new VersionNotHandledException();
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

            // Mojang slightly changed how packets work going between 1.16.5, 1.17, and 1.17.1, so we have to handle two different packet constructors
            // Pain
            
            try {
                net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy.class.getConstructor(int.class); // 1.17, but not 1.17.1
                connection_1_17.a(new net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy(entity_1_17.ae()));
            } catch (NoSuchMethodException err) {
                connection_1_17.a(new net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy(new int[] {entity_1_17.ae()})); // 1.17.1
            }
        }
    }

    public static class PacketUtils_v1_18_R1 implements PacketUtils
    {
        @Override
        public void hideEntityInternal(Entity e, Player player) 
        {
            net.minecraft.server.network.PlayerConnection connection_1_18 = ((org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer) player).getHandle().b;
            net.minecraft.world.entity.Entity entity_1_18 = ((org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity) e).getHandle();
            
            connection_1_18.a(new net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy(entity_1_18.ae()));
        }
    }

    public static class PacketUtils_v1_18_R2 implements PacketUtils
    {
        @Override
        public void hideEntityInternal(Entity e, Player player) 
        {
            net.minecraft.server.network.PlayerConnection connection_1_18 = ((org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer) player).getHandle().b;
            net.minecraft.world.entity.Entity entity_1_18 = ((org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity) e).getHandle();
            
            connection_1_18.a(new net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy(entity_1_18.ae()));
        }
    }

    public static class PacketUtils_v1_19_R1 implements PacketUtils
    {
        @Override
        public void hideEntityInternal(Entity e, Player player) 
        {
            net.minecraft.server.network.PlayerConnection connection_1_19 = ((org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer) player).getHandle().b;
            net.minecraft.world.entity.Entity entity_1_19 = ((org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity) e).getHandle();

            connection_1_19.a(new net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy(entity_1_19.ae()));
        }        
    }
}