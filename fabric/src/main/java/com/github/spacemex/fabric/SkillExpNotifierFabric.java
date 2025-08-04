package com.github.spacemex.fabric;

import com.github.spacemex.SkillExpNotifier;
import com.github.spacemex.fabric.networking.ServerNotifier;
import com.github.spacemex.networking.XpGainPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SkillExpNotifierFabric implements ModInitializer {
    public static Logger LOGGER = LoggerFactory.getLogger(SkillExpNotifier.MOD_ID);
    public static final Identifier XP_GAIN_PACKET_ID = new Identifier(SkillExpNotifier.MOD_ID, "network");
    @Override
    public void onInitialize() {
        SkillExpNotifier.init();
        ServerNotifier.register();
        registerPackets();
    }

    private void registerPackets(){
        ServerPlayNetworking.registerGlobalReceiver(XP_GAIN_PACKET_ID,(server,player,handler,buf,responseSender)-> server.execute(()->{

        }));
    }

    public static void sendXpGainPacket(ServerPlayerEntity player, XpGainPacket pkt){
        PacketByteBuf buf = PacketByteBufs.create();
        XpGainPacket.encode(pkt,buf);
        ServerPlayNetworking.send(player, XP_GAIN_PACKET_ID, buf);
    }
}
