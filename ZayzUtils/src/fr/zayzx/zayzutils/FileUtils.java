package fr.zayzx.zayzutils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileUtils {

    private static final int BUFFER_SIZE = 8192;

    private FileUtils() {}

    /* =====================================================
       CREATE
       ===================================================== */

    public static Path createFile(Path path) throws IOException {
        if (Files.notExists(path.getParent()))
            Files.createDirectories(path.getParent());
        return Files.createFile(path);
    }

    public static Path createDirectory(Path path) throws IOException {
        return Files.createDirectories(path);
    }

    /* =====================================================
       READ / WRITE
       ===================================================== */

    public static String readString(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    public static List<String> readLines(Path path) throws IOException {
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    public static void writeString(Path path, String content) throws IOException {
        ensureParentExists(path);
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    public static void appendString(Path path, String content) throws IOException {
        ensureParentExists(path);
        Files.writeString(path, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /* =====================================================
       COPY / MOVE
       ===================================================== */

    public static void copy(Path source, Path target) throws IOException {
        if (Files.isDirectory(source)) {
            copyDirectory(source, target);
        } else {
            ensureParentExists(target);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void move(Path source, Path target) throws IOException {
        ensureParentExists(target);
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(dir);
                Files.createDirectories(target.resolve(relative));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(file);
                Files.copy(file, target.resolve(relative),
                        StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /* =====================================================
       DELETE
       ===================================================== */

    public static void delete(Path path) throws IOException {
        if (!Files.exists(path)) return;

        if (Files.isDirectory(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } else {
            Files.deleteIfExists(path);
        }
    }

    /* =====================================================
       INFO
       ===================================================== */

    public static boolean exists(Path path) {
        return Files.exists(path);
    }

    public static boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    public static long size(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                return walk
                        .filter(Files::isRegularFile)
                        .mapToLong(p -> {
                            try {
                                return Files.size(p);
                            } catch (IOException e) {
                                return 0;
                            }
                        }).sum();
            }
        }
        return Files.size(path);
    }

    public static List<Path> list(Path directory) throws IOException {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.collect(Collectors.toList());
        }
    }

    /* =====================================================
       SEARCH
       ===================================================== */

    public static List<Path> findByExtension(Path dir, String extension) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            return walk
                    .filter(p -> p.toString().endsWith(extension))
                    .collect(Collectors.toList());
        }
    }

    /* =====================================================
       HASH
       ===================================================== */

    public static String sha256(Path path) throws IOException {
        return hash(path, "SHA-256");
    }

    public static String md5(Path path) throws IOException {
        return hash(path, "MD5");
    }

    private static String hash(Path path, String algorithm) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            try (InputStream is = Files.newInputStream(path)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /* =====================================================
       UTIL
       ===================================================== */

    private static void ensureParentExists(Path path) throws IOException {
        if (path.getParent() != null && Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
    }
}
