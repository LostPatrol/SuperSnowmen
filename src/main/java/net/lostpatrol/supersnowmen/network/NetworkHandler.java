package net.lostpatrol.supersnowmen.network;

import net.lostpatrol.supersnowmen.SuperSnowmen;
import net.lostpatrol.supersnowmen.network.handler.HandlerOpenSnowmanUpgrade;
import net.lostpatrol.supersnowmen.network.packet.PacketOpenSnowmanUpgrade;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel channel;

    private NetworkHandler() {
    }

    public static void register() {
        if (channel != null) {
            return;
        }
        channel = NetworkRegistry.newSimpleChannel(
                ResourceLocation.fromNamespaceAndPath(SuperSnowmen.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        int id = 0;
        channel.registerMessage(
                id,
                PacketOpenSnowmanUpgrade.class,
                PacketOpenSnowmanUpgrade::encode,
                PacketOpenSnowmanUpgrade::new,
                HandlerOpenSnowmanUpgrade::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    public static void sendOpenSnowmanUpgradeToServer(PacketOpenSnowmanUpgrade packet) {
        if (channel == null) {
            throw new IllegalStateException("Network channel is not registered");
        }
        channel.sendToServer(packet);
    }
}
