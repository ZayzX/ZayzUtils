package fr.zayzx.zayzutils;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.Consumer;

public final class HttpUtils {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private HttpUtils() {}

    /* =========================
       SIMPLE GET
       ========================= */

    public static String get(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            return CLIENT.send(request, BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void get(String url, Consumer<String> onSuccess, Consumer<Throwable> onError) {
        AsyncUtils.runAsync(() -> {
            try {
                String body = get(url);
                onSuccess.accept(body);
            } catch (Throwable t) {
                onError.accept(t);
            }
        });
    }

    /* =========================
       SIMPLE POST
       ========================= */

    public static String post(String url, String body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(BodyPublishers.ofString(body))
                    .header("Content-Type", "application/json")
                    .build();

            return CLIENT.send(request, BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void post(String url, String body, Consumer<String> onSuccess, Consumer<Throwable> onError) {
        AsyncUtils.runAsync(() -> {
            try {
                String res = post(url, body);
                onSuccess.accept(res);
            } catch (Throwable t) {
                onError.accept(t);
            }
        });
    }

    /* =========================
       DOWNLOAD FILE
       ========================= */

    public static Path download(String url, Path target) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            return CLIENT.send(request, BodyHandlers.ofFile(target)).body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void download(String url, Path target, Consumer<Path> onSuccess, Consumer<Throwable> onError) {
        AsyncUtils.runAsync(() -> {
            try {
                Path path = download(url, target);
                onSuccess.accept(path);
            } catch (Throwable t) {
                onError.accept(t);
            }
        });
    }
}
