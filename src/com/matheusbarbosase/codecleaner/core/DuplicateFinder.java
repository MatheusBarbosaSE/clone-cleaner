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
 * Stage 1 exposes a cheap pre-filter by grouping files by size.
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
     * Stage 2 will refine candidates by hashing content.
     */
    public List<DuplicateGroup> findDuplicates(Path root) throws IOException {
        return List.of();
    }
}
