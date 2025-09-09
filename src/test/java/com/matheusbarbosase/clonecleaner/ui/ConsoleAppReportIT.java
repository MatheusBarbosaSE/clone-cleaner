package com.matheusbarbosase.clonecleaner.ui;

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
        Path a = Files.writeString(dir.resolve("a.txt"), "dup");
        Path b = Files.writeString(dir.resolve("b.txt"), "dup");
        Path txt = dir.resolve("report.txt");

        ProcessResult r = runApp(List.of(
                dir.toString(),
                "--no-color",
                "--report=" + txt
        ), null);

        assertEquals(0, r.exitCode);
        assertTrue(Files.exists(txt));
        String content = Files.readString(txt);
        assertTrue(content.contains("Hash: "));
        assertTrue(content.contains("KEEP -> "));
        assertTrue(content.contains("DEL  -> "));
        assertTrue(content.contains(a.getFileName().toString()));
        assertTrue(content.contains(b.getFileName().toString()));
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
                "--csv=" + csv
        ), null);

        assertEquals(0, r.exitCode);
        assertTrue(Files.exists(csv));
        List<String> lines = Files.readAllLines(csv);
        assertFalse(lines.isEmpty());
        assertEquals("hash,keep,action,path", lines.get(0));
        assertTrue(lines.size() >= 3);
        String joined = String.join("\n", lines);
        assertTrue(joined.contains("KEEP"));
        assertTrue(joined.contains("DEL"));
        assertTrue(joined.contains(a.getFileName().toString()));
        assertTrue(joined.contains(b.getFileName().toString()));
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
