package com.github.spacemex.config;

import com.github.spacemex.Helper;
import com.github.spacemex.yml.YamlConfigUtil;
import dev.architectury.platform.Platform;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class ConfigReader {
    private final YamlConfigUtil util;
    public ConfigReader() {
        File yamlFile = Platform.getConfigFolder().resolve("SkillExpNotifier/config.yml").toFile();
        Yaml yaml = new Yaml();
        Map<String,Object> data;
        try(FileReader reader = new FileReader(yamlFile)){
            data = yaml.load(reader);
        }catch (IOException e){
            throw new RuntimeException("Failed to load config file",e);
        }
        util = new YamlConfigUtil(data);
    }
    public YamlConfigUtil getConfig() {
        return util;
    }
}