package net.lostpatrol.supersnowmen.network;

import net.lostpatrol.supersnowmen.SuperSnowmen;
import net.lostpatrol.supersnowmen.client.ClientPoweredSnowmen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PoweredSnowmanPacket(int entityId, boolean powered) implements CustomPacketPayload {
    public static final Type<PoweredSnowmanPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SuperSnowmen.MOD_ID, "powered_snowman"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PoweredSnowmanPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PoweredSnowmanPacket::entityId,
            ByteBufCodecs.BOOL, PoweredSnowmanPacket::powered,
            PoweredSnowmanPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    static void handle(PoweredSnowmanPacket packet, IPayloadContext context) {
        ClientPoweredSnowmen.update(packet.entityId, packet.powered);
    }
}
