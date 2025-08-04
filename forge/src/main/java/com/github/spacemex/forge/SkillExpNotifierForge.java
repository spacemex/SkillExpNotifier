package com.github.spacemex.forge;

import com.github.spacemex.forge.networking.client.ClientNotifier;
import com.github.spacemex.forge.networking.client.ToastPacketHandler;
import com.github.spacemex.networking.XpGainPacket;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.github.spacemex.SkillExpNotifier;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SkillExpNotifier.MOD_ID)
public final class SkillExpNotifierForge {
    public static Logger LOGGER = LoggerFactory.getLogger(SkillExpNotifier.MOD_ID);
    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Identifier.fromNamespaceAndPath(SkillExpNotifier.MOD_ID, "network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    public SkillExpNotifierForge(final FMLJavaModLoadingContext modLoadingContext) {
        IEventBus eventBus = modLoadingContext.getModEventBus();
        EventBuses.registerModEventBus(SkillExpNotifier.MOD_ID,eventBus);
        SkillExpNotifier.init();
        eventBus.addListener(this::onCommonSetup);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ()-> ()-> {
            eventBus.addListener(this::clientInit);
        });
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        CHANNEL.registerMessage(
                0,
                XpGainPacket.class,
                XpGainPacket::encode,
                XpGainPacket::decode,
                (msg, ctxSupplier) ->{
                    NetworkEvent.Context ctx = ctxSupplier.get();
                    ctx.setPacketHandled(true);
                    ctx.enqueueWork(()-> DistExecutor.safeRunWhenOn(Dist.CLIENT,()-> ()->{
                        ToastPacketHandler.onXpGain(msg);
                    }));
                }
        );
    }
    @OnlyIn(Dist.CLIENT)
    private void clientInit(FMLClientSetupEvent event) {
        SkillExpNotifier.initClient();
        ClientNotifier.register();
    }
}
