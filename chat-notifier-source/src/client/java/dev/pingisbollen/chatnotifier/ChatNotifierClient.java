package dev.pingisbollen.chatnotifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public final class ChatNotifierClient implements ClientModInitializer {
    public static final String MOD_ID = "chat_notifier";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Config CONFIG;
    private static final ConcurrentHashMap<String, Long> LAST_SENT = new ConcurrentHashMap<>();
    private static volatile String lastMessageKey = "";
    private static volatile long lastMessageAt = 0;

    @Override public void onInitializeClient() {
        CONFIG = Config.load();
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> inspect(message.getString()));
        if (CONFIG.includeSystemMessages) ClientReceiveMessageEvents.GAME.register((message, overlay) -> inspect(message.getString()));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommands.literal("chatnotify")
                .executes(ctx -> { openGui(); return 1; })
                .then(ClientCommands.literal("test").executes(ctx -> { NtfyClient.sendTest(); return 1; }))
                .then(ClientCommands.literal("reload").executes(ctx -> { CONFIG = Config.load(); ctx.getSource().sendFeedback(Component.literal("Chat Notifier settings reloaded.")); return 1; }))
                .then(ClientCommands.literal("toggle").executes(ctx -> { CONFIG.enabled = !CONFIG.enabled; CONFIG.save(); ctx.getSource().sendFeedback(Component.literal("Chat notifications " + (CONFIG.enabled ? "enabled" : "disabled") + ".")); return 1; }))));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> NtfyClient.shutdown());
    }

    private static void inspect(String message) {
        if (CONFIG == null || !CONFIG.enabled || message == null) return;
        String key = message.strip(); long now = System.currentTimeMillis();
        if (key.equals(lastMessageKey) && now - lastMessageAt < 500) return;
        lastMessageKey = key; lastMessageAt = now;
        for (Config.Keyword keyword : CONFIG.keywords) {
            if (keyword != null && keyword.enabled && Matcher.matches(message, keyword.text, CONFIG.caseSensitive, CONFIG.wholeWord)) {
                long previous = LAST_SENT.getOrDefault(keyword.text.toLowerCase(java.util.Locale.ROOT), 0L);
                if (now - previous >= Math.max(0, CONFIG.cooldownSeconds) * 1000L) {
                    LAST_SENT.put(keyword.text.toLowerCase(java.util.Locale.ROOT), now);
                    NtfyClient.send(keyword.text, "Unknown sender", message);
                }
                break;
            }
        }
    }
    public static void openGui() { Minecraft.getInstance().gui.setScreen(new MainScreen(Minecraft.getInstance().gui.screen())); }
}


