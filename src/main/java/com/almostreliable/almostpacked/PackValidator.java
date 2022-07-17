package com.almostreliable.almostpacked;

import com.almostreliable.almostpacked.ModData.AddonFile;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// download logic credits: Vazkii
@SuppressWarnings("CallToPrintStackTrace")
class PackValidator {

    private final FileProcessor fileProcessor;
    private final File modDir;
    private final Config config;
    private final List<String> removalFilter;
    private ExecutorService executor;
    private int downloadCount;
    private ModData[] toValidate;

    PackValidator(FileProcessor fileProcessor) {
        this.fileProcessor = fileProcessor;
        modDir = fileProcessor.getModDir();
        config = fileProcessor.getConfig();
        removalFilter = new LinkedList<>();
    }

    void handleSyncing() throws IOException {
        AlmostPacked.printHeader("Checking for sync requirements...");

        var installedMods = fileProcessor.getInstalledMods();
        var installedModData = AlmostPacked.GSON.fromJson(installedMods, ModData[].class);

        if (fileProcessor.getSyncedMods().isEmpty()) {
            System.out.println("This appears to be the first run of the tool.\nInitial data will be written.");
            fileProcessor.overwriteSync(installedMods);
            System.out.println("All mods have been written to the '" + config.fileName + ".json' file!");
            toValidate = installedModData;
            return;
        }

        var syncedMods = fileProcessor.getSyncedMods();
        var syncedModData = AlmostPacked.GSON.fromJson(syncedMods, ModData[].class);

        if (Arrays.equals(installedModData, syncedModData)) {
            System.out.println("No sync changes detected!");
            if (AlmostPacked.isMerging()) {
                toValidate = installedModData;
            }
            return;
        }

        if (AlmostPacked.isMerging()) {
            System.out.println("Changes in the sync file detected! Updating instance...");
            fileProcessor.overwriteInstance();
            toValidate = syncedModData;
            return;
        }

        if (AlmostPacked.isPushing()) {
            System.out.println("Changes in the instance detected! Updating sync file...");

            printDevLog(installedMods.size() + " mods found inside 'minecraftinstance.json':");
            var installedContainer = createContainer(installedMods, installedModData);
            printDevLog("\n");
            printDevLog(syncedMods.size() + " mods found inside '" + config.fileName + ".json':");
            var syncedContainer = createContainer(syncedMods, syncedModData);

            Set<Integer> modsToRemove = new HashSet<>(syncedContainer.keySet());
            installedContainer.forEach((projectId, installedMod) -> {
                modsToRemove.remove(projectId);
                var syncedMod = syncedContainer.get(projectId);
                if (syncedMod == null || !syncedMod.modData().equals(installedMod.modData())) {
                    syncedContainer.put(projectId, installedMod);
                }
            });
            modsToRemove.forEach(syncedContainer::remove);

            printDevLog("\n");
            printDevLog(syncedContainer.size() + " mods after syncing:");
            syncedContainer.forEach((projectId, mod) -> printDevLog("\t - " + mod.modData()));

            var syncArray = new JsonArray();
            syncedContainer.values().forEach(mod -> syncArray.add(mod.json));
            fileProcessor.overwriteSync(syncArray);
        }
    }

    void validate() {
        if (toValidate == null) return;
        AlmostPacked.printHeader("Checking pack completeness...");

        System.out.println("Downloading missing mods...");
        downloadMissingMods();
        System.out.println();

        System.out.println("Deleting removed mods...");
        deleteRemovedMods();
    }

    private Map<Integer, ModDataContainer> createContainer(JsonArray mods, ModData... modData) {
        Map<Integer, ModDataContainer> container = new HashMap<>();
        for (var i = 0; i < mods.size(); i++) {
            var modDataContainer = new ModDataContainer(mods.get(i), modData[i]);
            container.put(modDataContainer.modData().installedFile.getProjectId(), modDataContainer);
            printDevLog("\t - " + modDataContainer.modData());
        }
        return container;
    }

