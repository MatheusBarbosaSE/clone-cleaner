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

    public static void main(String[] args) {
        printBanner();

        if (args.length == 0) {
            System.out.println("Usage: java ConsoleApp <directory> [--keep=first|newest] [--delete] [--report=path.txt] [--csv=path.csv] [--absolute]");
            return;
        }

        Path root = Path.of(args[0]);
        if (!Files.isDirectory(root)) {
            System.err.println("Not a directory: " + root);
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
            System.out.println("Scan root: " + root.toAbsolutePath());
            System.out.println("Duplicate groups (by content): " + groups.size());
            System.out.println("Files in duplicate groups: " + totalFilesInGroups);
            System.out.println("Keep policy: " + policy + (doDelete ? " | MODE: DELETE" : " | MODE: DRY-RUN")
                    + (useAbsolute ? " | PATHS: ABSOLUTE" : " | PATHS: RELATIVE"));
            System.out.println();

            long plannedDeletes = 0;
            for (DuplicateGroup g : groups) {
                var keep = cleanerSelect(cleaner, g, policy);
                var delCount = g.size() - 1;
                plannedDeletes += delCount;

                System.out.println("Hash: " + g.getHash() + " | Files: " + g.size());
                System.out.println("  KEEP -> " + fp(root, keep, useAbsolute));
                int shown = 0;
                for (var p : g.getPaths()) {
                    if (!p.equals(keep)) {
                        System.out.println("  DEL  -> " + fp(root, p, useAbsolute));
                        shown++;
                        if (shown >= 10 && delCount > 10) {
                            System.out.println("  ... +" + (delCount - 10) + " more");
                            break;
                        }
                    }
                }
                System.out.println();
            }

            System.out.println("Planned deletions: " + plannedDeletes);

            if (reportPath != null) {
                writeTxtReport(reportPath, groups, policy, cleaner, root, useAbsolute);
                System.out.println("TXT report saved to: " + reportPath.toAbsolutePath());
            }

            if (csvPath != null) {
                writeCsvReport(csvPath, groups, policy, cleaner, root, useAbsolute);
                System.out.println("CSV report saved to: " + csvPath.toAbsolutePath());
            }

            if (!doDelete || plannedDeletes == 0) {
                System.out.println("No deletion performed. Use --delete to apply.");
                return;
            }

            if (!confirm()) {
                System.out.println("Aborted by user. No files were deleted.");
                return;
            }

            long deleted = 0;
            for (DuplicateGroup g : groups) {
                var keep = cleanerSelect(cleaner, g, policy);
                deleted += cleaner.deleteAllExcept(g, keep);
            }
            System.out.println("Deleted files: " + deleted);

        } catch (Exception e) {
            System.err.println("Operation failed: " + e.getMessage());
        }
    }

    private static KeepPolicy parseKeepPolicy(String[] args) {
        for (String a : args) {
            if (a.startsWith("--keep=")) {
                String v = a.substring("--keep=".length()).toLowerCase(Locale.ROOT);
                if (v.equals("newest")) return KeepPolicy.NEWEST;
                if (v.equals("first")) return KeepPolicy.FIRST;
                System.out.println("Unknown keep policy: " + v + " (using FIRST)");
                return KeepPolicy.FIRST;
            }
        }
        return KeepPolicy.FIRST;
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String a : args) if (a.equalsIgnoreCase(flag)) return true;
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

    private static boolean confirm() {
        System.out.print("Type YES to confirm deletion: ");
        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();
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
                        w.write(escape(g.getHash()) + "," + escape(keepStr) + ",DEL," + escape(fp(root, p, absolute)) + "\n");
                    }
                }
            }
        }
    }

    private static String fp(Path root, Path p, boolean absolute) {
        if (absolute) return p.toAbsolutePath().toString();
        try {
            return root.toAbsolutePath().relativize(p.toAbsolutePath()).toString();
        } catch (IllegalArgumentException e) {
            // Different roots/drives: fallback to absolute
            return p.toAbsolutePath().toString();
        }
    }

    private static String escape(String s) {
        boolean needQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String v = s.replace("\"", "\"\"");
        return needQuote ? "\"" + v + "\"" : v;
    }

    private static void printBanner() {
        System.out.println("=======================================");
        System.out.println("            CODE CLEANER");
        System.out.println("       Duplicate Files Detector");
        System.out.println("=======================================");
    }
}
