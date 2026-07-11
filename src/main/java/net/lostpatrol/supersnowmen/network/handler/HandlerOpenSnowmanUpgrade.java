package net.lostpatrol.supersnowmen.network.handler;

import net.lostpatrol.supersnowmen.network.packet.PacketOpenSnowmanUpgrade;
import net.lostpatrol.supersnowmen.snowman.SnowmanEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class HandlerOpenSnowmanUpgrade {
    private HandlerOpenSnowmanUpgrade() {
    }

    public static void handle(PacketOpenSnowmanUpgrade packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            Entity entity = player.level().getEntity(packet.snowmanId());
            if (entity instanceof SnowGolem snowman) {
                SnowmanEvents.handleSnowmanInteraction(player, snowman, packet.withdraw());
            }
        });
        context.setPacketHandled(true);
    }
}
