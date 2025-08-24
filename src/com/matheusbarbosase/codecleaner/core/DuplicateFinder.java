package com.matheusbarbosase.codecleaner.core;

import com.matheusbarbosase.codecleaner.core.model.DuplicateGroup;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Finds duplicate files under a root directory.
 * Implementation will be added in stages.
 */
public class DuplicateFinder {

    private final FileHasher hasher;

    public DuplicateFinder(FileHasher hasher) {
        this.hasher = hasher;
    }

    /**
     * Returns groups of duplicates with size >= 2.
     * @param root directory to scan
     * @return duplicate groups
     * @throws IOException if file access fails
     */
    public List<DuplicateGroup> findDuplicates(Path root) throws IOException {
        // Implementation to be added in the next step.
        return List.of();
    }
}
