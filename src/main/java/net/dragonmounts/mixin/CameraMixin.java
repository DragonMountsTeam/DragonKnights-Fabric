package net.dragonmounts.mixin;

import net.dragonmounts.config.ClientConfig;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract float getMaxZoom(float distance);

    @Shadow
    protected abstract void move(float x, float y, float z);

    @Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"), cancellable = true)
    public void cameraOffset(BlockGetter a, Entity host, boolean c, boolean d, float e, CallbackInfo info) {
        if (host.getVehicle() instanceof TameableDragonEntity) {
            this.move(-this.getMaxZoom(ClientConfig.INSTANCE.camera_distance.get()), 0.0F, -ClientConfig.INSTANCE.camera_offset.get());
            info.cancel();
        }
    }
}
