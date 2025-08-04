package com.github.spacemex.client;

import com.github.spacemex.Helper;
import com.github.spacemex.config.ConfigReader;
import com.github.spacemex.yml.YamlConfigUtil;
import com.google.gson.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
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
    private record Entry(Pattern pattern, ItemStack icon){}
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private static YamlConfigUtil config(){
        return new ConfigReader().getConfig();
    }

    public static void loadFromFile(Path configFile, Path gameDir) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            Files.createDirectories(configFile.getParent());

            JsonArray jsonArray = new JsonArray();
            boolean shouldWriteFile = false;

            if (config().getBoolean("Client-Settings.Auto-Register-Data", true)) {
                List<SearchForSkills.CategoryData> auto = SearchForSkills.scan(gameDir);

                if (!auto.isEmpty()) {
                    for (var cd : auto) {
                        JsonObject o = new JsonObject();
                        // exact match on that category ID
                        o.addProperty("regex", "^" + Pattern.quote(cd.idPath()) + "$");
                        o.addProperty("icon", cd.iconId());
                        jsonArray.add(o);
                    }
                    shouldWriteFile = true;
                }
            }

            if (!Files.exists(configFile) ||
                    (config().getBoolean("Client-Settings.Auto-Register-Data", true) && jsonArray.isEmpty())) {
                jsonArray.add(makeEntry(".*mining.*", "minecraft:iron_pickaxe", gson));
                jsonArray.add(makeEntry(".*farming.*", "minecraft:iron_hoe", gson));
                jsonArray.add(makeEntry(".*husbandry.*", "minecraft:wheat", gson));
                jsonArray.add(makeEntry(".*fishing.*", "minecraft:fishing_rod", gson));
                jsonArray.add(makeEntry(".*adventuring.*", "minecraft:iron_sword", gson));
                shouldWriteFile = true;
            }

            if (shouldWriteFile) {
                Files.writeString(configFile, gson.toJson(jsonArray));
            }

            try (Reader reader = Files.newBufferedReader(configFile)) {
                JsonElement root = JsonParser.parseReader(reader);
                if (!root.isJsonArray()) {
                    Helper.getPlatformsLogger().warn("IconMappings.json isn't an array, skipping.");
                    return;
                }
                ENTRIES.clear();
                for (JsonElement el : root.getAsJsonArray()) {
                    JsonObject obj = el.getAsJsonObject();
                    String regex = obj.get("regex").getAsString();
                    String iconId = obj.get("icon").getAsString();

                    Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                    Item item = Registries.ITEM.get(Identifier.tryParse(iconId));
                    ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item);
                    ENTRIES.add(new Entry(p, stack));
                }
            }
        } catch (IOException e) {
            Helper.getPlatformsLogger().error("Failed loading IconMappings.json", e);
        }
    }
    private static JsonObject makeEntry(String regex, String icon, Gson g) {
        JsonObject o = new JsonObject();
        o.addProperty("regex", regex);
        o.addProperty("icon",   icon);
        return o;
    }

    public static ItemStack getIconFor(String categoryPath) {
        for (var e : ENTRIES) {
            if (e.pattern.matcher(categoryPath).matches()) {
                return e.icon.copy();
            }
        }
        return ItemStack.EMPTY;
    }
}
