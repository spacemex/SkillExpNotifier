package com.github.spacemex.forge.networking.client;

import com.github.spacemex.client.CustomToastComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.extensions.IForgeGuiGraphics;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        MinecraftForge.EVENT_BUS.register(ClientNotifier.class);
    }
}
