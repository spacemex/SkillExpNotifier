package com.github.spacemex.fabric.networking.client;

import com.github.spacemex.client.CustomToastComponent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

public class ClientNotifier implements ClientModInitializer {
    private static final CustomToastComponent TOASTS = new CustomToastComponent(MinecraftClient.getInstance());

    public static CustomToastComponent getToastComponent() {
        return TOASTS;
    }
    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            TOASTS.render(graphics);
        });
    }
}
