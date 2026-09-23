package com.github.spacemex.fabric.networking;

import com.github.spacemex.fabric.SkillExpNotifierFabric;
import com.github.spacemex.networking.XpGainPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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
        for (ServerPlayer player : server.getPlayerList().getPlayers()){
            UUID uuid = player.getUUID();
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
