package com.github.spacemex.forge.networking;

import com.github.spacemex.SkillExpNotifier;
import com.github.spacemex.forge.SkillExpNotifierForge;
import com.github.spacemex.networking.XpGainPacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
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
                           ClientConnection connection = player.networkHandler.getConnection();
                           SkillExpNotifierForge.CHANNEL.send(
                                   new XpGainPacket(id,delta),
                                   connection
                           );
                        }
                        playerMap.put(id,total);
                    })
            );
        }
    }
}
