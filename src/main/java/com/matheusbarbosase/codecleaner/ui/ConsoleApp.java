package com.matheusbarbosase.codecleaner.ui;

import com.matheusbarbosase.codecleaner.core.CleanerService;
import com.matheusbarbosase.codecleaner.core.DuplicateFinder;
import com.matheusbarbosase.codecleaner.core.FileHasher;
import com.matheusbarbosase.codecleaner.core.model.DuplicateGroup;
import com.matheusbarbosase.codecleaner.core.model.KeepPolicy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ConsoleApp {

    // ANSI (simple, optional)
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String DIM = "\u001B[2m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";

    private static final Scanner IN = new Scanner(System.in);

    public static void main(String[] args) {
        boolean useColor = !hasFlag(args, "--no-color");

        printBanner(useColor);

        if (args.length == 0) {
            println(useColor, YELLOW,
                    "Usage: java ConsoleApp <directory> [--keep=first|newest] [--delete] [--report=path.txt] [--csv=path.csv] [--absolute] [--no-color]");
            return;
        }

        Path root = Path.of(args[0]);
        if (!Files.isDirectory(root)) {
            println(useColor, RED, "Not a directory: " + root);
            return;
        }

        KeepPolicy policy = parseKeepPolicy(args);
        boolean doDelete = hasFlag(args, "--delete");
        boolean useAbsolute = hasFlag(args, "--absolute");
        Path reportPath = parseReportPath(args);
        Path csvPath = parseCsvPath(args);

        var hasher = new FileHasher();
        var finder = new DuplicateFinder(hasher);
        var cleaner = new CleanerService();

        try {
            List<DuplicateGroup> groups = finder.findDuplicates(root);

            long totalFilesInGroups = groups.stream().mapToLong(g -> g.getPaths().size()).sum();
            println(useColor, CYAN, "Scan root: " + root.toAbsolutePath());
            println(useColor, CYAN, "Duplicate groups (by content): " + groups.size());
            println(useColor, CYAN, "Files in duplicate groups: " + totalFilesInGroups);
            println(useColor, CYAN, "Keep policy: " + policy
                    + (doDelete ? " | MODE: DELETE" : " | MODE: DRY-RUN")
                    + (useAbsolute ? " | PATHS: ABSOLUTE" : " | PATHS: RELATIVE"));
            System.out.println();

            long plannedDeletes = 0;
            long plannedBytes = 0;

            for (DuplicateGroup g : groups) {
                var keep = cleanerSelect(cleaner, g, policy);
                var delCount = g.size() - 1;
                plannedDeletes += delCount;

                println(useColor, BOLD + BLUE, "Hash: " + g.getHash() + " | Files: " + g.size());

                println(useColor, GREEN, "  KEEP -> " + fp(root, keep, useAbsolute));
                int shown = 0;
                for (var p : g.getPaths()) {
                    if (!p.equals(keep)) {
                        println(useColor, RED, "  DEL  -> " + fp(root, p, useAbsolute));
                        plannedBytes += safeSize(p);
                        shown++;
                        if (shown >= 10 && delCount > 10) {
                            println(useColor, DIM, "  ... +" + (delCount - 10) + " more");
                            break;
                        }
                    }
                }
                System.out.println();
            }

            println(useColor, MAGENTA, "Planned deletions: " + plannedDeletes);
            println(useColor, MAGENTA, "Potential space to reclaim: " + formatBytes(plannedBytes));
            System.out.println();

            if (reportPath != null) {
                writeTxtReport(reportPath, groups, policy, cleaner, root, useAbsolute);
                println(useColor, GREEN, "TXT report saved to: " + reportPath.toAbsolutePath());
            }

            if (csvPath != null) {
                writeCsvReport(csvPath, groups, policy, cleaner, root, useAbsolute);
                println(useColor, GREEN, "CSV report saved to: " + csvPath.toAbsolutePath());
            }

            if (!doDelete || plannedDeletes == 0) {
                println(useColor, YELLOW, "No deletion performed. Use --delete to apply.");
                return;
            }

            if (!confirm(useColor)) {
                println(useColor, YELLOW, "Aborted by user. No files were deleted.");
                return;
            }

            long deleted = 0;
            for (DuplicateGroup g : groups) {
                var keep = cleanerSelect(cleaner, g, policy);
                deleted += cleaner.deleteAllExcept(g, keep);
            }
            println(useColor, GREEN, "Deleted files: " + deleted);

        } catch (Exception e) {
            println(useColor, RED, "Operation failed: " + e.getMessage());
        }
    }

    private static KeepPolicy parseKeepPolicy(String[] args) {
        for (String a : args) {
            if (a.startsWith("--keep=")) {
                String v = a.substring("--keep=".length()).toLowerCase(Locale.ROOT);
                if (v.equals("newest"))
                    return KeepPolicy.NEWEST;
                if (v.equals("first"))
                    return KeepPolicy.FIRST;
                System.out.println("Unknown keep policy: " + v + " (using FIRST)");
                return KeepPolicy.FIRST;
            }
        }
        return KeepPolicy.FIRST;
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String a : args)
            if (a.equalsIgnoreCase(flag))
                return true;
        return false;
    }

    private static Path parseReportPath(String[] args) {
        for (String a : args) {
            if (a.startsWith("--report=")) {
                String p = a.substring("--report=".length());
                try {
                    return Path.of(p);
                } catch (InvalidPathException e) {
                    System.err.println("Invalid report path: " + p);
                    return null;
                }
            }
        }
        return null;
    }

    private static Path parseCsvPath(String[] args) {
        for (String a : args) {
            if (a.startsWith("--csv=")) {
                String p = a.substring("--csv=".length());
                try {
                    return Path.of(p);
                } catch (InvalidPathException e) {
                    System.err.println("Invalid CSV path: " + p);
                    return null;
                }
            }
        }
        return null;
    }

    private static boolean confirm(boolean useColor) {
        print(useColor, BOLD + YELLOW, "Type YES to confirm deletion: ");
        String line = IN.nextLine();
        return "YES".equals(line);
    }

    private static Path cleanerSelect(CleanerService cleaner, DuplicateGroup g, KeepPolicy policy) throws Exception {
        return cleaner.selectToKeep(g, policy);
    }

    private static void writeTxtReport(Path out, List<DuplicateGroup> groups, KeepPolicy policy, CleanerService cleaner,
            Path root, boolean absolute) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write("CODE CLEANER - Duplicate Report\n");
            w.write("Policy: " + policy + "\n");
            w.write("Groups: " + groups.size() + "\n");
            w.write("Paths: " + (absolute ? "ABSOLUTE" : "RELATIVE to " + root.toAbsolutePath()) + "\n\n");
            for (DuplicateGroup g : groups) {
                Path keep = cleaner.selectToKeep(g, policy);
                w.write("Hash: " + g.getHash() + " | Files: " + g.size() + "\n");
                w.write("  KEEP -> " + fp(root, keep, absolute) + "\n");
                for (Path p : g.getPaths()) {
                    if (!p.equals(keep)) {
                        w.write("  DEL  -> " + fp(root, p, absolute) + "\n");
                    }
                }
                w.write("\n");
            }
        }
    }

    private static void writeCsvReport(Path out, List<DuplicateGroup> groups, KeepPolicy policy, CleanerService cleaner,
            Path root, boolean absolute) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write("hash,keep,action,path\n"); // header
            for (DuplicateGroup g : groups) {
                Path keep = cleaner.selectToKeep(g, policy);
                String keepStr = fp(root, keep, absolute);
                // row for KEEP
                w.write(escape(g.getHash()) + "," + escape(keepStr) + ",KEEP," + escape(keepStr) + "\n");
                // rows for DEL
                for (Path p : g.getPaths()) {
                    if (!p.equals(keep)) {
                        w.write(escape(g.getHash()) + "," + escape(keepStr) + ",DEL," + escape(fp(root, p, absolute))
                                + "\n");
                    }
                }
            }
        }
    }

    private static String fp(Path root, Path p, boolean absolute) {
        if (absolute)
            return p.toAbsolutePath().toString();
        try {
            return root.toAbsolutePath().relativize(p.toAbsolutePath()).toString();
        } catch (IllegalArgumentException e) {
            return p.toAbsolutePath().toString(); // different drives
        }
    }

    private static String escape(String s) {
        boolean needQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String v = s.replace("\"", "\"\"");
        return needQuote ? "\"" + v + "\"" : v;
    }

    private static long safeSize(Path p) {
        try {
            return Files.size(p);
        } catch (IOException e) {
            return 0L;
        }
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        double v = bytes / 1024.0;
        if (v < 1024)
            return String.format(Locale.ROOT, "%.1f KB", v);
        v /= 1024.0;
        if (v < 1024)
            return String.format(Locale.ROOT, "%.1f MB", v);
        v /= 1024.0;
        return String.format(Locale.ROOT, "%.1f GB", v);
    }

    private static void printBanner(boolean useColor) {
        println(useColor, BOLD + CYAN,
                "=======================================\n" +
                "            CODE CLEANER\n" +
                "       Duplicate Files Detector\n" +
                "=======================================");
    }

    private static void println(boolean useColor, String color, String s) {
        if (useColor)
            System.out.println(color + s + RESET);
        else
            System.out.println(s);
    }

    private static void print(boolean useColor, String color, String s) {
        if (useColor)
            System.out.print(color + s + RESET);
        else
            System.out.print(s);
    }
}
