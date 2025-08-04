package com.github.spacemex.fabric.client;

import com.github.spacemex.SkillExpNotifier;
import com.github.spacemex.client.XpToast;
import com.github.spacemex.fabric.SkillExpNotifierFabric;
import com.github.spacemex.fabric.networking.client.ClientNotifier;
import com.github.spacemex.networking.XpGainPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class SkillExpNotifierFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SkillExpNotifier.initClient();

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            ClientNotifier.getToastComponent().render(drawContext);
        });

        ClientPlayNetworking.registerGlobalReceiver(SkillExpNotifierFabric.XP_GAIN_PACKET_ID,(client,handler,buf,responseSender)->{
            XpGainPacket pkt = XpGainPacket.decode(buf);
            client.execute(()->{
                ClientNotifier.getToastComponent().addToast(new XpToast(pkt.categoryId(),pkt.delta()));
            });
        });
    }
}
