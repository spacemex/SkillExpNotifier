package com.github.spacemex.forge.networking.client;

import com.github.spacemex.client.CustomToastComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiOverlayEvent;
import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;
import net.neoforged.neoforge.common.NeoForge;


@OnlyIn(Dist.CLIENT)
public class ClientNotifier {
    private static final CustomToastComponent TOASTS = new CustomToastComponent(MinecraftClient.getInstance());

    public static void render(DrawContext context){
        TOASTS.render(context);
    }

    public static CustomToastComponent getToastComponent(){
        return TOASTS;
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event){
        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()){
            render(event.getGuiGraphics());
        }
    }

    public static void register(){
        NeoForge.EVENT_BUS.register(ClientNotifier.class);
    }
}
