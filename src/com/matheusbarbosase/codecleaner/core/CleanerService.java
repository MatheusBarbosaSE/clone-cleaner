package com.matheusbarbosase.codecleaner.core;

import com.matheusbarbosase.codecleaner.core.model.DuplicateGroup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Deletion operations for duplicate groups.
 */
public class CleanerService {

    /**
     * Deletes all files in the group except the given one.
     * @param group duplicate group
     * @param fileToKeep a path contained in the group
     * @return number of deleted files
     * @throws IOException on deletion failure
     * @throws IllegalArgumentException if fileToKeep is not in the group
     */
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
}
