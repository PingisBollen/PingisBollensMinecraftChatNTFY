package dev.pingisbollen.chatnotifier.mixin;

import dev.pingisbollen.chatnotifier.MainScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void chatNotifier$addButton(CallbackInfo ci) {
        PauseScreen self = (PauseScreen) (Object) this;
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        Button button = Button.builder(Component.literal("Chat Notifier"), clicked ->
                Minecraft.getInstance().gui.setScreen(new MainScreen(self)))
                .bounds(width / 2 - 100, height - 32, 200, 20)
                .build();
        ((ScreenAccessor) (Object) this).chatNotifier$addRenderableWidget(button);
    }
}

