package com.matheusbarbosase.codecleaner.ui;

import com.matheusbarbosase.codecleaner.core.DuplicateFinder;
import com.matheusbarbosase.codecleaner.core.FileHasher;
import com.matheusbarbosase.codecleaner.core.model.DuplicateGroup;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConsoleApp {

    public static void main(String[] args) {
        printBanner();

        if (args.length == 0) {
            System.out.println("Usage: java ConsoleApp <directory>");
            return;
        }

        Path root = Path.of(args[0]);
        if (!Files.isDirectory(root)) {
            System.err.println("Not a directory: " + root);
            return;
        }

        var hasher = new FileHasher();
        var finder = new DuplicateFinder(hasher);

        try {
            List<DuplicateGroup> groups = finder.findDuplicates(root);

            long filesInGroups = groups.stream()
                    .mapToLong(g -> g.getPaths().size())
                    .sum();

            System.out.println("Scan root: " + root.toAbsolutePath());
            System.out.println("Duplicate groups (by content): " + groups.size());
            System.out.println("Files in duplicate groups: " + filesInGroups);
            System.out.println();

            groups.stream()
                    .limit(20)
                    .forEach(g -> {
                        System.out.println("Hash: " + g.getHash() + " | Files: " + g.size());
                        g.getPaths().stream().limit(5).forEach(p -> System.out.println("  - " + p));
                        int remaining = g.size() - 5;
                        if (remaining > 0) System.out.println("  ... +" + remaining + " more");
                        System.out.println();
                    });

            if (groups.isEmpty()) {
                System.out.println("No duplicates found.");
            }
        } catch (Exception e) {
            System.err.println("Scan failed: " + e.getMessage());
        }
    }

    private static void printBanner() {
        System.out.println("=======================================");
        System.out.println("            CODE CLEANER");
        System.out.println("       Duplicate Files Detector");
        System.out.println("=======================================");
    }
}
