package com.matheusbarbosase.codecleaner.core.model;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Immutable set of files that share the same content hash.
 */
public class DuplicateGroup {
    private final String hash;
    private final List<Path> paths;

    public DuplicateGroup(String hash, List<Path> paths) {
        this.hash = hash;
        this.paths = List.copyOf(paths);
    }

    public String getHash() {
        return hash;
    }

    public List<Path> getPaths() {
        return Collections.unmodifiableList(paths);
    }

    public int size() {
        return paths.size();
    }

    @Override
    public String toString() {
        return "DuplicateGroup{hash=" + hash + ", count=" + paths.size() + "}";
    }
}
