package com.matheusbarbosase.clonecleaner.core;

import com.matheusbarbosase.clonecleaner.core.model.DuplicateGroup;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateFinderTest {
    @Test void findsDuplicatesByContent() throws Exception {
        Path dir = Files.createTempDirectory("cc_dir_");
        Path a = Files.writeString(dir.resolve("a.txt"), "same");
        Path b = Files.writeString(dir.resolve("b.txt"), "same");
        Path c = Files.writeString(dir.resolve("c.txt"), "diff");
        List<DuplicateGroup> groups = new DuplicateFinder(new FileHasher()).findDuplicates(dir);
        assertEquals(1, groups.size());
        DuplicateGroup g = groups.get(0);
        assertTrue(g.getPaths().contains(a));
        assertTrue(g.getPaths().contains(b));
        assertFalse(g.getPaths().contains(c));
    }
}
