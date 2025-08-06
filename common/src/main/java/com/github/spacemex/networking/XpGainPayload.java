package com.github.spacemex.networking;

import com.github.spacemex.SkillExpNotifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record XpGainPayload(Identifier categoryId, int delta) implements CustomPayload {

    public static final Id<XpGainPayload> ID =
            new Id<>(Identifier.of(SkillExpNotifier.MOD_ID, "xp_gain"));
    public static final PacketCodec<PacketByteBuf, Identifier> IDENTIFIER_CODEC =
            PacketCodec.of(
                    (id, buf) -> buf.writeString(id.toString()),
                    (buf) -> Identifier.of(buf.readString())
            );
    public static final PacketCodec<PacketByteBuf, XpGainPayload> CODEC =
            PacketCodec.tuple(
                    IDENTIFIER_CODEC, XpGainPayload::categoryId,
                    PacketCodecs.VAR_INT, XpGainPayload::delta,
                    XpGainPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
