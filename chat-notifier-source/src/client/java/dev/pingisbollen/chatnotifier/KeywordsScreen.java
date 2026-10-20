package dev.pingisbollen.chatnotifier;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class KeywordsScreen extends Screen {
    private final Screen parent;
    private EditBox input;
    private String status = "";
    private int selected = -1;
    private int listOffset = 0;

    public KeywordsScreen(Screen parent) {
        super(Component.literal("Manage keywords"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        String previousInput = input == null ? "" : input.getValue();
        int cx = width / 2;

        input = new EditBox(font, cx - 100, height - 104, 200, 20, Component.literal("Keyword"));
        input.setMaxLength(80);
        input.setValue(previousInput);
        addRenderableWidget(input);

        addRenderableWidget(Button.builder(Component.literal("Add keyword / save edit"), b -> saveKeyword())
                .bounds(cx - 100, height - 78, 200, 20).build());

        boolean selectedEnabled = selected >= 0 && selected < ChatNotifierClient.CONFIG.keywords.size()
                && ChatNotifierClient.CONFIG.keywords.get(selected).enabled;
        addRenderableWidget(Button.builder(Component.literal(selectedEnabled ? "Disable selected" : "Enable selected"), b -> toggle())
                .bounds(cx - 100, height - 52, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Delete"), b -> delete())
                .bounds(cx + 2, height - 52, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            ChatNotifierClient.CONFIG.save();
            minecraft.gui.setScreen(parent);
        }).bounds(cx - 100, height - 26, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Case sensitive: " + ChatNotifierClient.CONFIG.caseSensitive), b -> {
            ChatNotifierClient.CONFIG.caseSensitive = !ChatNotifierClient.CONFIG.caseSensitive;
            b.setMessage(Component.literal("Case sensitive: " + ChatNotifierClient.CONFIG.caseSensitive));
        }).bounds(cx - 210, 40, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Whole word: " + ChatNotifierClient.CONFIG.wholeWord), b -> {
            ChatNotifierClient.CONFIG.wholeWord = !ChatNotifierClient.CONFIG.wholeWord;
            b.setMessage(Component.literal("Whole word: " + ChatNotifierClient.CONFIG.wholeWord));
        }).bounds(cx + 10, 40, 200, 20).build());

        int keywordCount = ChatNotifierClient.CONFIG.keywords.size();
        int maxOffset = Math.max(0, keywordCount - visibleRows());
        listOffset = Math.max(0, Math.min(listOffset, maxOffset));
        int end = Math.min(keywordCount, listOffset + visibleRows());
        for (int i = listOffset; i < end; i++) {
            final int index = i;
            Config.Keyword keyword = ChatNotifierClient.CONFIG.keywords.get(i);
            String label = (i == selected ? "> " : "") + (keyword.enabled ? "[ON] " : "[OFF] ") + keyword.text;
            addRenderableWidget(Button.builder(Component.literal(label), b -> {
                selected = index;
                input.setValue(ChatNotifierClient.CONFIG.keywords.get(index).text);
                rebuildWidgets();
                input.setFocused(true);
            }).bounds(cx - 160, 70 + (i - listOffset) * 22, 320, 20).build());
        }
    }

    private void saveKeyword() {
        String value = input.getValue().trim();
        if (value.isEmpty()) {
            status = "Type a word or phrase in the box first.";
            return;
        }

        if (selected >= 0 && selected < ChatNotifierClient.CONFIG.keywords.size()) {
            ChatNotifierClient.CONFIG.keywords.get(selected).text = value;
            status = "Keyword updated. Type another word to add it.";
        } else {
            ChatNotifierClient.CONFIG.keywords.add(new Config.Keyword(value, true));
            status = "Keyword added. Type another word to add more.";
            listOffset = Math.max(0, ChatNotifierClient.CONFIG.keywords.size() - visibleRows());
        }

        // Clear selection so the next save adds a new keyword instead of replacing this one.
        selected = -1;
        input.setValue("");
        ChatNotifierClient.CONFIG.save();
        rebuildWidgets();
        input.setFocused(true);
    }

    private void toggle() {
        if (selected >= 0 && selected < ChatNotifierClient.CONFIG.keywords.size()) {
            Config.Keyword keyword = ChatNotifierClient.CONFIG.keywords.get(selected);
            keyword.enabled = !keyword.enabled;
            ChatNotifierClient.CONFIG.save();
            rebuildWidgets();
        } else {
            status = "Select a keyword first.";
        }
    }

    private void delete() {
        if (selected >= 0 && selected < ChatNotifierClient.CONFIG.keywords.size()) {
            ChatNotifierClient.CONFIG.keywords.remove(selected);
            selected = -1;
            input.setValue("");
            ChatNotifierClient.CONFIG.save();
            rebuildWidgets();
        } else {
            status = "Select a keyword first.";
        }
    }

    private int visibleRows() {
        return Math.max(1, (height - 250) / 22);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int x, int y, float d) {
        super.extractRenderState(g, x, y, d);
        g.centeredText(font, title, width / 2, 14, 0xFFFFFF);
        if (ChatNotifierClient.CONFIG.keywords.isEmpty()) {
            g.centeredText(font, "No keywords yet. Add one below.", width / 2, 90, 0xAAAAAA);
        }
        int count = ChatNotifierClient.CONFIG.keywords.size();
        if (count > visibleRows()) {
            g.centeredText(font, "Scroll over the list to see more words (" + (listOffset + 1) + "-"
                    + Math.min(count, listOffset + visibleRows()) + " of " + count + ")", width / 2, 58, 0xBBBBBB);
        }
        g.centeredText(font, status, width / 2, height - 119, 0xFFCC55);
        g.centeredText(font, "Select a row to edit it, or type below to add a keyword.", width / 2, height - 132, 0xBBBBBB);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseY >= 65 && mouseY < height - 180 && ChatNotifierClient.CONFIG.keywords.size() > visibleRows()) {
            int maxOffset = ChatNotifierClient.CONFIG.keywords.size() - visibleRows();
            listOffset = Math.max(0, Math.min(maxOffset, listOffset + (verticalAmount < 0 ? 1 : -1)));
            rebuildWidgets();
            input.setFocused(true);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
