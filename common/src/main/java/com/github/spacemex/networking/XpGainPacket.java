package com.github.spacemex.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
@Deprecated
public record XpGainPacket(Identifier categoryId, int delta) {

    public static void encode(XpGainPacket pkt, FriendlyByteBuf buf) {
        buf.writeIdentifier(pkt.categoryId);
        buf.writeVarInt(pkt.delta);
    }

    public static XpGainPacket decode(FriendlyByteBuf buf) {
        Identifier id = buf.readIdentifier();
        int delta = buf.readVarInt();
        return new XpGainPacket(id, delta);
    }
}
