package com.matheusbarbosase.codecleaner.ui;

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
    Path del = Files.writeString(dir.resolve("del.txt"), "dup");

    ProcessResult r = runApp(List.of(
        dir.toString(), // root
        "--no-color", // disable ANSI output
        "--report=" + dir.resolve("report.txt") // force report generation
    ), /* stdin */ null);

    String out = r.stdout;
    int code = r.exitCode;

    assertEquals(0, code, "app must exit 0");
    assertTrue(out.contains("Duplicate groups (by content): 1"));
    assertTrue(out.contains("Planned deletions: 1"));

    // nothing should be deleted (without --delete)
    assertTrue(Files.exists(keep));
    assertTrue(Files.exists(del));

    // report file should be created
    assertTrue(Files.exists(dir.resolve("report.txt")));
  }

  @Test
  void deleteFlow_shouldDeleteOneDuplicate_afterYES() throws Exception {
    Path dir = Files.createTempDirectory("cc_it_del_");
    Path keep = Files.writeString(dir.resolve("keep.txt"), "same");
    Path del = Files.writeString(dir.resolve("del.txt"), "same");

    ProcessResult r = runApp(List.of(
        dir.toString(),
        "--no-color",
        "--delete" // delete mode
    ), /* stdin */ "YES\n"); // confirm in stdin

    String out = r.stdout;
    int code = r.exitCode;

    assertEquals(0, code, "app must exit 0");
    assertTrue(out.contains("Planned deletions: 1"));
    assertTrue(out.contains("Deleted files: 1"));

    // kept file remains, duplicate should be deleted
    assertTrue(Files.exists(keep));
    assertFalse(Files.exists(del), "expected duplicate file to be deleted");
  }

  // helpers

  private record ProcessResult(int exitCode, String stdout, String stderr) {
  }

  private ProcessResult runApp(List<String> args, String stdin) throws IOException, InterruptedException {
    String javaBin = System.getProperty("java.home") + java.io.File.separator + "bin" + java.io.File.separator + "java";

    // Use the test classpath (includes both main and test classes)
    String cp = System.getProperty("java.class.path");

    java.util.ArrayList<String> cmd = new java.util.ArrayList<>();
    cmd.add(javaBin);
    cmd.add("-cp");
    cmd.add(cp);
    cmd.add("com.matheusbarbosase.codecleaner.ui.ConsoleApp");
    cmd.addAll(args);

    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.redirectErrorStream(false); // capture stdout and stderr separately
    Process p = pb.start();

    if (stdin != null && !stdin.isEmpty()) {
      try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()))) {
        w.write(stdin);
      }
    } else {
      p.getOutputStream().close();
    }

    // capture output
    String out = new String(p.getInputStream().readAllBytes());
    String err = new String(p.getErrorStream().readAllBytes());
    int code = p.waitFor();

    return new ProcessResult(code, out, err);
  }
}
