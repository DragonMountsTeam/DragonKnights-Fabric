package net.dragonmounts.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.dragonmounts.client.gui.FluteOverlay;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;showProfilerChart()Z"
    ), cancellable = true)
    public void onKeyPress(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo info) {
        if (FluteOverlay.isAvailable() && key == InputConstants.KEY_TAB && action != 0) {
            FluteOverlay.nextSelection();
            info.cancel();
        }
    }

    private KeyboardHandlerMixin() {}
}
