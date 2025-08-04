package com.github.spacemex.client;

import com.github.spacemex.Helper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class SearchForSkills {
    public record CategoryData(String idPath,String iconId) {}

    public static List<CategoryData> scan(Path gameDir){
        List<File> found = new ArrayList<>();
        Path datapacks = gameDir.resolve("datapacks");
        scanDir(datapacks,found);
        if (Platform.isModLoaded("kubejs")){
            Path jsData = gameDir.resolve("kubejs/data");
            scanDir(jsData,found);
        }

        var results = new ArrayList<CategoryData>();
        for (File file : found){
            try(Reader reader = Files.newBufferedReader(file.toPath())){
                JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                String idPath = file.getParentFile().getName();
                String itemId = obj.getAsJsonObject("icon")
                        .getAsJsonObject("data")
                        .get("item").getAsString();
                results.add(new CategoryData(idPath,itemId));
            }catch (Exception e){
                Helper.getPlatformsLogger().error("Failed to read category file {}",file.toPath(),e);
            }
        }
        return results;
    }

    private static void scanDir(Path dir, List<File> out) {
        File d = dir.toFile();
        if (!d.exists() || !d.isDirectory()) return;
        for (File f : Objects.requireNonNull(d.listFiles())) {
            if (f.isDirectory()) scanDir(f.toPath(), out);
            else if ("category.json".equals(f.getName())) out.add(f);
        }
    }
}
