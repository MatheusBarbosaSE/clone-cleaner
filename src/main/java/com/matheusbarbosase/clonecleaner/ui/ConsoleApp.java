package com.matheusbarbosase.clonecleaner.ui;

import com.matheusbarbosase.clonecleaner.core.CleanerService;
import com.matheusbarbosase.clonecleaner.core.DuplicateFinder;
import com.matheusbarbosase.clonecleaner.core.FileHasher;
import com.matheusbarbosase.clonecleaner.core.model.DuplicateGroup;
import com.matheusbarbosase.clonecleaner.core.model.KeepPolicy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ConsoleApp {

    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final Scanner IN = new Scanner(System.in);

    public static void main(String[] args) {
        boolean useColor = true, delete = false, relative = true;
        KeepPolicy policy = KeepPolicy.FIRST;
        Path root = null;
        Path txtReport = null, csvReport = null;

        for (String a : args) {
            switch (a) {
                case "--no-color" -> useColor = false;
                case "--delete"   -> delete = true;
                case "--absolute" -> relative = false;
                default -> {
                    if (a.startsWith("--keep=")) {
                        String v = a.substring(7).toLowerCase(Locale.ROOT);
                        policy = v.equals("newest") ? KeepPolicy.NEWEST : KeepPolicy.FIRST;
                    } else if (a.startsWith("--report=")) {
                        txtReport = Path.of(a.substring(9));
                    } else if (a.startsWith("--csv=")) {
                        csvReport = Path.of(a.substring(6));
                    } else if (root == null) {
                        root = Path.of(a);
                    }
                }
            }
        }

        if (root == null || !Files.isDirectory(root)) {
            System.err.println("Usage: ConsoleApp <path> [--delete] [--keep=first|newest] [--report=path.txt] [--csv=path.csv] [--absolute] [--no-color]");
            return;
        }

        banner(useColor);

        FileHasher hasher = new FileHasher();
        DuplicateFinder finder = new DuplicateFinder(hasher);
        CleanerService cleaner = new CleanerService();

        List<DuplicateGroup> groups;
        try { groups = finder.findDuplicates(root); }
        catch (IOException e) { System.err.println("Scan failed: " + e.getMessage()); return; }

        int totalFiles = groups.stream().mapToInt(DuplicateGroup::size).sum();
        println(useColor, CYAN, "Scan root: " + root.toAbsolutePath());
        println(useColor, CYAN, "Duplicate groups (by content): " + groups.size());
        println(useColor, CYAN, "Files in duplicate groups: " + totalFiles);
        println(useColor, CYAN, "Keep policy: " + policy + " | MODE: " + (delete ? "DELETE" : "DRY-RUN") + " | PATHS: " + (relative ? "RELATIVE" : "ABSOLUTE"));
        System.out.println();

        long planned = 0, bytes = 0;
        for (DuplicateGroup g : groups) {
            Path keep;
            try { keep = cleaner.selectToKeep(g, policy); }
            catch (IOException e) { continue; }

            println(useColor, BOLD + CYAN, "Hash: " + g.getHash() + " | Files: " + g.size());
            System.out.println("  KEEP -> " + fp(root, keep, relative));
            for (Path p : g.getPaths()) {
                if (!p.equals(keep)) {
                    System.out.println("  DEL  -> " + fp(root, p, relative));
                    planned++;
                    try { bytes += Files.size(p); } catch (IOException ignored) {}
                }
            }
            System.out.println();
        }

        println(useColor, YELLOW, "Planned deletions: " + planned);
        System.out.printf("Potential space to reclaim: %.1f KB%n", bytes / 1024.0);
        System.out.println();

        if (txtReport != null) {
            try { writeTxtReport(txtReport, groups, root, relative, policy); println(useColor, GREEN, "TXT report: " + txtReport.toAbsolutePath()); }
            catch (IOException e) { println(useColor, RED, "TXT report failed: " + e.getMessage()); }
        }
        if (csvReport != null) {
            try { writeCsvReport(csvReport, groups, root, relative, policy); println(useColor, GREEN, "CSV report: " + csvReport.toAbsolutePath()); }
            catch (IOException e) { println(useColor, RED, "CSV report failed: " + e.getMessage()); }
        }

        if (delete && planned > 0) {
            if (confirm(useColor)) {
                long deleted = 0;
                for (DuplicateGroup g : groups) {
                    try {
                        Path keep = cleaner.selectToKeep(g, policy);
                        deleted += cleaner.deleteAllExcept(g, keep);
                    } catch (IOException ignored) {}
                }
                println(useColor, GREEN, "Deleted files: " + deleted);
            } else {
                println(useColor, YELLOW, "Deletion cancelled by user.");
            }
        }
    }

    private static boolean confirm(boolean useColor) {
        print(useColor, BOLD + YELLOW, "Type YES to confirm deletion: ");
        String line = IN.nextLine();
        return "YES".equals(line);
    }

    private static void writeTxtReport(Path out, List<DuplicateGroup> groups, Path root, boolean relative, KeepPolicy policy) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write("CLONE CLEANER - Duplicate Report\n");
            w.write("Policy: " + policy + "\n\n");
            for (DuplicateGroup g : groups) {
                w.write("Hash: " + g.getHash() + "\n");
                for (Path p : g.getPaths()) {
                    w.write("  " + fp(root, p, relative) + "\n");
                }
                w.write("\n");
            }
        }
    }

    private static void writeCsvReport(Path out, List<DuplicateGroup> groups, Path root, boolean relative, KeepPolicy policy) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write("hash,keep,action,path\n");
            for (DuplicateGroup g : groups) {
                Path keep;
                try { keep = new CleanerService().selectToKeep(g, policy); }
                catch (IOException e) { continue; }
                String keepStr = fp(root, keep, relative);
                w.write(g.getHash() + "," + keepStr + ",KEEP," + keepStr + "\n");
                for (Path p : g.getPaths()) {
                    if (!p.equals(keep)) {
                        w.write(g.getHash() + "," + keepStr + ",DEL," + fp(root, p, relative) + "\n");
                    }
                }
            }
        }
    }

    private static String fp(Path root, Path p, boolean relative) {
        if (!relative) return p.toAbsolutePath().toString();
        try { return root.toAbsolutePath().relativize(p.toAbsolutePath()).toString(); }
        catch (IllegalArgumentException e) { return p.toAbsolutePath().toString(); }
    }

    private static void banner(boolean useColor) {
        println(useColor, BOLD + CYAN,
                """
                        =======================================
                                    CLONE CLEANER
                               Duplicate Files Detector
                        =======================================""");
    }

    private static void println(boolean useColor, String color, String s) {
        if (useColor) System.out.println(color + s + RESET); else System.out.println(s);
    }
    private static void print(boolean useColor, String color, String s) {
        if (useColor) System.out.print(color + s + RESET); else System.out.print(s);
    }
}
