package com.matheusbarbosase.clonecleaner.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class FileHasher {

    public String sha256(Path file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(file);
                 DigestInputStream dis = new DigestInputStream(in, md)) {
                dis.transferTo(OutputStreamNull.INSTANCE);
            }
            return HexFormat.of().formatHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    // Tiny sink to read stream fully
    private static final class OutputStreamNull extends java.io.OutputStream {
        static final OutputStreamNull INSTANCE = new OutputStreamNull();
        @Override public void write(int b) {}
    }
}
