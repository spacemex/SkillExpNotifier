package com.github.spacemex.client;

import com.github.spacemex.Helper;
import com.github.spacemex.config.ConfigReader;
import com.github.spacemex.yml.YamlConfigUtil;
import com.google.common.collect.Queues;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.*;

@Environment(EnvType.CLIENT)
public class CustomToastComponent {
    private YamlConfigUtil config(){
        return new ConfigReader().getConfig();
    }
    private final MinecraftClient minecraft;
    private final List<CustomToastInstance<?>> visable = new ArrayList<>();
    private final BitSet occupiedSlots = new BitSet();
    private final Deque<Toast> queued = Queues.newArrayDeque();

    public CustomToastComponent(MinecraftClient minecraft) {
        this.minecraft = minecraft;
    }

    public void addToast(Toast toast){
        if (config().getBoolean("Client-Settings.Disable",false)) return;
        XpToast existingVisible = getToast(XpToast.class,toast.getType());
        if (existingVisible != null && toast instanceof XpToast incoming){
            existingVisible.addGained(incoming.getGained());
            Helper.getPlatformsLogger().debug("Merged visible toast {} and {}",existingVisible,toast);
            return;
        }
        for (Toast queuedToast : queued){
            if (queuedToast instanceof XpToast queuedXp &&
            queuedToast.getClass() == toast.getClass() &&
                    Objects.equals(queuedToast.getType(), toast.getType())){
                queuedXp.addGained(((XpToast) toast).getGained());
                Helper.getPlatformsLogger().debug("Merged queued toast {} and {}",queuedXp,toast);
                return;
            }
        }
        queued.add(toast);
        Helper.getPlatformsLogger().debug("Queued toast {}",toast);
    }

    public void render(DrawContext ctx){
        if (minecraft.options.hudHidden) return;

        int screenWidth = ctx.getScaledWindowWidth();

        Iterator<CustomToastInstance<?>> it = visable.iterator();
        while (it.hasNext()){
            CustomToastInstance<?> instance = it.next();
            if (instance.render(screenWidth,ctx)){
                occupiedSlots.clear(instance.index,instance.index + instance.slotCount);
                it.remove();
            }
        }
        if (!queued.isEmpty() && freeSlots() > 0){
            Iterator<Toast> qi = queued.iterator();
            while (qi.hasNext() && freeSlots() > 0){
                Toast toast = qi.next();
                int slots = toast.getRequiredSpaceCount();
                int idx = findFreeIndex(slots);
                if (idx != -1){
                    visable.add(new CustomToastInstance<>(toast,idx,slots));
                    occupiedSlots.set(idx,idx + slots);
                    qi.remove();
                }
            }
        }
    }

    private int freeSlots(){
        return getSlotCount() - occupiedSlots.cardinality();
    }

