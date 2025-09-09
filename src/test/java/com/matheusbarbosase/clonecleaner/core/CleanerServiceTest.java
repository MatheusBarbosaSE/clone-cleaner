package com.matheusbarbosase.clonecleaner.core;

import com.matheusbarbosase.clonecleaner.core.model.DuplicateGroup;
import com.matheusbarbosase.clonecleaner.core.model.KeepPolicy;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CleanerServiceTest {
    @Test void selectToKeepPolicy() throws Exception {
        Path dir = Files.createTempDirectory("cc_sel_");
        Path old = Files.writeString(dir.resolve("1.txt"), "x");
        Thread.sleep(5);
        Path newer = Files.writeString(dir.resolve("2.txt"), "x");
        DuplicateGroup group = new DuplicateGroup("h"); group.add(old); group.add(newer);
        CleanerService cleaner = new CleanerService();
        assertEquals(old, cleaner.selectToKeep(group, KeepPolicy.FIRST));
        assertEquals(newer, cleaner.selectToKeep(group, KeepPolicy.NEWEST));
    }
    @Test void deleteAllExcept() throws Exception {
        Path dir = Files.createTempDirectory("cc_del_");
        Path keep = Files.writeString(dir.resolve("keep.txt"), "x");
        Path d1 = Files.writeString(dir.resolve("d1.txt"), "x");
        Path d2 = Files.writeString(dir.resolve("d2.txt"), "x");
        DuplicateGroup g = new DuplicateGroup("h"); g.add(keep); g.add(d1); g.add(d2);
        long deleted = new CleanerService().deleteAllExcept(g, keep);
        assertEquals(2, deleted);
        assertTrue(Files.exists(keep)); assertFalse(Files.exists(d1)); assertFalse(Files.exists(d2));
    }
}
