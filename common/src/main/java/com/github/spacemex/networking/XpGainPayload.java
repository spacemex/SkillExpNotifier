package com.github.spacemex.networking;

import com.github.spacemex.SkillExpNotifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record XpGainPayload(Identifier categoryId, int delta) implements CustomPacketPayload {

    public static final Type<XpGainPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(SkillExpNotifier.MOD_ID, "xp_gain"));
    public static final StreamCodec<FriendlyByteBuf, Identifier> IDENTIFIER_CODEC =
            StreamCodec.ofMember(
                    (id, buf) -> buf.writeUtf(id.toString()),
                    (buf) -> Identifier.parse(buf.readUtf())
            );
    public static final StreamCodec<FriendlyByteBuf, XpGainPayload> CODEC =
            StreamCodec.composite(
                    IDENTIFIER_CODEC, XpGainPayload::categoryId,
                    ByteBufCodecs.VAR_INT, XpGainPayload::delta,
                    XpGainPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
