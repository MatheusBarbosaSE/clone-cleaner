package com.matheusbarbosase.codecleaner.core;

import com.matheusbarbosase.codecleaner.core.model.DuplicateGroup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Finds duplicate files under a root directory.
 * Stage 1: pre-filter by file size.
 * Stage 2: confirm by content hash (SHA-256).
 */
public class DuplicateFinder {

    private final FileHasher hasher;

    public DuplicateFinder(FileHasher hasher) {
        this.hasher = hasher;
    }

    /**
     * Groups regular files by size.
     * @param root scan root
     * @return map: size (bytes) -> list of files
     * @throws IOException on IO failure
     */
    public Map<Long, List<Path>> groupBySize(Path root) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .collect(Collectors.groupingBy(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }));
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException io) throw io;
            throw e;
        }
    }

    /**
     * Returns groups of files that share the same content hash.
     * Only size-candidate files are hashed.
     * @param root scan root
     * @return duplicate groups (size >= 2)
     * @throws IOException on IO failure
     */
    public List<DuplicateGroup> findDuplicates(Path root) throws IOException {
        var bySize = groupBySize(root);

        // Only hash candidate sets (same size >= 2)
        var candidates = bySize.values().stream()
                .filter(list -> list.size() >= 2)
                .toList();

        // Hash files within each candidate set and regroup by SHA-256
        var byHash = candidates.stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(p -> {
                    try {
                        return hasher.sha256(p);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));

        // Build DuplicateGroup for hashes with at least two files
        var groups = byHash.entrySet().stream()
                .filter(e -> e.getValue().size() >= 2)
                .map(e -> new DuplicateGroup(e.getKey(), e.getValue()))
                .sorted((a, b) -> Integer.compare(b.size(), a.size()))
                .toList();

        return groups;
    }
}
