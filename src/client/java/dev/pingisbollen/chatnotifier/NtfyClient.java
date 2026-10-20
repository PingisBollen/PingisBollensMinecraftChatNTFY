package dev.pingisbollen.chatnotifier;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NtfyClient {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "chat-notifier-ntfy"); t.setDaemon(true); return t; });
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    private NtfyClient() {}
    public static void send(String keyword, String sender, String chat) { publish("Keyword: " + keyword + "\nSender: " + sender + "\n" + chat); }
    public static void sendTest() { publish("Test notification from Chat Notifier. If you see this, ntfy publishing works."); }
    private static void publish(String body) {
        Config c = ChatNotifierClient.CONFIG;
        if (c.topic == null || !c.topic.matches("[A-Za-z0-9_-]{1,64}")) { ChatNotifierClient.LOGGER.warn("ntfy notification not sent: configure a valid topic in Chat Notifier settings."); return; }
        try {
            URI base = URI.create(c.serverUrl.trim());
            if (!"https".equalsIgnoreCase(base.getScheme()) && !"http".equalsIgnoreCase(base.getScheme())) throw new IllegalArgumentException("Server URL must use HTTP or HTTPS.");
            String url = base.toString().replaceAll("/+$", "") + "/" + c.topic;
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(12)).header("Content-Type", "text/plain; charset=utf-8").header("X-Title", safeHeader(c.title.isBlank() ? "Minecraft Chat" : c.title)).header("X-Priority", Integer.toString(Math.max(1, Math.min(5, c.priority)))).POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            if (!c.tags.isBlank()) request.header("X-Tags", safeHeader(c.tags));
            if (!c.token.isBlank()) request.header("Authorization", "Bearer " + c.token.trim());
            EXECUTOR.submit(() -> {
                try {
                    HttpResponse<String> response = HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() >= 200 && response.statusCode() < 300) ChatNotifierClient.LOGGER.info("ntfy notification accepted by server.");
                    else ChatNotifierClient.LOGGER.warn("ntfy notification failed with HTTP {}: {}", response.statusCode(), statusHint(response.statusCode()));
                } catch (Exception e) { ChatNotifierClient.LOGGER.warn("ntfy notification failed: {}", e.getClass().getSimpleName()); }
            });
        } catch (Exception e) { ChatNotifierClient.LOGGER.warn("ntfy settings invalid; notification not sent: {}", e.getMessage()); }
    }
    private static String safeHeader(String value) { return value.replace("\r", " ").replace("\n", " "); }
    private static String statusHint(int status) { return switch (status) { case 401,403 -> "check the configured token and server permissions"; case 404 -> "check the server URL and topic"; case 429 -> "rate limited; try a longer cooldown"; default -> "check ntfy server settings"; }; }
    public static void shutdown() { EXECUTOR.shutdown(); }
}
