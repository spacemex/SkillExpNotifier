package com.github.spacemex.client;

import com.github.spacemex.Helper;
import com.github.spacemex.config.ConfigReader;
import com.github.spacemex.yml.YamlConfigUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class EntryRegistry {
    private record Entry(Pattern pattern, ItemStack icon) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private static YamlConfigUtil config() {
        return new ConfigReader().getConfig();
    }

    public static void loadFromFile(Path configFile, Path gameDir) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            Files.createDirectories(configFile.getParent());

            boolean autoRegister = config().getBoolean("Client-Settings.Auto-Register-Data", true);

            // Only generate the file the first time.
            if (!Files.exists(configFile)) {
                JsonArray generatedEntries = autoRegister ? buildAutoEntries(gameDir) : new JsonArray();

                if (generatedEntries.isEmpty()) {
                    generatedEntries = buildDefaultEntries();
                }

                Files.writeString(configFile, gson.toJson(generatedEntries));
            }

            readEntries(configFile);
        } catch (IOException e) {
            Helper.getPlatformsLogger().error("Failed loading IconMappings.json", e);
        }
    }

    private static JsonArray buildAutoEntries(Path gameDir) {
        JsonArray jsonArray = new JsonArray();
        List<SearchForSkills.CategoryData> auto = SearchForSkills.scan(gameDir);

        for (SearchForSkills.CategoryData cd : auto) {
            JsonObject obj = new JsonObject();
            obj.addProperty("regex", "^" + Pattern.quote(cd.idPath()) + "$");
            obj.addProperty("icon", cd.iconId());
            jsonArray.add(obj);
        }

        return jsonArray;
    }

    private static JsonArray buildDefaultEntries() {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(makeEntry(".*mining.*", "minecraft:iron_pickaxe"));
        jsonArray.add(makeEntry(".*farming.*", "minecraft:iron_hoe"));
        jsonArray.add(makeEntry(".*husbandry.*", "minecraft:wheat"));
        jsonArray.add(makeEntry(".*fishing.*", "minecraft:fishing_rod"));
        jsonArray.add(makeEntry(".*adventuring.*", "minecraft:iron_sword"));
        return jsonArray;
    }

    private static void readEntries(Path configFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(configFile)) {
            JsonElement root = JsonParser.parseReader(reader);

            if (!root.isJsonArray()) {
                Helper.getPlatformsLogger().warn("IconMappings.json isn't an array, skipping.");
                return;
            }

            ENTRIES.clear();

            for (JsonElement el : root.getAsJsonArray()) {
                if (!el.isJsonObject()) {
                    continue;
                }

                JsonObject obj = el.getAsJsonObject();
                JsonElement regexEl = obj.get("regex");
                JsonElement iconEl = obj.get("icon");

                if (regexEl == null || iconEl == null) {
                    Helper.getPlatformsLogger().warn("Skipping invalid icon mapping entry: {}", obj);
                    continue;
                }

                String regex = regexEl.getAsString();
                String iconId = iconEl.getAsString();

                Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                ItemStack iconStack = parseIcon(iconId);

                ENTRIES.add(new Entry(pattern, iconStack));
            }
        }
    }

    private static ItemStack parseIcon(String iconId) {
        Identifier identifier = Identifier.tryParse(iconId);

        if (identifier == null) {
            Helper.getPlatformsLogger().warn("Invalid item identifier in iconMappings.json: {}", iconId);
            return ItemStack.EMPTY;
        }

        if (!Registries.ITEM.containsId(identifier)) {
            Helper.getPlatformsLogger().warn("Unknown item in iconMappings.json: {}", iconId);
            return ItemStack.EMPTY;
        }

        return new ItemStack(Registries.ITEM.get(identifier));
    }

    private static JsonObject makeEntry(String regex, String icon) {
        JsonObject obj = new JsonObject();
        obj.addProperty("regex", regex);
        obj.addProperty("icon", icon);
        return obj;
    }

    public static ItemStack getIconFor(String categoryPath) {
        for (Entry entry : ENTRIES) {
            if (entry.pattern.matcher(categoryPath).matches()) {
                return entry.icon.copy();
            }
        }

        return ItemStack.EMPTY;
    }
}