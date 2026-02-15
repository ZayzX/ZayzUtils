package fr.zayzx.zayzutils;

import com.google.gson.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class JsonUtils {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private JsonUtils() {}

    /* =========================
       STRING <-> JSON
       ========================= */

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /* =========================
       FILE <-> JSON
       ========================= */

    public static <T> T read(Path path, Class<T> clazz) {
        try {
            return GSON.fromJson(Files.readString(path), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(Path path, Object obj) {
        try {
            if (path.getParent() != null && Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, GSON.toJson(obj));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* =========================
       ASYNC WITH CALLBACK
       ========================= */

    public static <T> void readAsync(Path path, Class<T> clazz, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        AsyncUtils.runAsync(() -> {
            try {
                T obj = read(path, clazz);
                onSuccess.accept(obj);
            } catch (Throwable t) {
                onError.accept(t);
            }
        });
    }

    public static void writeAsync(Path path, Object obj, Runnable onSuccess, Consumer<Throwable> onError) {
        AsyncUtils.runAsync(() -> {
            try {
                write(path, obj);
                if (onSuccess != null) onSuccess.run();
            } catch (Throwable t) {
                onError.accept(t);
            }
        });
    }
}
