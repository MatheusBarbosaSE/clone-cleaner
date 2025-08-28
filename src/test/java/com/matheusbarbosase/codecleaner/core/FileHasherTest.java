package com.matheusbarbosase.codecleaner.core;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class FileHasherTest {

  @Test
  void sameContentProducesSameHash() throws Exception {
    Path f1 = Files.createTempFile("cc_", ".bin");
    Path f2 = Files.createTempFile("cc_", ".bin");
    Files.writeString(f1, "hello");
    Files.writeString(f2, "hello");

    FileHasher hasher = new FileHasher();
    String h1 = hasher.sha256(f1);
    String h2 = hasher.sha256(f2);

    assertEquals(h1, h2);
  }

  @Test
  void differentContentProducesDifferentHash() throws Exception {
    Path f1 = Files.createTempFile("cc_", ".bin");
    Path f2 = Files.createTempFile("cc_", ".bin");
    Files.writeString(f1, "hello");
    Files.writeString(f2, "world");

    FileHasher hasher = new FileHasher();
    String h1 = hasher.sha256(f1);
    String h2 = hasher.sha256(f2);

    assertNotEquals(h1, h2);
  }
}
