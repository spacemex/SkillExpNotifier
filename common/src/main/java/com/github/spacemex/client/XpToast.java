package com.github.spacemex.client;

import com.github.spacemex.config.ConfigReader;
import com.github.spacemex.yml.YamlConfigUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;

@Environment(EnvType.CLIENT)
public class XpToast implements Toast {
    private static YamlConfigUtil readConfigOnce() {
        return new ConfigReader().getConfig();
    }

    private static final Identifier BG =  Identifier.ofVanilla("toast/advancement");

    private final Identifier categoryId;
    private int gained;
    private long lastUpdateTime;

    private final int bgW;
    private final int bgH;
    private final boolean bgEnabled;
    private final boolean bgTranslucent;
    private final float bgAlpha;

    private final boolean iconEnabled;
    private final float iconScale;
    private final int iconX;
    private final int iconY;
    private final ItemStack iconStack;

    private final boolean inline;
    private final long stackTimerMs;

    private final boolean titleShadow;
    private final int titleShadowARGB;
    private final boolean titleTranslucent;
    private final int titleARGB;
    private final float titleScale;
    private final boolean titleBold;
    private final String titlePattern;

    private final boolean expShadow;
    private final int expShadowARGB;
    private final boolean expTranslucent;
    private final int expARGB;
    private final float expScale;
    private final boolean expBold;
    private final String expPattern;

    private OrderedText preTitleLine;
    private OrderedText preExpLine;
    private OrderedText preCombinedLine;
    private int combinedBaselineY;

    public XpToast(Identifier categoryId, int gained) {
        this.categoryId = categoryId;
        this.gained = gained;
        this.lastUpdateTime = System.currentTimeMillis();

        YamlConfigUtil cfg = readConfigOnce();

        this.bgH = cfg.getInt("Toast-Rendering.Height", 16);
        this.bgW = cfg.getInt("Toast-Rendering.Width", 160);
        this.bgEnabled = !cfg.getBoolean("Toast-Rendering.Disable-Background", true);
        this.bgTranslucent = cfg.getBoolean("Toast-Rendering.Background-Translucent", false);
        this.bgAlpha = (cfg.getFloat("Toast-Rendering.Background-alpha", 127) / 255f);

        this.iconEnabled = cfg.getBoolean("Icon-Settings.Enabled", true);
        this.iconScale = cfg.getFloat("Icon-Settings.Size", 12) / 16f;
        this.iconX = cfg.getInt("Icon-Settings.X-Offset", 14);
        this.iconY = cfg.getInt("Icon-Settings.Y-Offset", 2);

        this.inline = cfg.getBoolean("Toast-Animation.Inline", true);
        this.stackTimerMs = cfg.getLong("Toast-Animation.Stack-XP-Timer", 5000);

        this.titleBold = cfg.getBoolean("Title-Settings.Bold", false);
        this.titleScale = cfg.getFloat("Title-Settings.Size", 6) / 9f;
        this.titleShadow = cfg.getBoolean("Title-Settings.Shadow", false);
        int titleShadowRGB = cfg.getInt("Title-Settings.Shadow-Color", 0) & 0xFFFFFF;
        int titleShadowA = cfg.getBoolean("Title-Settings.Translucent", false) ? cfg.getInt("Title-Settings.Alpha", 127) : 255;
        this.titleShadowARGB = (titleShadowA << 24) | titleShadowRGB;

        this.titleTranslucent = cfg.getBoolean("Title-Settings.Translucent", false);
        int titleRGB = (cfg.getInt("Title-Settings.Color", 16755200) & 0xFFFFFF);
        int titleA = titleTranslucent ? cfg.getInt("Title-Settings.Alpha", 127) : 255;
        this.titleARGB = (titleA << 24) | titleRGB;

        this.titlePattern = cfg.getString("Title-Settings.Title", "%title%");

        this.expBold = cfg.getBoolean("Experience-Settings.Bold", false);
        this.expScale = cfg.getFloat("Experience-Settings.Size", 6) / 9f;
        this.expShadow = cfg.getBoolean("Experience-Settings.Shadow", false);
        int expShadowRGB = (cfg.getInt("Experience-Settings.Shadow-Color", 0) & 0xFFFFFF);
        int expShadowA = cfg.getBoolean("Experience-Settings.Translucent", false) ? cfg.getInt("Experience-Settings.Alpha", 0) : 255;
        this.expShadowARGB = (expShadowA << 24) | expShadowRGB;

        this.expTranslucent = cfg.getBoolean("Experience-Settings.Translucent", false);
        int expRGB = (cfg.getInt("Experience-Settings.Color", 16755200) & 0xFFFFFF);
        int expA = expTranslucent ? cfg.getInt("Experience-Settings.Alpha", 127) : 255;
        this.expARGB = (expA << 24) | expRGB;

        this.expPattern = cfg.getString("Experience-Settings.Exp", " +%exp% xp");

        ItemStack stack = ItemStack.EMPTY;
        String path = this.categoryId.getPath();
        if (this.iconEnabled) {
            stack = EntryRegistry.getIconFor(path);
        }
        this.iconStack = stack;

        rebuildTextLayouts();
    }

