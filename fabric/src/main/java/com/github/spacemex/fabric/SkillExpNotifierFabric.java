package com.github.spacemex.fabric;

import com.github.spacemex.SkillExpNotifier;
import com.github.spacemex.fabric.networking.ServerNotifier;
import com.github.spacemex.networking.XpGainPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SkillExpNotifierFabric implements ModInitializer {
    public static Logger LOGGER = LoggerFactory.getLogger(SkillExpNotifier.MOD_ID);
    @Override
    public void onInitialize() {
        SkillExpNotifier.init();
        ServerNotifier.register();
        registerPackets();
    }

    private void registerPackets(){
        PayloadTypeRegistry.playS2C().register(XpGainPayload.ID,XpGainPayload.CODEC);
    }

    public static void sendXpGainPacket(ServerPlayerEntity player, XpGainPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

}
