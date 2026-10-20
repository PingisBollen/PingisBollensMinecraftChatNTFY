package dev.pingisbollen.chatnotifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("chat-notifier.json");
    public boolean enabled = true;
    public boolean caseSensitive = false;
    public boolean wholeWord = false;
    public boolean includeSystemMessages = false;
    public boolean includeOutgoing = false;
    public String serverUrl = "https://ntfy.sh";
    public String topic = "";
    public String token = "";
    public int priority = 3;
    public String tags = "";
    public int cooldownSeconds = 10;
    public String title = "Minecraft Chat";
    public List<Keyword> keywords = new ArrayList<>(List.of(new Keyword("pingisbollen", true)));

    public static Config load() {
        try {
            if (Files.exists(FILE)) {
                Config loaded = GSON.fromJson(Files.readString(FILE), Config.class);
                if (loaded != null) {
                    if (loaded.keywords == null) loaded.keywords = new ArrayList<>();
                    if (loaded.serverUrl == null) loaded.serverUrl = "https://ntfy.sh";
                    if (loaded.topic == null) loaded.topic = "";
                    if (loaded.token == null) loaded.token = "";
                    return loaded;
                }
            }
        } catch (Exception ignored) { }
        Config fresh = new Config();
        fresh.save();
        return fresh;
    }

    public void save() {
        try { Files.createDirectories(FILE.getParent()); Files.writeString(FILE, GSON.toJson(this)); }
        catch (IOException e) { ChatNotifierClient.LOGGER.error("Could not save Chat Notifier settings.", e); }
    }

    public static final class Keyword {
        public String text;
        public boolean enabled;
        public Keyword(String text, boolean enabled) { this.text = text; this.enabled = enabled; }
    }
}
