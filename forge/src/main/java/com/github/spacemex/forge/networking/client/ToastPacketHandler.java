package com.github.spacemex.forge.networking.client;

import com.github.spacemex.client.XpToast;
import com.github.spacemex.networking.XpGainPacket;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ToastPacketHandler {
    public static void onXpGain(XpGainPacket pkt){
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
