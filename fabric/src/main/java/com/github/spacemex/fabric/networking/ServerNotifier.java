package com.github.spacemex.fabric.networking;

import com.github.spacemex.fabric.SkillExpNotifierFabric;
import com.github.spacemex.networking.XpGainPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerNotifier {
    private static final Map<UUID,Map<Identifier,Integer>> lastTotals = new HashMap<>();

    public static void register(){
        ServerTickEvents.END_SERVER_TICK.register(ServerNotifier::tick);
    }

    private static void tick(MinecraftServer server){
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
            UUID uuid = player.getUuid();
            var playerMap = lastTotals.computeIfAbsent(uuid,__ -> new HashMap<>());

            SkillsAPI.streamCategories().forEach(cat ->
                    cat.getExperience().ifPresent(exp -> {
                        Identifier id = cat.getId();
                        int total = exp.getTotal(player);
                        int prev = playerMap.getOrDefault(id,total);
                        if (total > prev){
                           int delta = total - prev;
                           SkillExpNotifierFabric.sendXpGainPacket(player,new XpGainPayload(id,delta));
                        }
                        playerMap.put(id,total);
                    }));
        }
    }
}
