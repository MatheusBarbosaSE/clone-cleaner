package com.matheusbarbosase.clonecleaner.core;

import com.matheusbarbosase.clonecleaner.core.model.DuplicateGroup;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class DuplicateFinder {

    private final FileHasher hasher;

    public DuplicateFinder(FileHasher hasher) {
        this.hasher = Objects.requireNonNull(hasher);
    }

    public List<DuplicateGroup> findDuplicates(Path root) throws IOException {
        Map<String, DuplicateGroup> byHash = new LinkedHashMap<>();

        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!attrs.isRegularFile()) return FileVisitResult.CONTINUE;
                String h = hasher.sha256(file);
                byHash.computeIfAbsent(h, DuplicateGroup::new).add(file);
                return FileVisitResult.CONTINUE;
            }
        });

        List<DuplicateGroup> groups = new ArrayList<>();
        for (DuplicateGroup g : byHash.values()) {
            if (g.size() > 1) groups.add(g);
        }
        return groups;
    }
}
