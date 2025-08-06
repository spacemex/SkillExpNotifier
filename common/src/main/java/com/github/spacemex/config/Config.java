package com.github.spacemex.config;

import com.github.spacemex.Helper;
import com.github.spacemex.yml.YamlConfigTemplateWriter;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

public class Config {
    public static void generateConfig(Path configPath) {

        Helper.getPlatformsLogger().info("Generating Config at {}", configPath.toAbsolutePath());
        File yamlFile = new File(configPath.toUri());
        yamlFile.getParentFile().mkdirs();
        YamlConfigTemplateWriter writer = new YamlConfigTemplateWriter(configPath.toFile());

        writer
                .header("""
                        Client-side configuration for SkillExpNotifier
                        Base configuration provided by Chakyl.
                        Useful Link: https://convertingcolors.com/decimal-color-16755200.html?search=Decimal(16755200)
                        """)
                .add("Client-Settings.Disable", false, "Disables all rendering of toasts (client-side only)")
                .add("Client-Settings.File-Watcher", true, "Enables the file watcher to detect config changes automatically")
                .add("Client-Settings.Auto-Register-Data", true, "Automatically registers skill categories and assigns icons (overrides existing entries)")

                .add("Settings.Max-Toasts", 1, "Maximum number of toasts to display simultaneously")
                .add("Settings.Animation-Direction", "top", "Direction for toast animation and stacking (top, down, left, right)")

                .add("Toast-Rendering.Anchor-Point", "bottom-left", "Screen anchor point for toast positioning. Options: top-left, top-center, top-right, middle-left, middle-center, middle-right, bottom-left, bottom-center, bottom-right")
                .add("Toast-Rendering.Base-X", 0, "Horizontal offset from the anchor point in pixels")
                .add("Toast-Rendering.Base-Y", 0, "Vertical offset from the anchor point in pixels")
                .add("Toast-Rendering.Height", 16, "Height of the toast background in pixels")
                .add("Toast-Rendering.Width", 160, "Width of the toast background in pixels")
                .add("Toast-Rendering.Background-Translucent", false, "**Legacy** (Does nothing 1.21.6 and up. Do to rendering system constantly changing) Makes the toast background partially transparent")
                .add("Toast-Rendering.Disable-Background", true, "Completely removes the toast background")
                .add("Toast-Rendering.Background-alpha", 127, "Transparency level of the background (0-255, lower is more transparent)")

                .add("Toast-Animation.Stack-XP-Timer", 5000, "Duration in milliseconds that a toast remains visible after the last XP update")
                .add("Toast-Animation.Animation-Time", 1000, "Duration in milliseconds of the slide in/out animation")
                .add("Toast-Animation.No-Slide", false, "Disables the sliding animation for toasts")
                .add("Toast-Animation.Inline", true, "Shows title and XP text on a single line instead of stacked")

                .add("Title-Settings.Title", "%title%", "Text format for the toast title (%title% is replaced with the skill name)")
                .add("Title-Settings.Size", 6, "Font size of the title text in points")
                .add("Title-Settings.Color", 16755200, "Color of the title text in ARGB format")
                .add("Title-Settings.Shadow", false, "Adds a shadow effect to the title text")
                .add("Title-Settings.Shadow-Color", 0, "Color of the title text shadow in ARGB format")
                .add("Title-Settings.Translucent", false, "Makes the title text partially transparent")
                .add("Title-Settings.Alpha", 127, "Transparency level of the title text (0-255, lower is more transparent)")
                .add("Title-Settings.Bold", false, "Makes the title text bold")

                .add("Experience-Settings.Exp", " +%exp% xp", "Text format for the XP display (%exp% is replaced with the amount)")
                .add("Experience-Settings.Size", 6, "Font size of the XP text in points")
                .add("Experience-Settings.Color", 16755200, "Color of the XP text in ARGB format")
                .add("Experience-Settings.Shadow", false, "Adds a shadow effect to the XP text")
                .add("Experience-Settings.Shadow-Color", 0, "Color of the XP text shadow in ARGB format")
                .add("Experience-Settings.Translucent", false, "Makes the XP text partially transparent")
                .add("Experience-Settings.Alpha", 127, "Transparency level of the XP text (0-255, lower is more transparent)")
                .add("Experience-Settings.Bold", false, "Makes the XP text bold")

                .add("Icon-Settings.Enabled", true, "Enables display of skill icons in toasts")
                .add("Icon-Settings.X-Offset", 14, "Horizontal position of the icon within the toast in pixels")
                .add("Icon-Settings.Y-Offset", 2, "Vertical position of the icon within the toast in pixels")
                .add("Icon-Settings.Size", 12, "Size of the icon in pixels")

                .add("Sound-Settings.Enabled", true, "Enables sound effects when toasts appear and disappear")
                .add("Sound-Settings.In-Sound", Arrays.asList("minecraft:overworld=minecraft:ui.toast.in", "minecraft:the_nether=minecraft:ui.toast.in", "minecraft:the_end=minecraft:ui.toast.in"),
                        "Sound mappings for toast appearance by dimension (Format: <dimension>=<sound>)")
                .add("Sound-Settings.Out-Sound", Arrays.asList("minecraft:overworld=minecraft:ui.toast.out", "minecraft:the_nether=minecraft:ui.toast.out", "minecraft:the_end=minecraft:ui.toast.out"),
                        "Sound mappings for toast disappearance by dimension (Format: <dimension>=<sound>)")
                .write();
    }
}
