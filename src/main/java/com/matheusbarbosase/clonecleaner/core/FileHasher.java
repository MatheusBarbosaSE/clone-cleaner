package com.matheusbarbosase.clonecleaner.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class responsible for hashing file content using SHA-256.
 */
public class FileHasher {

    /**
     * Computes the SHA-256 hash of a given file.
     *
     * @param file the path to the file
     * @return the hash as a hexadecimal string
     * @throws IOException              if an I/O error occurs
     * @throws NoSuchAlgorithmException if SHA-256 is not supported
     */
    public String hash(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] data = Files.readAllBytes(file);
        byte[] hash = digest.digest(data);
        return toHex(hash);
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
