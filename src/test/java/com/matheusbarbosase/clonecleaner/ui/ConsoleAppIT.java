package com.matheusbarbosase.clonecleaner.ui;

import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleAppIT {

    @Test
    void dryRun_shouldNotDeleteFiles_andPrintPlan() throws Exception {
        Path dir = Files.createTempDirectory("cc_it_dry_");
        Path keep = Files.writeString(dir.resolve("keep.txt"), "dup");
        Path del  = Files.writeString(dir.resolve("del.txt"),  "dup");

        ProcessResult r = runApp(List.of(
                dir.toString(),
                "--no-color",
                "--report=" + dir.resolve("report.txt")
        ), null);

        assertEquals(0, r.exitCode);
        assertTrue(r.stdout.contains("Duplicate groups (by content): 1"));
        assertTrue(r.stdout.contains("Planned deletions: 1"));
        assertTrue(Files.exists(keep));
        assertTrue(Files.exists(del));
        assertTrue(Files.exists(dir.resolve("report.txt")));
    }

    @Test
    void deleteFlow_shouldDeleteOneDuplicate_afterYES() throws Exception {
        Path dir = Files.createTempDirectory("cc_it_del_");
        Path keep = Files.writeString(dir.resolve("keep.txt"), "same");
        Path del  = Files.writeString(dir.resolve("del.txt"),  "same");

        ProcessResult r = runApp(List.of(
                dir.toString(),
                "--no-color",
                "--delete"
        ), "YES\n");

        assertEquals(0, r.exitCode);
        assertTrue(r.stdout.contains("Planned deletions: 1"));
        assertTrue(r.stdout.contains("Deleted files: 1"));
        assertTrue(Files.exists(keep));
        assertFalse(Files.exists(del));
    }

    private record ProcessResult(int exitCode, String stdout, String stderr) {}

    private ProcessResult runApp(List<String> args, String stdin) throws IOException, InterruptedException {
        String javaBin = System.getProperty("java.home") + java.io.File.separator + "bin" + java.io.File.separator + "java";
        String cp = System.getProperty("java.class.path");
        java.util.ArrayList<String> cmd = new java.util.ArrayList<>();
        cmd.add(javaBin); cmd.add("-cp"); cmd.add(cp);
        cmd.add("com.matheusbarbosase.clonecleaner.ui.ConsoleApp");
        cmd.addAll(args);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(false);
        Process p = pb.start();

        if (stdin != null && !stdin.isEmpty()) {
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()))) { w.write(stdin); }
        } else { p.getOutputStream().close(); }

        String out = new String(p.getInputStream().readAllBytes());
        String err = new String(p.getErrorStream().readAllBytes());
        int code = p.waitFor();
        return new ProcessResult(code, out, err);
    }
}
