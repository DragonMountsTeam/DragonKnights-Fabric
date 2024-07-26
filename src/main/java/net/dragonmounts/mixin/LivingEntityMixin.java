package net.dragonmounts.mixin;

import net.dragonmounts.capability.IArmorEffectManager.Provider;
import net.dragonmounts.init.DMArmorEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    protected PlayerEntity attackingPlayer;

    private LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    protected abstract int getXpToDrop(PlayerEntity player);

    @Inject(method = "dropXp", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getXpToDrop(Lnet/minecraft/entity/player/PlayerEntity;)I"), cancellable = true)
    public void xpBonus(CallbackInfo info) {
        if (this.attackingPlayer != null && ((Provider) this.attackingPlayer).dragonmounts$getManager().isActive(DMArmorEffects.ENCHANT)) {
            for (int i = (int) (this.getXpToDrop(this.attackingPlayer) * 1.5F + 0.5F), j; i > 0; i -= j) {
                this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.getX(), this.getY(), this.getZ(), j = ExperienceOrbEntity.roundToOrbSize(i)));
            }
            info.cancel();
        }
    }
}
