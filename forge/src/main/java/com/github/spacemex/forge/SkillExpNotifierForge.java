package com.github.spacemex.forge;

import com.github.spacemex.SkillExpNotifier;
import com.github.spacemex.forge.networking.XpGainPayload;
import com.github.spacemex.forge.networking.client.ClientNotifier;
import com.github.spacemex.forge.networking.client.ToastPacketHandler;
import com.github.spacemex.networking.XpGainPacket;
import net.minecraft.util.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SkillExpNotifier.MOD_ID)
public final class SkillExpNotifierForge {
    public static Logger LOGGER = LoggerFactory.getLogger(SkillExpNotifier.MOD_ID);
    public static final Identifier XP_GAIN_PACKET_ID = new Identifier(SkillExpNotifier.MOD_ID, "network");
    public static final String PROTOCOL_VERSION = "1";

    public SkillExpNotifierForge(IEventBus eventBus) {
        SkillExpNotifier.init();

        eventBus.addListener(this::registerPayloadHandler);

        if (FMLEnvironment.dist.isClient()){
            eventBus.addListener(this::clientInit);
        }

        LOGGER.info("SkillExpNotifier NeoForge module initialized");

    }

    private void registerPayloadHandler(RegisterPayloadHandlerEvent event){
        final IPayloadRegistrar registrar = event.registrar(SkillExpNotifier.MOD_ID)
                .versioned(PROTOCOL_VERSION);

        registrar.play(XP_GAIN_PACKET_ID, XpGainPayload::new, handler -> handler
                .client((payload, context) -> {
                    context.workHandler().execute(() -> {
                        ToastPacketHandler.onXpGain(new XpGainPacket(payload.getCategoryId(), payload.getDelta()));
                    });
                })
        );
    }
    @OnlyIn(Dist.CLIENT)
    private void clientInit(FMLClientSetupEvent event) {
        SkillExpNotifier.initClient();
        ClientNotifier.register();
    }
}
