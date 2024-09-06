package net.dragonmounts.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.dragonmounts.capability.IArmorEffectManager;
import net.dragonmounts.init.DMArmorEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @Inject(method = "processMobExperience", at = @At(
            value = "INVOKE",
            target = "Lorg/apache/commons/lang3/mutable/MutableFloat;intValue()I",
            remap = false
    ))
    private static void xpBonus(ServerLevel a, Entity b, Entity c, int d, CallbackInfoReturnable<Integer> info, @Local LivingEntity killer, @Local MutableFloat exp) {
        if (killer instanceof IArmorEffectManager.Provider provider && provider.dragonmounts$getManager().isActive(DMArmorEffects.ENCHANT)) {
            exp.add(Math.ceil(exp.floatValue() * 0.5F));
        }
    }

    private EnchantmentHelperMixin() {}
}
