package com.github.spacemex;

import com.github.spacemex.client.EntryRegistry;
import com.github.spacemex.config.Config;
import dev.architectury.platform.Platform;

import java.nio.file.Path;

public final class SkillExpNotifier {
    public static final String MOD_ID = "skillexpnotifier";
    public static void init() {
        Helper.getPlatformsLogger().info("Finally, Hello At Last World ~ " + MOD_ID);
    }

    public static void initClient() {
        Helper.getPlatformsLogger().info("Client Init");
        Path configFile = Platform.getConfigFolder().resolve("SkillExpNotifier/config.yml");
        Path iconFile = Platform.getConfigFolder().resolve("SkillExpNotifier/iconMappings.json");
        Config.generateConfig(configFile);
        EntryRegistry.loadFromFile(iconFile,Platform.getGameFolder());
    }

}
