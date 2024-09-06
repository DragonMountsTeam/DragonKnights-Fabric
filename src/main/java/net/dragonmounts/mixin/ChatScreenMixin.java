package net.dragonmounts.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.dragonmounts.client.gui.DMConfigScreen;
import net.dragonmounts.command.ConfigCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Unique
    private boolean dragonmounts$shouldClose = true;

    @Inject(method = "init", at = @At("HEAD"))
    public void reset(CallbackInfo info) {
        this.dragonmounts$shouldClose = true;
    }

    @WrapWithCondition(
            method = "keyPressed",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen;handleChatInput(Ljava/lang/String;Z)V")
            ),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V")
    )
    public boolean preventClosingScreen(Minecraft a, Screen b) {
        return this.dragonmounts$shouldClose;
    }

    @Inject(method = "handleChatInput", at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z"), cancellable = true)
    public void handleClientCommand(String string, boolean b, CallbackInfo info) {
        if (ConfigCommand.OPEN_CONFIG_SCREEN.equals(string)) {
            this.dragonmounts$shouldClose = false;
            var minecraft = this.minecraft;
            //noinspection DataFlowIssue
            minecraft.setScreen(new DMConfigScreen(minecraft.screen));
            info.cancel();
        }
    }

    private ChatScreenMixin(Component a) {super(a);}
}
