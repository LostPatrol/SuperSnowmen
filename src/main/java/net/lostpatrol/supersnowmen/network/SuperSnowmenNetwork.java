package net.lostpatrol.supersnowmen.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class SuperSnowmenNetwork {
    private SuperSnowmenNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                PoweredSnowmanPacket.TYPE,
                PoweredSnowmanPacket.STREAM_CODEC,
                PoweredSnowmanPacket::handle);
    }

    public static void sendPoweredState(Entity entity, boolean powered) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, new PoweredSnowmanPacket(entity.getId(), powered));
    }

    public static void sendPoweredState(ServerPlayer player, Entity entity, boolean powered) {
        PacketDistributor.sendToPlayer(player, new PoweredSnowmanPacket(entity.getId(), powered));
    }
}
