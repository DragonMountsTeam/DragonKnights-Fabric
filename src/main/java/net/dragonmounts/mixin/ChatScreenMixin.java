package net.dragonmounts.mixin;

import net.dragonmounts.client.gui.DMConfigScreen;
import net.dragonmounts.command.ConfigCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin({ChatScreen.class, SleepingChatScreen.class})
public abstract class ChatScreenMixin extends Screen {
    private ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "keyPressed", at = @At(
            value = "INVOKE",
            target = "Ljava/lang/String;isEmpty()Z"
    ), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void handleClientCommand(int key, int scan, int modifiers, CallbackInfoReturnable<Boolean> info, String string) {
        if (ConfigCommand.OPEN_CONFIG_SCREEN.equals(string)) {
            MinecraftClient minecraft = this.client;
            //noinspection DataFlowIssue
            minecraft.inGameHud.getChatHud().addToMessageHistory(string);
            minecraft.openScreen(new DMConfigScreen(minecraft, minecraft.currentScreen));
            info.setReturnValue(true);
        }
    }
}
