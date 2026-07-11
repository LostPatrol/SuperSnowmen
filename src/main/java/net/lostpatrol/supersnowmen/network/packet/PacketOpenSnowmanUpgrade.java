package net.lostpatrol.supersnowmen.network.packet;

import net.minecraft.network.FriendlyByteBuf;

public class PacketOpenSnowmanUpgrade {
    private final int snowmanId;
    private final boolean withdraw;

    public PacketOpenSnowmanUpgrade(int snowmanId, boolean withdraw) {
        this.snowmanId = snowmanId;
        this.withdraw = withdraw;
    }

    public PacketOpenSnowmanUpgrade(FriendlyByteBuf buf) {
        this.snowmanId = buf.readInt();
        this.withdraw = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(snowmanId);
        buf.writeBoolean(withdraw);
    }

    public int snowmanId() {
        return snowmanId;
    }

    public boolean withdraw() {
        return withdraw;
    }
}
