package com.github.spacemex.config;

import com.github.spacemex.Helper;
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
    private static long lastModified = -1;
    private static YamlConfigUtil config;

    public static void  init(){
        reloadConfig();
        ClientTickEvent.CLIENT_POST.register(client -> tick());
    }
    private static void tick(){
        if (!getBoolean("Client-Settings.File-Watcher",true)) return;
        try{
            Path configFile = Platform.getConfigFolder().resolve("SkillExpNotifier/config.yml");
            long currentModified = Files.getLastModifiedTime(configFile).toMillis();
            if (currentModified > lastModified){
                lastModified = currentModified;
                reloadConfig();
                Helper.getPlatformsLogger().info("[File Watcher] Config Reloaded");
            }
        }catch (IOException e){
            Helper.getPlatformsLogger().error("[File Watcher] Failed to read file modified time",e);
        }
    }

    private static void reloadConfig(){
        Path configFile = Platform.getConfigFolder().resolve("SkillExpNotifier/config.yml");
        try(FileInputStream fis = new FileInputStream(configFile.toFile())){
            Yaml yaml = new Yaml();
            Map<String,Object> data = yaml.load(fis);
            config = new YamlConfigUtil(data);
            lastModified = Files.getLastModifiedTime(configFile).toMillis();
        }catch (IOException e){
            Helper.getPlatformsLogger().error("[File Watcher] Failed to load config",e);
        }
    }

    private static boolean getBoolean(String key, boolean defaultValue){
        return config != null ? config.getBoolean(key, defaultValue) : defaultValue;
    }
}
