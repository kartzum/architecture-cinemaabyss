package s.l.f.t.proxy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleProxy {

    public static void run() {
        int port = getIntegerEnv("PROXY_PORT", 8000);
        HttpServer server;
        try {
            server = HttpServer.create(
                    new InetSocketAddress("0.0.0.0", port), 0
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/", new ProxyHandler());
        server.start();
    }

    static class ProxyHandler implements HttpHandler {
        private final HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        private final Map<String, String> targets;

        ProxyHandler() {
            String movies = getStringEnv("MOVIES_SERVICE_URL", "http://localhost:8081");
            String mono = getStringEnv("MONOLITH_URL", "http://localhost:8080");
            targets = Map.of(
                    "/api/movies", movies,
                    "/api/users", mono,
                    "/health", mono
            );
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();

            try {
                String targetUrl = null;
                for (Map.Entry<String, String> e : targets.entrySet()) {
                    if (path.startsWith(e.getKey())) {
                        targetUrl = e.getValue() + path + (query != null ? "?" + query : "");
                    }
                }
                if (targetUrl == null) {
                    sendError(exchange, 404, "Not Found", null);
                    return;
                }
                handleProxyRequest(exchange, targetUrl);
            } catch (Exception e) {
                sendError(exchange, 500, "Internal Server Error", null);
            }
        }

        private void handleProxyRequest(HttpExchange exchange, String targetUrl) throws IOException {
            System.out.println("Handle request: " + targetUrl);

            try {
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(targetUrl))
                        .method(
                                exchange.getRequestMethod(),
                                HttpRequest.BodyPublishers.ofInputStream(exchange::getRequestBody)
                        );

                exchange.getRequestHeaders().forEach((key, values) -> {
                    if (!key.equalsIgnoreCase("Host") &&
                            !key.equalsIgnoreCase("Connection")) {
                        values.forEach(value -> requestBuilder.header(key, value));
                    }
                });

                HttpResponse<InputStream> response = httpClient.send(
                        requestBuilder.build(),
                        HttpResponse.BodyHandlers.ofInputStream()
                );

                Map<String, List<String>> responseHeaders = new HashMap<>(response.headers().map());
                responseHeaders.remove("content-length");
                exchange.getResponseHeaders().putAll(responseHeaders);
                exchange.sendResponseHeaders(response.statusCode(), 0);

                try (OutputStream os = exchange.getResponseBody(); InputStream is = response.body()) {
                    is.transferTo(os);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                sendError(exchange, 500, "Request interrupted", e);
            } catch (IOException e) {
                sendError(exchange, 502, "Bad Gateway: " + e.getMessage(), e);
            }
        }

        private void sendError(
                HttpExchange exchange,
                int code,
                String message,
                Exception e
        ) throws IOException {
            System.out.println("Error: " + message);
            if (e != null) {
                e.printStackTrace(System.err);
            }
            exchange.sendResponseHeaders(code, message.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(message.getBytes());
            }
        }
    }

    private static String getStringEnv(String key, String defaultValue) {
        return System.getenv().getOrDefault(key, defaultValue);
    }

    private static int getIntegerEnv(String key, int defaultValue) {
        try {
            return Integer.parseInt(getStringEnv(key, null));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
