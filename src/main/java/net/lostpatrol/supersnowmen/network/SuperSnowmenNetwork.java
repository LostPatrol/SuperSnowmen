package net.lostpatrol.supersnowmen.network;

import net.lostpatrol.supersnowmen.SuperSnowmen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class SuperSnowmenNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(SuperSnowmen.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private SuperSnowmenNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(PoweredSnowmanPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PoweredSnowmanPacket::encode)
                .decoder(PoweredSnowmanPacket::decode)
                .consumerMainThread(PoweredSnowmanPacket::handle)
                .add();
    }

    public static void sendPoweredState(Entity entity, boolean powered) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                new PoweredSnowmanPacket(entity.getId(), powered));
    }

    public static void sendPoweredState(ServerPlayer player, Entity entity, boolean powered) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new PoweredSnowmanPacket(entity.getId(), powered));
    }
}
