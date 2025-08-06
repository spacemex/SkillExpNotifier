package com.github.spacemex.fabric.networking.client;

import com.github.spacemex.SkillExpNotifier;
import com.github.spacemex.client.CustomToastComponent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class ClientNotifier implements ClientModInitializer {
    private static final CustomToastComponent TOASTS = new CustomToastComponent(MinecraftClient.getInstance());

    public static CustomToastComponent getToastComponent() {
        return TOASTS;
    }
    @Override
    public void onInitializeClient() {
        HudElementRegistry.addLast(Identifier.of(SkillExpNotifier.MOD_ID, "xp_toast_id"),
                (context, tickCounter) -> TOASTS.render(context));

       /** Removed In 1.20.6
        *  HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            TOASTS.render(graphics);
        });*/
    }
}
