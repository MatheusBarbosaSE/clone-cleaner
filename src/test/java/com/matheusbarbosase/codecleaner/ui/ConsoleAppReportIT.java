package com.matheusbarbosase.codecleaner.ui;

import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleAppReportIT {

  @Test
  void txtReport_shouldContainKeepAndDelLines() throws Exception {
    Path dir = Files.createTempDirectory("cc_it_rep_txt_");
    // create two duplicate files
    Path a = Files.writeString(dir.resolve("a.txt"), "dup");
    Path b = Files.writeString(dir.resolve("b.txt"), "dup");
    Path txt = dir.resolve("report.txt");

    ProcessResult r = runApp(List.of(
        dir.toString(),
        "--no-color",
        "--report=" + txt.toString() // generate TXT report
    ), null); // no stdin (dry-run)

    assertEquals(0, r.exitCode, "app must exit 0");
    assertTrue(Files.exists(txt), "TXT report should be created");

    String content = Files.readString(txt);
    // minimal invariants of the TXT report
    assertTrue(content.contains("Hash: "), "TXT should list group hash");
    assertTrue(content.contains("KEEP -> "), "TXT should show KEEP line");
    assertTrue(content.contains("DEL  -> "), "TXT should show DEL line");
    // file names should appear (relative paths by default)
    assertTrue(content.contains(a.getFileName().toString()), "TXT should list 'a' file");
    assertTrue(content.contains(b.getFileName().toString()), "TXT should list 'b' file");
  }

  @Test
  void csvReport_shouldHaveHeaderAndRows() throws Exception {
    Path dir = Files.createTempDirectory("cc_it_rep_csv_");
    Path a = Files.writeString(dir.resolve("one.txt"), "same");
    Path b = Files.writeString(dir.resolve("two.txt"), "same");
    Path csv = dir.resolve("report.csv");

    ProcessResult r = runApp(List.of(
        dir.toString(),
        "--no-color",
        "--csv=" + csv.toString() // generate CSV report
    ), null);

    assertEquals(0, r.exitCode, "app must exit 0");
    assertTrue(Files.exists(csv), "CSV report should be created");

    List<String> lines = Files.readAllLines(csv);
    assertFalse(lines.isEmpty(), "CSV should not be empty");
    assertEquals("hash,keep,action,path", lines.get(0), "CSV header must match");

    // Expected: one duplicate group of 2 files
    // 1 header + 1 KEEP row + 1 DEL row = 3 lines total
    // (If implementation changes to include more rows, keep the invariant checks
    // below.)
    assertTrue(lines.size() >= 3, "CSV should contain header + rows");
    String joined = String.join("\n", lines);
    assertTrue(joined.contains("KEEP"), "CSV should contain a KEEP row");
    assertTrue(joined.contains("DEL"), "CSV should contain a DEL row");
    // file names should appear
    assertTrue(joined.contains(a.getFileName().toString()), "CSV should list 'a' file");
    assertTrue(joined.contains(b.getFileName().toString()), "CSV should list 'b' file");
  }

  // helpers

  private record ProcessResult(int exitCode, String stdout, String stderr) {
  }

  private ProcessResult runApp(List<String> args, String stdin) throws IOException, InterruptedException {
    String javaBin = System.getProperty("java.home")
        + java.io.File.separator + "bin"
        + java.io.File.separator + "java";

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