    private void rebuildTextLayouts() {
        MinecraftClient mc = MinecraftClient.getInstance();
        String path = categoryId.getPath();
        String titleRaw = formatCategoryName(path);

        String titleStr = titlePattern.replace("%title%", titleRaw);
        if (titleBold) titleStr = "§l" + titleStr;

        String expStr = expPattern.replace("%exp%", String.valueOf(gained));
        if (expBold) expStr = "§l" + expStr;

        if (inline) {
            String combined = titleStr + expStr;
            this.preCombinedLine = Text.literal(combined).asOrderedText();

            this.combinedBaselineY = bgH / 2 - (int)( (6) / 2f );
        } else {
            this.preTitleLine = Text.literal(titleStr).asOrderedText();
            this.preExpLine = Text.literal(expStr).asOrderedText();
        }
    }

    public void addGained(int delta) {
        this.gained += delta;
        this.lastUpdateTime = System.currentTimeMillis();
        rebuildTextLayouts();
    }

    int getGained() { return gained; }


    @Override
    public Visibility draw(DrawContext ctx, ToastManager manager, long startTime) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (bgEnabled) {
            if (bgTranslucent) {
                RenderSystem.setShaderColor(1f, 1f, 1f, bgAlpha);
            }
            ctx.drawTexture(BG, 0, 0, bgW, bgH, 0, 0, 160, 32, 256, 256);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        if (iconEnabled && !iconStack.isEmpty()) {
            ctx.getMatrices().push();
            ctx.getMatrices().scale(iconScale, iconScale, 1f);
            int x0 = (int)(iconX / iconScale);
            int y0 = (int)(iconY / iconScale);
            ctx.drawItem(iconStack, x0, y0);
            ctx.getMatrices().pop();
        }

        if (inline) {
            ctx.getMatrices().push();
            ctx.getMatrices().scale(titleScale, titleScale, 1f);
            int x = (int)(30f / titleScale);
            int y = (int)(combinedBaselineY / titleScale);
            if (titleShadow) {
                ctx.drawText(mc.textRenderer, preCombinedLine, x + 1, y + 1, titleShadowARGB, false);
            }
            ctx.drawText(mc.textRenderer, preCombinedLine, x, y, titleARGB, false);
            ctx.getMatrices().pop();
        } else {
            ctx.getMatrices().push();
            ctx.getMatrices().scale(titleScale, titleScale, 1f);
            int tx = (int)(30f / titleScale);
            int ty = (int)(8f / titleScale);
            if (titleShadow) {
                ctx.drawText(mc.textRenderer, preTitleLine, tx + 1, ty + 1, titleShadowARGB, false);
            }
            ctx.drawText(mc.textRenderer, preTitleLine, tx, ty, titleARGB, false);
            ctx.getMatrices().pop();

            ctx.getMatrices().push();
            ctx.getMatrices().scale(expScale, expScale, 1f);
            int ex = (int)(30f / expScale);
            int ey = (int)(18f / expScale);
            if (expShadow) {
                ctx.drawText(mc.textRenderer, preExpLine, ex + 1, ey + 1, expShadowARGB, false);
            }
            ctx.drawText(mc.textRenderer, preExpLine, ex, ey, expARGB, false);
            ctx.getMatrices().pop();
        }

        long now = System.currentTimeMillis();
        return (now - lastUpdateTime) < stackTimerMs ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override public Object getType() { return categoryId; }
    @Override public int getHeight() { return bgH; }
    @Override public int getWidth() { return bgW; }

    public long getLastUpdateTime() { return lastUpdateTime; }

    public static String formatCategoryName(String rawPath) {
        String[] parts = rawPath.split("_");
        StringBuilder sb = new StringBuilder(rawPath.length() + parts.length);
        for (int i = 0; i < parts.length; i++) {
            String w = parts[i];
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
            if (i + 1 < parts.length) sb.append(' ');
        }
        return sb.toString();
    }
}