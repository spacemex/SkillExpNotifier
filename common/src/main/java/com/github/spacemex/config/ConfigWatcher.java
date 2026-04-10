package com.github.spacemex.config;

import com.github.spacemex.Helper;
import com.github.spacemex.client.EntryRegistry;
import com.github.spacemex.yml.YamlConfigUtil;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ConfigWatcher {
    private static long configLastModified = -1L;
    private static long iconLastModified = -1L;
    private static YamlConfigUtil config;

    private static Path iconMappingsFile;
    private static Path gameDir;

    public static void init(Path iconFile, Path currentGameDir) {
        iconMappingsFile = iconFile;
        gameDir = currentGameDir;

        reloadConfig();
        iconLastModified = getLastModified(iconMappingsFile);

        ClientTickEvent.CLIENT_POST.register(client -> tick());
    }

    private static void tick() {
        if (!getBoolean("Client-Settings.File-Watcher", true)) {
            return;
        }

        watchConfig();
        watchIconMappings();
    }

    private static void watchConfig() {
        try {
            Path configFile = Platform.getConfigFolder().resolve("SkillExpNotifier/config.yml");
            long currentModified = Files.getLastModifiedTime(configFile).toMillis();

            if (currentModified > configLastModified) {
                reloadConfig();
                Helper.getPlatformsLogger().info("[File Watcher] Config Reloaded");
            }
        } catch (IOException e) {
            Helper.getPlatformsLogger().error("[File Watcher] Failed to read config file modified time", e);
        }
    }

    private static void watchIconMappings() {
        if (iconMappingsFile == null || gameDir == null || !Files.exists(iconMappingsFile)) {
            return;
        }

        try {
            long currentModified = Files.getLastModifiedTime(iconMappingsFile).toMillis();

            if (currentModified > iconLastModified) {
                iconLastModified = currentModified;
                EntryRegistry.loadFromFile(iconMappingsFile, gameDir);
                Helper.getPlatformsLogger().info("[File Watcher] IconMappings Reloaded");
            }
        } catch (IOException e) {
            Helper.getPlatformsLogger().error("[File Watcher] Failed to read iconMappings.json modified time", e);
        }
    }

    private static void reloadConfig() {
        Path configFile = Platform.getConfigFolder().resolve("SkillExpNotifier/config.yml");

        try (FileInputStream fis = new FileInputStream(configFile.toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(fis);
            config = new YamlConfigUtil(data);
            configLastModified = Files.getLastModifiedTime(configFile).toMillis();
        } catch (IOException e) {
            Helper.getPlatformsLogger().error("[File Watcher] Failed to load config", e);
        }
    }

    private static long getLastModified(Path path) {
        try {
            return Files.exists(path) ? Files.getLastModifiedTime(path).toMillis() : -1L;
        } catch (IOException e) {
            Helper.getPlatformsLogger().error("[File Watcher] Failed to read file modified time for {}", path, e);
            return -1L;
        }
    }

    private static boolean getBoolean(String key, boolean defaultValue) {
        return config != null ? config.getBoolean(key, defaultValue) : defaultValue;
    }
}