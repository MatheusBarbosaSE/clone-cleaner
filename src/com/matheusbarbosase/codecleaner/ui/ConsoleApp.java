package com.matheusbarbosase.codecleaner.ui;

import com.matheusbarbosase.codecleaner.core.DuplicateFinder;
import com.matheusbarbosase.codecleaner.core.FileHasher;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Minimal CLI entry point for manual runs.
 */
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

        System.out.println("Scanner is not implemented yet. Root: " + root.toAbsolutePath());
    }

    private static void printBanner() {
        System.out.println("=======================================");
        System.out.println("            CODE CLEANER");
        System.out.println("       Duplicate Files Detector");
        System.out.println("=======================================");
    }
}
