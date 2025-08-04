package com.github.spacemex.forge;

import com.github.spacemex.SkillExpNotifier;
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
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SkillExpNotifier.MOD_ID)
public final class SkillExpNotifierForge {
    public static Logger LOGGER = LoggerFactory.getLogger(SkillExpNotifier.MOD_ID);
    public static SimpleChannel CHANNEL;
    public static final Identifier XP_GAIN_PACKET_ID = new Identifier(SkillExpNotifier.MOD_ID, "network");
    public static final int PROTOCOL_VERSION = 1;



    public SkillExpNotifierForge() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(SkillExpNotifier.MOD_ID,eventBus);
        SkillExpNotifier.init();
        eventBus.addListener(this::onCommonSetup);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ()-> ()-> {
            eventBus.addListener(this::clientInit);
        });
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        CHANNEL = ChannelBuilder.named(XP_GAIN_PACKET_ID)
                .networkProtocolVersion(PROTOCOL_VERSION)
                .clientAcceptedVersions((s,v)->true)
                .serverAcceptedVersions((s,v)->true)
                .simpleChannel();

        CHANNEL.messageBuilder(XpGainPacket.class)
                .encoder(XpGainPacket::encode)
                .decoder(XpGainPacket::decode)
                .consumerMainThread((msg, ctx)->{
                    if (ctx.getDirection().getReceptionSide().isClient()){
                        ToastPacketHandler.onXpGain(msg);
                    }
                    ctx.setPacketHandled(true);
                })
                .add();
    }
    @OnlyIn(Dist.CLIENT)
    private void clientInit(FMLClientSetupEvent event) {
        SkillExpNotifier.initClient();
        ClientNotifier.register();
    }
}
