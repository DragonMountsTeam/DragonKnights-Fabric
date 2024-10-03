package net.dragonmounts.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.dragonmounts.client.gui.FluteOverlay;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onScroll", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;swapPaint(D)V"
    ), cancellable = true)
    public void onMouseScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo info, @Local(ordinal = 2) int direction) {
        if (FluteOverlay.isAvailable()) {
            switch ((int) Math.signum(direction)) {
                case 1:
                    FluteOverlay.lastSelection();
                    info.cancel();
                    break;
                case -1:
                    FluteOverlay.nextSelection();
                    info.cancel();
                    break;
            }
        }
    }
}
