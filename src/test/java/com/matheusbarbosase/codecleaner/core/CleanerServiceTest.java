package com.matheusbarbosase.codecleaner.core;

import com.matheusbarbosase.codecleaner.core.model.DuplicateGroup;
import com.matheusbarbosase.codecleaner.core.model.KeepPolicy;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CleanerServiceTest {

  @Test
  void selectToKeepRespectsPolicy() throws Exception {
    Path dir = Files.createTempDirectory("cc_sel_");
    Path old = Files.writeString(dir.resolve("1.txt"), "x");
    Thread.sleep(5);
    Path newer = Files.writeString(dir.resolve("2.txt"), "x");

    DuplicateGroup group = new DuplicateGroup("dummy", List.of(old, newer));
    CleanerService cleaner = new CleanerService();

    Path keepFirst = cleaner.selectToKeep(group, KeepPolicy.FIRST);
    Path keepNewest = cleaner.selectToKeep(group, KeepPolicy.NEWEST);

    assertEquals(old, keepFirst);
    assertEquals(newer, keepNewest);
  }

  @Test
  void deleteAllExceptRemovesOthers() throws Exception {
    Path dir = Files.createTempDirectory("cc_del_");
    Path k = Files.writeString(dir.resolve("keep.txt"), "x");
    Path d1 = Files.writeString(dir.resolve("d1.txt"), "x");
    Path d2 = Files.writeString(dir.resolve("d2.txt"), "x");

    DuplicateGroup group = new DuplicateGroup("dummy", List.of(k, d1, d2));
    CleanerService cleaner = new CleanerService();

    int deleted = cleaner.deleteAllExcept(group, k);

    assertEquals(2, deleted);
    assertTrue(Files.exists(k));
    assertFalse(Files.exists(d1));
    assertFalse(Files.exists(d2));
  }
}
