package com.almostreliable.almostpacked;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.Comparator;
import java.util.stream.StreamSupport;

class FileProcessor {

    private final File dir;

    private Config config;
    private File modDir;
    private File mcInstanceFile;
    private File syncFile;
    private JsonObject mcInstance;
    private JsonArray installedMods;
    private JsonArray syncedMods;

    FileProcessor() {
        dir = new File(".");
        System.out.println("Working directory: " + dir.getAbsoluteFile().getParentFile());
    }

    void readConfig() throws IOException {
        AlmostPacked.printHeader("Reading config...");

        var subFolder = false;
        if (!new File(dir, "minecraftinstance.json").exists()) {
            if (!new File(getParentDir(dir), "minecraftinstance.json").exists()) {
                AlmostPacked.abort("Not running in a valid Minecraft instance!");
                System.exit(1);
            }
            subFolder = true;
        }

        var configFile = getOrCreateFile(
            "config.json",
            """
            {
                "fileName": "sync",
                "subFolder": %s,
                "prettyJson": true,
                "concurrentDownloads": 10,
                "devMode": false
            }
            """.formatted(subFolder)
        );
        config = readFile(configFile, Config.class);
    }

    void readFiles() throws IOException {
        System.out.println("Searching 'minecraftinstance.json' file...");
        mcInstanceFile = new File(getDirByConfig(), "minecraftinstance.json");
        if (!mcInstanceFile.exists()) {
            AlmostPacked.abort("File 'minecraftinstance.json' does not exist!");
        }
        System.out.println("Found 'minecraftinstance.json' file!");

        mcInstance = readFile(mcInstanceFile, JsonObject.class);
        System.out.println();

        syncFile = getOrCreateFile(config.fileName + ".json", "[]");
        System.out.println();
    }

    void readDirectories() {
        System.out.println("Searching 'mods' directory...");

        modDir = new File(getDirByConfig(), "mods");
        if (modDir.exists() && !modDir.isDirectory()) {
            AlmostPacked.abort("Directory 'mods' exists but is a file!");
            System.exit(1);
        }

        if (!modDir.exists()) {
            System.out.println("Directory 'mods' does not exist! Creating...");
            if (!modDir.mkdir()) {
                AlmostPacked.abort("Directory 'mods' couldn't be created!");
                System.exit(1);
            }
        }

        System.out.println("Found 'mods' directory!");
        System.out.println();
    }

    void readMods() throws IOException {
        System.out.println("Reading installed mods...");

        installedMods = mcInstance.getAsJsonArray("installedAddons");
        if (installedMods == null) {
            AlmostPacked.abort("File 'minecraftinstance.json' is invalid!");
            System.exit(1);
        }
        if (installedMods.isEmpty() && AlmostPacked.isPushing()) {
            AlmostPacked.abort("No mods installed!");
            System.exit(0);
        }
        installedMods = sortMods(installedMods);
        System.out.println("Found " + installedMods.size() + " installed mods!");
        System.out.println();

        syncedMods = readFile(syncFile, JsonArray.class);
        syncedMods = sortMods(syncedMods);
        System.out.println("Found " + syncedMods.size() + " synced mods!");
    }

    void overwriteInstance() throws IOException {
        mcInstance.add("installedAddons", syncedMods);
        writeFile(mcInstanceFile, mcInstance, false);
    }

    void overwriteSync(JsonArray content) throws IOException {
        writeFile(syncFile, sortMods(content), config.prettyJson);
    }

    private JsonArray sortMods(JsonArray mods) {
        var sortedModList = StreamSupport.stream(mods.spliterator(), false)
            .map(JsonElement::getAsJsonObject)
            .sorted(Comparator.comparingInt(
                o -> o.get("addonID").getAsInt()))
            .toList();
        var sortedMods = new JsonArray();
        for (var mod : sortedModList) {
            sortedMods.add(mod);
        }
        return sortedMods;
    }

    private File getParentDir(File dir) {
        return dir.getAbsoluteFile().getParentFile().getParentFile();
    }

    private File getOrCreateFile(String fileName, String defaultValue) throws IOException {
        System.out.println("Searching '" + fileName + "' file...");

        var file = new File(dir, fileName);
        if (!file.exists()) {
            System.out.println("File '" + fileName + "' does not exist! Creating...");

            if (!file.createNewFile()) {
                AlmostPacked.abort("File '" + fileName + "' couldn't be created!");
                System.exit(1);
            }
            try (var writer = new FileWriter(file)) {
                writer.write(defaultValue);
            }
        }

        System.out.println("Found '" + fileName + "' file!");
        return file;
    }

    private <T> T readFile(File file, Class<T> clazz) throws FileNotFoundException {
        System.out.println("Reading '" + file.getName() + "' file...");
        var content = AlmostPacked.GSON.fromJson(new FileReader(file), clazz);
        System.out.println("Read '" + file.getName() + "' file!");
        return content;
    }

    private void writeFile(File file, JsonElement content, boolean pretty) throws IOException {
        var writer = new FileWriter(file);
        if (pretty) {
            AlmostPacked.PRETTY_GSON.toJson(content, writer);
        } else {
            AlmostPacked.GSON.toJson(content, writer);
        }
        writer.flush();
        writer.close();
    }

    private File getDirByConfig() {
        if (config.subFolder) {
            return getParentDir(dir);
        }
        return dir;
    }

    Config getConfig() {
        return config;
    }

    File getModDir() {
        return modDir;
    }

    JsonArray getInstalledMods() {
        return installedMods;
    }

    JsonArray getSyncedMods() {
        return syncedMods;
    }
}
