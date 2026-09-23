package com.github.spacemex.forge.networking;

import com.github.spacemex.networking.XpGainPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerNotifier {
    private static final Map<UUID, Map<Identifier,Integer>> lastTotals = new HashMap<>();

    @SubscribeEvent
    @SuppressWarnings("all")
    public static void onServerTick(ServerTickEvent.Post event){
        if (event == null) return;
        MinecraftServer sserver = event.getServer();
        int ticks = sserver.getTickCount();
        if (ticks % 20 != 0) return;  // Run every 20 ticks
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        for (var player : server.getPlayerList().getPlayers()){
            UUID uuid = player.getUUID();
            var playerMap = lastTotals.computeIfAbsent(uuid,__ -> new HashMap<>());

            SkillsAPI.streamCategories().forEach(cat ->
                    cat.getExperience().ifPresent(exp -> {
                        Identifier id = cat.getId();
                        int total = exp.getTotal(player);
                        int prev = playerMap.getOrDefault(id,total);
                        if (total > prev){
                           int delta = total - prev;
                            XpGainPayload payload = new XpGainPayload(id, delta);
                            PacketDistributor.sendToPlayer(player,payload);
                        }
                        playerMap.put(id,total);
                    })
            );
        }
    }

    public static void register(){
        NeoForge.EVENT_BUS.register(ServerNotifier.class);
    }
}
