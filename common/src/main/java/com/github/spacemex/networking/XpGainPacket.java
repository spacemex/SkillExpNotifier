package com.github.spacemex.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
@Deprecated
public record XpGainPacket(Identifier categoryId, int delta) {

    public static void encode(XpGainPacket pkt, PacketByteBuf buf) {
        buf.writeIdentifier(pkt.categoryId);
        buf.writeVarInt(pkt.delta);
    }

    public static XpGainPacket decode(PacketByteBuf buf) {
        Identifier id = buf.readIdentifier();
        int delta = buf.readVarInt();
        return new XpGainPacket(id, delta);
    }
}
