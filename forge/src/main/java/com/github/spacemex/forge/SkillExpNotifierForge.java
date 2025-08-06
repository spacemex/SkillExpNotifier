package com.github.spacemex.forge;

import com.github.spacemex.SkillExpNotifier;
import com.github.spacemex.forge.networking.ServerNotifier;
import com.github.spacemex.forge.networking.client.ClientNotifier;
import com.github.spacemex.forge.networking.client.ToastPacketHandler;
import com.github.spacemex.networking.XpGainPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SkillExpNotifier.MOD_ID)
public final class SkillExpNotifierForge {
    public static Logger LOGGER = LoggerFactory.getLogger(SkillExpNotifier.MOD_ID);

    public SkillExpNotifierForge(IEventBus eventBus) {
        SkillExpNotifier.init();

        ServerNotifier.register();
        eventBus.addListener(this::registerPayloadHandlers);

        if (FMLEnvironment.dist.isClient()){
            eventBus.addListener(this::clientInit);
        }

        LOGGER.info("SkillExpNotifier NeoForge module initialized");

    }

    private void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar(SkillExpNotifier.MOD_ID)
                .playToClient(
                        XpGainPayload.ID,
                        XpGainPayload.CODEC,
                        (payload, context) -> {
                            context.enqueueWork(()-> {
                                ToastPacketHandler.onXpGain(
                                        new XpGainPayload(payload.categoryId(), payload.delta())
                                );
                            });
                        }
                );
    }
    @OnlyIn(Dist.CLIENT)
    private void clientInit(FMLClientSetupEvent event) {
        SkillExpNotifier.initClient();
        ClientNotifier.register();
    }
}
