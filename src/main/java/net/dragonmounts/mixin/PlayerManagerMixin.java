package net.dragonmounts.mixin;

import net.dragonmounts.capability.IArmorEffectManager.Provider;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerManagerMixin {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    public void sendInitPacket(Connection a, ServerPlayer player, CommonListenerCookie c, CallbackInfo info) {
        ((Provider) player).dragonmounts$getManager().sendInitPacket();
    }

    private PlayerManagerMixin() {}
}
