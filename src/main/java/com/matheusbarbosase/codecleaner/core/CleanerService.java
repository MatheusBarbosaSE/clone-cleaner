package com.matheusbarbosase.codecleaner.core;

import com.matheusbarbosase.codecleaner.core.model.DuplicateGroup;
import com.matheusbarbosase.codecleaner.core.model.KeepPolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;

public class CleanerService {

    public int deleteAllExcept(DuplicateGroup group, Path fileToKeep) throws IOException {
        if (group.getPaths().stream().noneMatch(p -> p.equals(fileToKeep))) {
            throw new IllegalArgumentException("fileToKeep is not part of the group");
        }
        int deleted = 0;
        for (Path p : group.getPaths()) {
            if (!p.equals(fileToKeep)) {
                Files.deleteIfExists(p);
                deleted++;
            }
        }
        return deleted;
    }

    public Path selectToKeep(DuplicateGroup group, KeepPolicy policy) throws IOException {
        return switch (policy) {
            case FIRST -> group.getPaths().stream()
                    .min(Comparator.comparing(Path::toString))
                    .orElseThrow();
            case NEWEST -> newest(group.getPaths());
        };
    }

    private Path newest(List<Path> paths) throws IOException {
        Path newest = null;
        FileTime tMax = null;
        for (Path p : paths) {
            FileTime t = Files.getLastModifiedTime(p);
            if (tMax == null || t.compareTo(tMax) > 0) {
                tMax = t;
                newest = p;
            }
        }
        if (newest == null) throw new IllegalStateException("empty paths");
        return newest;
    }
}
