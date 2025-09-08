package com.matheusbarbosase.clonecleaner.core;

import com.matheusbarbosase.clonecleaner.core.model.DuplicateGroup;
import com.matheusbarbosase.clonecleaner.core.model.KeepPolicy;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Objects;

public class CleanerService {

    public java.nio.file.Path selectToKeep(DuplicateGroup g, KeepPolicy policy) throws IOException {
        Objects.requireNonNull(g);
        if (policy == KeepPolicy.NEWEST) {
            return g.getPaths().stream()
                    .max(Comparator.comparingLong(this::lastModifiedOrZero))
                    .orElse(g.getPaths().get(0));
        }
        return g.getPaths().get(0);
    }

    public long deleteAllExcept(DuplicateGroup g, Path keep) {
        long deleted = 0;
        for (Path p : g.getPaths()) {
            if (!p.equals(keep)) {
                try {
                    Files.deleteIfExists(p);
                    deleted++;
                } catch (IOException ignored) {}
            }
        }
        return deleted;
    }

    private long lastModifiedOrZero(Path p) {
        try { return Files.getLastModifiedTime(p).toMillis(); }
        catch (IOException e) { return 0L; }
    }
}