    @SuppressWarnings("java:S2142")
    private void downloadMissingMods() {
        executor = Executors.newFixedThreadPool(config.concurrentDownloads);
        var startTime = System.currentTimeMillis();

        Set<AddonFile> toDownload = new HashSet<>();
        Set<File> toEnable = new HashSet<>();
        Set<File> toDisable = new HashSet<>();
        for (var data : toValidate) {
            var modFile = data.installedFile;
            if (modFile == null) continue;

            var fileName = modFile.getFileNameOnDisk();
            var file = new File(modDir, fileName);

            if (file.exists()) {
                removalFilter.add(fileName);
                continue;
            }

            if (modFile.isDisabled()) {
                var enabledFile = new File(modDir, modFile.getFileName());
                toDisable.add(enabledFile);
                if (enabledFile.exists()) continue;
            } else {
                var disabledFile = new File(modDir, modFile.getFileName() + ".disabled");
                if (disabledFile.exists()) {
                    toEnable.add(disabledFile);
                    continue;
                }
            }
            toDownload.add(modFile);
        }

        toDownload.forEach(mod -> download(new File(modDir, mod.getFileName()), mod.getDownloadUrl()));

        try {
            executor.shutdown();
            // noinspection ResultOfMethodCallIgnored
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            System.out.println("Downloads were interrupted!");
            e.printStackTrace();
        }

        if (downloadCount == 0) {
            System.out.println("No mods needed to be downloaded!");
        } else {
            var elapsedTime = (System.currentTimeMillis() - startTime) / 1_000F;
            System.out.printf("Downloaded %d mods! Duration: %.2fs%n", downloadCount, elapsedTime);
        }

        toEnable.forEach(modFile -> {
            var enabled = new File(modDir, modFile.getName().replace(".disabled", ""));
            if (modFile.renameTo(enabled)) {
                System.out.println("Enabled " + enabled.getName());
            } else {
                System.out.println("Failed to enable " + enabled.getName());
            }
            removalFilter.add(enabled.getName());
        });
        toDisable.forEach(modFile -> {
            var disabled = new File(modDir, modFile.getName() + ".disabled");
            if (modFile.renameTo(disabled)) {
                System.out.println("Disabled " + modFile.getName());
            } else {
                System.out.println("Failed to disable " + modFile.getName());
            }
            removalFilter.add(disabled.getName());
        });
    }

    private void download(File modFile, String downloadUrl) {
        Runnable downloadTask = () -> {
            var modName = modFile.getName();
            System.out.println("Downloading " + modName);

            try {
                var time = System.currentTimeMillis();
                var url = new URL(downloadUrl);
                var out = new FileOutputStream(modFile);
                var connection = url.openConnection();
                var in = connection.getInputStream();
                var buf = new byte[4_096];

                var read = 0;
                do {
                    out.write(buf, 0, read);
                    read = in.read(buf);
                } while (read > 0);

                out.close();
                in.close();

                var secs = (System.currentTimeMillis() - time) / 1_000F;
                System.out.printf("Downloaded %s (took %.2fs)%n", modName, secs);
            } catch (Exception e) {
                System.out.println("Failed to download " + modName);
                e.printStackTrace();
            }
        };

        removalFilter.add(modFile.getName());
        downloadCount++;
        executor.submit(downloadTask);
    }

    private void deleteRemovedMods() {
        var toRemove = modDir.listFiles(f -> !f.isDirectory() && !removalFilter.contains(f.getName()));
        if (toRemove == null || toRemove.length == 0) {
            System.out.println("No mods needed to be removed!");
            return;
        }

        var removed = 0;
        for (var mod : toRemove) {
            System.out.println("Removing " + mod.getName());
            try {
                Files.delete(mod.toPath());
                System.out.println("Removed " + mod.getName());
                removed++;
            } catch (IOException e) {
                System.out.println("Failed to download " + mod.getName());
            }
        }

        System.out.println("Deleted " + removed + " old mods!");
    }

    private void printDevLog(String message) {
        if (!config.devMode) return;
        System.out.println(message);
    }

    private record ModDataContainer(JsonElement json, ModData modData) {}
}
