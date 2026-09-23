package com.github.spacemex.forge.networking.client;

import com.github.spacemex.client.CustomToastComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;


@OnlyIn(Dist.CLIENT)
public class ClientNotifier {
    private static final CustomToastComponent TOASTS = new CustomToastComponent(Minecraft.getInstance());

    public static void render(GuiGraphicsExtractor context){
        TOASTS.render(context);
    }

    public static CustomToastComponent getToastComponent(){
        return TOASTS;
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event){
            render(event.getGuiGraphics());

    }

    public static void register(){
        NeoForge.EVENT_BUS.register(ClientNotifier.class);
    }
}
