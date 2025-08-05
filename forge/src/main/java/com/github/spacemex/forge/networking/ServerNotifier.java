package com.github.spacemex.forge.networking;

import com.github.spacemex.SkillExpNotifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = SkillExpNotifier.MOD_ID,bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerNotifier {
    private static final Map<UUID, Map<Identifier,Integer>> lastTotals = new HashMap<>();

    @SubscribeEvent
    @SuppressWarnings("all")
    public static void onServerTick(TickEvent.ServerTickEvent event){
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        for (var player : server.getPlayerManager().getPlayerList()){
            UUID uuid = player.getUuid();
            var playerMap = lastTotals.computeIfAbsent(uuid,__ -> new HashMap<>());

            SkillsAPI.streamCategories().forEach(cat ->
                    cat.getExperience().ifPresent(exp -> {
                        Identifier id = cat.getId();
                        int total = exp.getTotal(player);
                        int prev = playerMap.getOrDefault(id,total);
                        if (total > prev){
                           int delta = total - prev;
                            XpGainPayload payload = new XpGainPayload(id, delta);
                            PacketDistributor.PLAYER.with((ServerPlayerEntity)player)
                                    .send(payload);
                        }
                        playerMap.put(id,total);
                    })
            );
        }
    }
}
