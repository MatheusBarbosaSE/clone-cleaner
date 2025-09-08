package com.matheusbarbosase.clonecleaner.core.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DuplicateGroup {
    private final String hash;
    private final List<Path> paths = new ArrayList<>();

    public DuplicateGroup(String hash) { this.hash = hash; }

    public String getHash() { return hash; }

    public void add(Path p) { paths.add(p); }

    public List<Path> getPaths() { return Collections.unmodifiableList(paths); }

    public int size() { return paths.size(); }
}
