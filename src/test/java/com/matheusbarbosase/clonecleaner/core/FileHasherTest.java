package com.matheusbarbosase.clonecleaner.core;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class FileHasherTest {
    @Test void sameContentSameHash() throws Exception {
        Path a = Files.createTempFile("cc_", ".txt");
        Path b = Files.createTempFile("cc_", ".txt");
        Files.writeString(a, "hello"); Files.writeString(b, "hello");
        String h1 = new FileHasher().sha256(a);
        String h2 = new FileHasher().sha256(b);
        assertEquals(h1, h2);
    }
    @Test void differentContentDifferentHash() throws Exception {
        Path a = Files.createTempFile("cc_", ".txt");
        Path b = Files.createTempFile("cc_", ".txt");
        Files.writeString(a, "hello"); Files.writeString(b, "world");
        assertNotEquals(new FileHasher().sha256(a), new FileHasher().sha256(b));
    }
}
