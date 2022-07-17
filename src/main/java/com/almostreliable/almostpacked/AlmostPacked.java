package com.almostreliable.almostpacked;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.stream.Stream;

@SuppressWarnings("CallToPrintStackTrace")
public final class AlmostPacked {

    static final Gson GSON = new Gson();
    static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String VERSION = AlmostPacked.class.getPackage().getImplementationVersion();
    @SuppressWarnings({"NonConstantFieldWithUpperCaseName", "RedundantFieldInitialization"})
    private static String FLAG = null;
    private static boolean SHOULD_FAIL = false;

    private AlmostPacked() {}

    public static void main(String... args) {
        System.out.println();
        System.out.println("AlmostPacked v" + VERSION);
        System.out.println("Author: Almost Reliable");

        // help for invalid usage
        if (args.length != 1 || Stream.of("-m", "--merge", "-p", "--push").noneMatch(args[0]::equals)) {
            System.out.println("\nNot enough arguments provided!");
            System.out.println("Usage: java -jar AlmostPacked.jar <flag>");
            System.out.println("Flags:");
            System.out.println("  -m, --merge\t\texecutes the post-merge tasks");
            System.out.println("  -p, --push\t\texecutes the pre-push tasks");
            System.exit(1);
        }

        FLAG = args[0];
        var startTime = System.currentTimeMillis();

        var fileProcessor = new FileProcessor();
        try {
            fileProcessor.readConfig();

            printHeader("Processing...");
            fileProcessor.readFiles();
            fileProcessor.readDirectories();
            fileProcessor.readMods();

            var packVerifier = new PackValidator(fileProcessor);
            packVerifier.handleSyncing();
            packVerifier.validate();
        } catch (IOException e) {
            System.out.println("\nERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        var elapsedTime = (System.currentTimeMillis() - startTime) / 1_000F;
        printHeader("Done! " + String.format("Duration: %.2fs", elapsedTime));

        if (fileProcessor.getConfig().failOnChange && SHOULD_FAIL) {
            printHeader("Hook failed because the sync file was changed!\nReview the changes.");
            System.exit(1);
        }
    }

    static void abort(String message) {
        System.out.println(message);
        System.out.println("Aborting...");
    }

    static void printHeader(String message) {
        System.out.println("\n\n############################################");
        System.out.println(message + "\n");
    }

    static void shouldFail() {
        SHOULD_FAIL = true;
    }

    static boolean isMerging() {
        assert FLAG != null;
        return FLAG.equals("-m") || FLAG.equals("--merge");
    }

    static boolean isPushing() {
        assert FLAG != null;
        return FLAG.equals("-p") || FLAG.equals("--push");
    }
}
