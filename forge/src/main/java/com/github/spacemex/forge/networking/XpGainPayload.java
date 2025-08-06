package com.github.spacemex.forge.networking;

import com.github.spacemex.SkillExpNotifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class XpGainPayload implements CustomPayload {
    private static final Identifier ID = new Identifier(SkillExpNotifier.MOD_ID, "xp_gain");
    private final Identifier categoryId;
    private final int delta;

    public XpGainPayload(Identifier categoryId, int delta){
        this.categoryId = categoryId;
        this.delta = delta;
    }

    public XpGainPayload(PacketByteBuf buf) {
        this.categoryId = buf.readIdentifier();
        this.delta = buf.readInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(categoryId);
        buf.writeVarInt(delta);
    }

    @Override
    public Identifier id() {
        return ID;
    }

    public Identifier getCategoryId() {
        return categoryId;
    }

    public int getDelta() {
        return delta;
    }

}
