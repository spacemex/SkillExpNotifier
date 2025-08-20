package com.github.spacemex.client;

import com.github.spacemex.config.ConfigReader;
import com.github.spacemex.yml.YamlConfigUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;

@Environment(EnvType.CLIENT)
public class XpToast implements Toast {
    private YamlConfigUtil config(){
        return new ConfigReader().getConfig();
    }

    private static final Identifier BG = new Identifier("toast/advancement");
    private final Identifier categoryId;
    private int gained;
    private long lastUpdateTime;

    int getGained(){
        return gained;
    }

    public XpToast(Identifier categoryId, int gained){
        this.categoryId = categoryId;
        this.gained = gained;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void addGained(int delta){
        this.gained += delta;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    @Override
    public Visibility draw(DrawContext ctx, ToastManager manager, long startTime) {
        MinecraftClient mc = MinecraftClient.getInstance();
        long now = System.currentTimeMillis();
        //Background
        int bgH = config().getInt("Toast-Rendering.Height",16);
        int bgW = config().getInt("Toast-Rendering.Width",160);

        if (!config().getBoolean("Toast-Rendering.Disable-Background",true)){
            if (config().getBoolean("Toast-Rendering.Background-Translucent",false)){
                float a = config().getFloat("Toast-Rendering.Background-alpha",127) / 255f;
                RenderSystem.setShaderColor(1f,1f,1f,a);
            }
            ctx.drawGuiTexture(BG,0,0,bgW,bgH);
            RenderSystem.setShaderColor(1f,1f,1f,1f);
        }

        //Icon
        String path = categoryId.getPath();
        String title = formatCategoryName(path);
        if (config().getBoolean("Icon-Settings.Enabled",true)){
            ItemStack iconStack = EntryRegistry.getIconFor(path);
            if (!iconStack.isEmpty()){
                float iconScale = config().getFloat("Icon-Settings.Size",12) / 16f;
                ctx.getMatrices().push();
                ctx.getMatrices().scale(iconScale,iconScale,1f);

                int x0 = (int)(config().getInt("Icon-Settings.X-Offset",14) / iconScale);
                int y0 = (int)(config().getInt("Icon-Settings.Y-Offset",2) / iconScale);

                ctx.drawItem(iconStack,x0,y0);
                ctx.getMatrices().pop();
            }
        }

        // Text Settings
        BiConsumer<String,Integer> drawTitle = (text, y)->{
            ctx.getMatrices().push();
            float scale = config().getFloat("Title-Settings.Size",6) / 9f;
            ctx.getMatrices().scale(scale,scale,1f);
            int x = (int) (30f / scale);
            int yy = (int) (y / scale);

            if (config().getBoolean("Title-Settings.Shadow",false)){
                int sdRgb = config().getInt("Title-Settings.Shadow-Color",0) & 0xFFFFFF;
                int sdAlpha = config().getBoolean("Title-Settings.Translucent",false) ?
                        config().getInt("Title-Settings.Alpha",127) : 255;
                int sdArgb = (sdAlpha << 24) | sdRgb;
                ctx.drawText(mc.textRenderer,text,x + 1, yy + 1, sdArgb, false);
            }

            int rgb = config().getInt("Title-Settings.Color",16755200) & 0xFFFFFF;
            int alpha = config().getBoolean("Title-Settings.Translucent",false) ?
                    config().getInt("Title-Settings.Alpha",127) : 255;
            int argb = (alpha << 24) | rgb;
            ctx.drawText(mc.textRenderer,text,x,yy,argb,false);
            ctx.getMatrices().pop();
        };

        // Exp Text
        BiConsumer<String,Integer> drawExp = (text, y)->{
            ctx.getMatrices().push();
            float scale = config().getFloat("Experience-Settings.Size",6) / 9f;
            ctx.getMatrices().scale(scale,scale,1f);
            int x = (int) (30f / scale);
            int yy = (int) (y / scale);

            if (config().getBoolean("Experience-Settings.Shadow",false)) {
                int sdRgb = config().getInt("Experience-Settings.Shadow-Color",0) & 0xFFFFFF;
                int sdAlpha = config().getBoolean("Experience-Settings.Translucent",false) ?
                        config().getInt("Experience-Settings.Alpha",0) : 255;
                int sdArgb = (sdAlpha << 24) | sdRgb;
                ctx.drawText(mc.textRenderer,text,x + 1 , yy + 1,sdArgb,false);
            }
            int rgb = config().getInt("Experience-Settings.Color",16755200) & 0xFFFFFF;
            int alpha = config().getBoolean("Experience-Settings.Translucent",false) ?
                    config().getInt("Experience-Settings.Alpha",127) : 255;
            int argb = (alpha << 24) | rgb;
            ctx.drawText(mc.textRenderer, text, x, yy, argb, false);
            ctx.getMatrices().pop();
        };

        //inline vs. two line
        if (config().getBoolean("Toast-Animation.Inline",true)) {
            String tOv = config().getString("Title-Settings.Title","%title%")
                    .replace("%title%", title);
            String xOv = config().getString("Experience-Settings.Exp"," +%exp% xp")
                    .replace("%exp%", String.valueOf(gained));
            String combined = tOv + xOv;
            if (config().getBoolean("Title-Settings.Bold",false) ||
                    config().getBoolean("Experience-Settings.Bold",false)) combined = "§l" + combined;
            // draw inline, vertically centered in bgH (≈midY)
            int midY = bgH / 2 - (config().getInt("Title-Settings.Size",6) / 2);
            drawTitle.accept(combined, midY);
        } else {
            // two-line: title @ y=8, xp @ y=18
            String tt = (config().getBoolean("Title-Settings.Bold",false) ? "§l" : "") + title;
            drawTitle.accept(tt, 8);

            String xp = (config().getBoolean("Experience-Settings.Bold",false) ? "§l" : "")
                    + config().getString("Experience-Settings.Exp"," +%exp% xp")
                    .replace("%exp%", String.valueOf(gained));
            drawExp.accept(xp, 18);
        }

        return (now - lastUpdateTime) < config().getLong("Toast-Animation.Stack-XP-Timer",5000) ?
                Visibility.SHOW : Visibility.HIDE;
    }

    @Override
    public Object getType() {
        return categoryId;
    }

    public static String formatCategoryName(String rawPath) {
        return java.util.Arrays.stream(rawPath.split("_"))
                .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));
    }
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    @Override
    public int getHeight() {
        return config().getInt("Toast-Rendering.Height",16);
    }

    @Override
    public int getWidth() {
        return config().getInt("Toast-Rendering.Width",160);
    }
}
