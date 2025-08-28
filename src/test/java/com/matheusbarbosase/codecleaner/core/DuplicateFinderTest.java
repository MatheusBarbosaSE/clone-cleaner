package com.matheusbarbosase.codecleaner.core;

import com.matheusbarbosase.codecleaner.core.model.DuplicateGroup;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateFinderTest {

  @Test
  void findsDuplicatesByContent() throws Exception {
    Path dir = Files.createTempDirectory("cc_dir_");

    Path a = dir.resolve("a.txt");
    Path b = dir.resolve("b.txt");
    Path c = dir.resolve("c.txt");

    Files.writeString(a, "same");
    Files.writeString(b, "same");
    Files.writeString(c, "diff");

    DuplicateFinder finder = new DuplicateFinder(new FileHasher());
    List<DuplicateGroup> groups = finder.findDuplicates(dir);

    assertEquals(1, groups.size());
    DuplicateGroup g = groups.get(0);
    assertEquals(2, g.size());
    assertTrue(g.getPaths().contains(a));
    assertTrue(g.getPaths().contains(b));
    assertFalse(g.getPaths().contains(c));
  }
}
