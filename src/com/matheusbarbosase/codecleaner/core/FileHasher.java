package com.matheusbarbosase.codecleaner.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Computes cryptographic digests for files.
 * Uses SHA-256 to enable reliable duplicate detection.
 */
public class FileHasher {

    /**
     * Returns the SHA-256 hex digest for the given file.
     * @param file path to a regular file
     * @return lowercase hex string
     * @throws IOException if the file cannot be read
     */
    public String sha256(Path file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(file);
                 DigestInputStream din = new DigestInputStream(in, md)) {
                byte[] buffer = new byte[8192];
                while (din.read(buffer) != -1) { /* digest is updated */ }
            }
            return toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >>> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
