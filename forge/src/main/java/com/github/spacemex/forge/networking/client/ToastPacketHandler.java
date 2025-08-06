package com.github.spacemex.forge.networking.client;

import com.github.spacemex.client.XpToast;
import com.github.spacemex.networking.XpGainPayload;
import net.minecraft.util.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class ToastPacketHandler {
    public static void onXpGain(XpGainPayload pkt){
        Identifier id =  pkt.categoryId();
        int delta = pkt.delta();

        XpToast existing = ClientNotifier.getToastComponent()
                .getToast(XpToast.class,id);

        if (existing != null){
            existing.addGained(delta);
        }else {
            ClientNotifier.getToastComponent()
                    .addToast(new XpToast(id,delta));
        }
    }
}
