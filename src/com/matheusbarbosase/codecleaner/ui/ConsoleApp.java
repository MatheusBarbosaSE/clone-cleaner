package com.matheusbarbosase.codecleaner.ui;

import com.matheusbarbosase.codecleaner.core.DuplicateFinder;
import com.matheusbarbosase.codecleaner.core.FileHasher;

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
            var bySize = finder.groupBySize(root);

            long groups = bySize.values().stream().filter(list -> list.size() >= 2).count();
            long filesInGroups = bySize.values().stream().filter(list -> list.size() >= 2).mapToLong(List::size).sum();

            System.out.println("Scan root: " + root.toAbsolutePath());
            System.out.println("Candidate groups (same size >= 2): " + groups);
            System.out.println("Candidate files: " + filesInGroups);
            System.out.println();

            bySize.entrySet().stream()
                    .filter(e -> e.getValue().size() >= 2)
                    .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                    .limit(20)
                    .forEach(e -> {
                        System.out.println("Size: " + e.getKey() + " bytes | Files: " + e.getValue().size());
                        e.getValue().stream().limit(5).forEach(p -> System.out.println("  - " + p));
                        int remaining = e.getValue().size() - 5;
                        if (remaining > 0) System.out.println("  ... +" + remaining + " more");
                        System.out.println();
                    });

            System.out.println("Next: content hashing to confirm duplicates.");
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