    private int findFreeIndex(int slotCount){
        if (freeSlots() < slotCount) return -1;
        int count = 0;
        for (int i = 0; i < getSlotCount(); i++){
            if (occupiedSlots.get(i)){
                count = 0;
            }else {
                if (++count == slotCount){
                    return i + 1 - slotCount;
                }
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    public <T extends Toast> T getToast(Class<? extends T> pToastClass, Object token){
        for (CustomToastInstance<?> inst : visable){
            Toast t = inst.toast;
            if (pToastClass.isAssignableFrom(t.getClass()) && t.getType().equals(token))
                return (T) t;
        }
        for (Toast t : queued){
            if (pToastClass.isAssignableFrom(t.getClass()) && t.getType().equals(token))
                return (T) t;
        }
        return null;
    }
    @Environment(EnvType.CLIENT)

    private class CustomToastInstance<T extends Toast> {
        private final T toast;
        private final int index;
        private final int slotCount;
        private long animationTime = -1L;
        private long visibleTime = -1L;
        private Toast.Visibility visibility = Toast.Visibility.SHOW;

        CustomToastInstance(T toast, int index, int slotCount) {
            this.toast = toast;
            this.index = index;
            this.slotCount = slotCount;

            if (toast instanceof XpToast xpToast) {
                xpToast.addGained(0);
            }
        }

        @SuppressWarnings("all")
        public boolean render(int screenWidth, DrawContext ctx) {
            long now = System.currentTimeMillis();
            if (animationTime < 0) {
                animationTime = now;
                if (config().getBoolean("Sound-Settings.Enabled", true)) {
                    String dimKey = minecraft.world.getRegistryKey().getValue().toString();
                    if (dimKey.isEmpty()){
                        var world = minecraft.world.OVERWORLD;
                        dimKey = world.getValue().toString();
                    }
                    SoundEvent inSound = Registries.SOUND_EVENT.get(Identifier.tryParse(getSoundInForDimension(dimKey)));
                    if (inSound != null) Objects.requireNonNull(minecraft.player).playSound(inSound, 1.0F, 1.0F);
                }
            }

            if (visibility == Toast.Visibility.SHOW && now - animationTime <= getAnimationTime()) {
                visibleTime = now;
            }

            float t = (float) (now - animationTime) / getAnimationTime();
            t = Math.min(1f, Math.max(0f, t));
            float ease = t * t;
            if (visibility == Toast.Visibility.HIDE) {
                ease = 1f - ease;
            }

            ctx.getMatrices().push();

            String anchor = config().getString("Toast-Rendering.Anchor-Point", "bottom-left").toLowerCase();
            String dir = config().getString("Settings.Animation-Direction", "down").toLowerCase();
            boolean isLeft = dir.equals("left");
            boolean isRight = dir.equals("right");
            boolean isTop = dir.equals("top");
            boolean isDown = dir.equals("down") || (!isLeft && !isRight && !isTop);
            boolean noSlide = config().getBoolean("Toast-Animation.No-Slide", false);

            int toastW = toast.getWidth(), toastH = toast.getHeight();
            int screenH = ctx.getScaledWindowHeight();

            // Base coordinates for the anchor point
            float anchorX, anchorY;

            switch (anchor) {
                case "top-left" -> {
                    anchorX = 0;
                    anchorY = 0;
                }
                case "top-center" -> {
                    anchorX = (screenWidth - toastW) / 2f;
                    anchorY = 0;
                }
                case "top-right" -> {
                    anchorX = screenWidth - toastW;
                    anchorY = 0;
                }
                case "middle-left" -> {
                    anchorX = 0;
                    anchorY = (screenH - toastH) / 2f;
                }
                case "middle-center", "center" -> {
                    anchorX = (screenWidth - toastW) / 2f;
                    anchorY = (screenH - toastH) / 2f;
                }
                case "middle-right" -> {
                    anchorX = screenWidth - toastW;
                    anchorY = (screenH - toastH) / 2f;
                }
                case "bottom-left" -> {
                    anchorX = 0;
                    anchorY = screenH - toastH;
                }
                case "bottom-center" -> {
                    anchorX = (screenWidth - toastW) / 2f;
                    anchorY = screenH - toastH;
                }
                default -> { // bottom-right (default)
                    anchorX = screenWidth - toastW;
                    anchorY = screenH - toastH;
                }
            }

            // Apply base offsets from config
            anchorX += getBaseX();
            anchorY += getBaseY();

            // Determine stacking direction based on animation direction
            // This is the key change to fix the multiple toasts stacking issue
            float offsetX = 0, offsetY = 0;

            // Calculate position offset for this specific toast based on its index
            if (isTop) {
                offsetY = -index * toastH; // Stack upward from anchor
            } else if (isDown) {
                offsetY = index * toastH; // Stack downward from anchor
            } else if (isLeft) {
                offsetX = -index * toastW; // Stack leftward from anchor
            } else if (isRight) {
                offsetX = index * toastW; // Stack rightward from anchor
            }

            // Apply slide animation if enabled
            float slideX = 0, slideY = 0;
            if (!noSlide) {
                if (isLeft) {
                    slideX = (1f - ease) * toastW;
                } else if (isRight) {
                    slideX = -(1f - ease) * toastW;
                } else if (isTop) {
                    slideY = (1f - ease) * toastH;
                } else if (isDown) {
                    slideY = -(1f - ease) * toastH;
                }
            }

            // Calculate final position with all factors
            float x = anchorX + offsetX + slideX;
            float y = anchorY + offsetY + slideY;

            ctx.getMatrices().translate(x, y, 800f);
            toast.update(minecraft.getToastManager(),now);
            toast.draw(ctx, minecraft.textRenderer, now - visibleTime);
            Toast.Visibility newVis = toast.getVisibility();
            ctx.getMatrices().pop();

            if (newVis != visibility) {
                animationTime = now - (long) ((1f - ease) * getAnimationTime());
                visibility = newVis;
                if (config().getBoolean("Sound-Settings.Enabled", true)) {
                    String dimKey = minecraft.world.getRegistryKey().getValue().toString();
                    if (dimKey.isEmpty()) {
                        var world = minecraft.world.OVERWORLD;
                        dimKey = world.getValue().toString();
                    }
                    SoundEvent outSound = Registries.SOUND_EVENT.get(Identifier.tryParse(getSoundOutForDimension(dimKey)));
                    if (outSound != null) Objects.requireNonNull(minecraft.player).playSound(outSound, 1.0F, 1.0F);
                }
            }

            boolean finished;
            if (toast instanceof XpToast xpToast) {
                finished = now - xpToast.getLastUpdateTime() > config().getLong("Toast-Animation.Stack-XP-Timer", 5000);
            } else {
                finished = visibility == Toast.Visibility.HIDE && now - animationTime > getAnimationTime();
            }

            return finished;
        }
    }
    private int getBaseX(){
        return config().getInt("Toast-Rendering.Base-X",0);
    }
    private int getBaseY(){
        return config().getInt("Toast-Rendering.Base-Y",0);
    }
    private int getSlotCount(){
        return Math.max(1,config().getInt("Settings.Max-Toasts",1));
    }
    private long getAnimationTime(){
        return config().getLong("Toast-Animation.Animation-Time",1000) <= 0 ?
                1500L : config().getLong("Toast-Animation.Animation-Time",1000);
    }

    public static String getSoundInForDimension(String dimId) {
        YamlConfigUtil config = new ConfigReader().getConfig();
        List<String> mappings = config.getStringList("Sound-Settings.In-Sound", List.of(
                "minecraft:overworld=minecraft:ui.toast.in",
                "minecraft:the_nether=minecraft:ui.toast.in",
                "minecraft:the_end=minecraft:ui.toast.in"
        ));
        return parseMappings(mappings).getOrDefault(dimId, "minecraft:ui.toast.in");
    }

    public static String getSoundOutForDimension(String dimId) {
        YamlConfigUtil config = new ConfigReader().getConfig();
        List<String> mappings = config.getStringList("Sound-Settings.Out-Sound", List.of(
                "minecraft:overworld=minecraft:ui.toast.out",
                "minecraft:the_nether=minecraft:ui.toast.out",
                "minecraft:the_end=minecraft:ui.toast.out"
        ));
        return parseMappings(mappings).getOrDefault(dimId, "minecraft:ui.toast.out");
    }

    private static Map<String, String> parseMappings(List<String> raw) {
        Map<String, String> map = new HashMap<>();
        for (String entry : raw) {
            var split = entry.split("=", 2);
            if (split.length == 2) {
                map.put(split[0].trim(), split[1].trim());
            }
        }
        return map;
    }
}
