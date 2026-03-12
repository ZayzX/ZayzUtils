package fr.zayzx.zayzutils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.BiConsumer;

public final class HttpUtils {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static HttpServer server;

    private HttpUtils() {}

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

    public static void listen(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.start();
            System.out.println("HTTP server started on port " + port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getRoute(String path, BiConsumer<HttpExchange, String> handler) {
        server.createContext(path, exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET"))
                return;

            handler.accept(exchange, null);
        });
    }

    public static void postRoute(String path, BiConsumer<HttpExchange, String> handler) {
        server.createContext(path, exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST"))
                return;

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            handler.accept(exchange, body);
        });
    }

    public static void send(HttpExchange exchange, String response) {
        try {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
}