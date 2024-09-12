package net.dragonmounts.mixin;

import net.dragonmounts.config.ClientConfig;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Final
    @Shadow
    private MinecraftClient client;

    @Inject(method = "hasRidingInventory", at = @At("RETURN"), cancellable = true)
    public void isTameableDragon(CallbackInfoReturnable<Boolean> info) {
        if (ClientConfig.INSTANCE.redirect_inventory.get()) {
            //noinspection DataFlowIssue
            info.setReturnValue(info.getReturnValueZ() || this.client.player.getVehicle() instanceof TameableDragonEntity);
        }
    }

    private ClientPlayerInteractionManagerMixin() {}
}
