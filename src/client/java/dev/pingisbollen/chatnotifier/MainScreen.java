package dev.pingisbollen.chatnotifier;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MainScreen extends Screen {
    private final Screen parent;
    public MainScreen(Screen parent) { super(Component.literal("Chat Notifier")); this.parent = parent; }
    @Override protected void init() {
        int cx = width / 2;
        addRenderableWidget(Button.builder(Component.literal("Notifications: " + (ChatNotifierClient.CONFIG.enabled ? "ON" : "OFF")), b -> { ChatNotifierClient.CONFIG.enabled = !ChatNotifierClient.CONFIG.enabled; ChatNotifierClient.CONFIG.save(); clearWidgets(); init(); }).bounds(cx - 100, height / 4, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Manage keywords"), b -> minecraft.gui.setScreen(new KeywordsScreen(this))).bounds(cx - 100, height / 4 + 28, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Configure ntfy"), b -> minecraft.gui.setScreen(new NtfyScreen(this))).bounds(cx - 100, height / 4 + 56, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Send test notification"), b -> NtfyClient.sendTest()).bounds(cx - 100, height / 4 + 84, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> minecraft.gui.setScreen(parent)).bounds(cx - 100, height - 38, 200, 20).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, 24, 0xFFFFFF);
        graphics.centeredText(font, "Created by Pingisbollen", width / 2, height - 58, 0xB8B8B8);
        graphics.centeredText(font, "Public topics can be read or published to by anyone who knows the name.", width / 2, height - 74, 0xAAAAAA);
    }
}

