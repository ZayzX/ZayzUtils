package fr.zayzx.zayzutils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public final class ZipUtils {

    private static final int BUFFER_SIZE = 8192;

    private ZipUtils() {}

    /* =====================================================
       ZIP
       ===================================================== */

    // Zip fichier ou dossier
    public static void zip(Path source, Path zipFile) throws IOException {
        zip(source, zipFile, Deflater.DEFAULT_COMPRESSION);
    }

    // Zip avec niveau de compression
    public static void zip(Path source, Path zipFile, int compressionLevel) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(
                new BufferedOutputStream(Files.newOutputStream(zipFile)))) {

            zos.setLevel(compressionLevel);
            Path basePath = source.toAbsolutePath();

            if (Files.isDirectory(source)) {
                Files.walk(source)
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            ZipEntry entry = new ZipEntry(
                                    basePath.relativize(path).toString().replace("\\", "/"));
                            try {
                                zos.putNextEntry(entry);
                                Files.copy(path, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
            } else {
                ZipEntry entry = new ZipEntry(source.getFileName().toString());
                zos.putNextEntry(entry);
                Files.copy(source, zos);
                zos.closeEntry();
            }
        }
    }

    /* =====================================================
       UNZIP
       ===================================================== */

    public static void unzip(Path zipFile, Path targetDir) throws IOException {
        unzip(zipFile, targetDir, null);
    }

    // Unzip avec filtre (ex: extension)
    public static void unzip(Path zipFile, Path targetDir, ZipEntryFilter filter) throws IOException {

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        try (ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(Files.newInputStream(zipFile)))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                if (filter != null && !filter.accept(entry)) {
                    zis.closeEntry();
                    continue;
                }

                Path newPath = secureResolve(targetDir, entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());
                    try (OutputStream os = Files.newOutputStream(newPath)) {
                        copy(zis, os);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    /* =====================================================
       INFO / LECTURE
       ===================================================== */

    // Liste des fichiers dans le zip
    public static List<String> listEntries(Path zipFile) throws IOException {
        List<String> entries = new ArrayList<>();
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            zip.stream().forEach(e -> entries.add(e.getName()));
        }
        return entries;
    }

    // Vérifie si une entrée existe
    public static boolean contains(Path zipFile, String entryName) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            return zip.getEntry(entryName) != null;
        }
    }

    // Taille totale décompressée
    public static long getUncompressedSize(Path zipFile) throws IOException {
        long size = 0;
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                if (!e.isDirectory() && e.getSize() > 0) {
                    size += e.getSize();
                }
            }
        }
        return size;
    }

    /* =====================================================
       EXTRACTION CIBLÉE
       ===================================================== */

    // Extraire un seul fichier
    public static void extractEntry(Path zipFile, String entryName, Path output) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            ZipEntry entry = zip.getEntry(entryName);
            if (entry == null) return;

            Files.createDirectories(output.getParent());
            try (InputStream is = zip.getInputStream(entry);
                 OutputStream os = Files.newOutputStream(output)) {
                copy(is, os);
            }
        }
    }

    /* =====================================================
       VALIDATION
       ===================================================== */

    // Vérifie si le zip est valide
    public static boolean isValidZip(Path zipFile) {
        try (ZipFile ignored = new ZipFile(zipFile.toFile())) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /* =====================================================
       UTILITAIRES
       ===================================================== */

    private static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
    }

    // Protection Zip Slip
    private static Path secureResolve(Path targetDir, String entryName) throws IOException {
        Path resolved = targetDir.resolve(entryName).normalize();
        if (!resolved.startsWith(targetDir)) {
            throw new IOException("Zip entry malveillante : " + entryName);
        }
        return resolved;
    }

    /* =====================================================
       INTERFACES
       ===================================================== */

    @FunctionalInterface
    public interface ZipEntryFilter {
        boolean accept(ZipEntry entry);
    }
}
