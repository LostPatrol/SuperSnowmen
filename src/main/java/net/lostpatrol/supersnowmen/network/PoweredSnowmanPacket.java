package net.lostpatrol.supersnowmen.network;

import net.lostpatrol.supersnowmen.client.ClientPoweredSnowmen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PoweredSnowmanPacket(int entityId, boolean powered) {
    static void encode(PoweredSnowmanPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeBoolean(packet.powered);
    }

    static PoweredSnowmanPacket decode(FriendlyByteBuf buffer) {
        return new PoweredSnowmanPacket(buffer.readVarInt(), buffer.readBoolean());
    }

    static void handle(PoweredSnowmanPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        ClientPoweredSnowmen.update(packet.entityId, packet.powered);
        contextSupplier.get().setPacketHandled(true);
    }
}
