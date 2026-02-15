package fr.zayzx.zayzutils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class IOUtils {

    private static final int BUFFER = 8192;

    private IOUtils() {}

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }

    public static String toString(InputStream in) throws IOException {
        return new String(toByteArray(in), StandardCharsets.UTF_8);
    }

    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try { c.close(); } catch (IOException ignored) {}
        }
    }
}
