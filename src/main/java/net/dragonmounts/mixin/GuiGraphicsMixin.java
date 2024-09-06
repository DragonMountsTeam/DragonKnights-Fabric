package net.dragonmounts.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.dragonmounts.api.IDragonScaleArmorEffect;
import net.dragonmounts.capability.ArmorEffectManager;
import net.dragonmounts.item.DragonScaleArmorItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @ModifyExpressionValue(
            method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;getCooldownPercent(Lnet/minecraft/world/item/Item;F)F")
    )
    public float getCooldown(float original, @Local(argsOnly = true) ItemStack stack) {
        if (stack.getItem() instanceof DragonScaleArmorItem armor && armor.effect instanceof IDragonScaleArmorEffect.Advanced advanced) {
            int cooldown = ArmorEffectManager.getLocalCooldown(advanced);
            if (cooldown > 0) return Mth.clamp(cooldown / (float) advanced.cooldown, original, 1F);
        }
        return original;
    }

    private GuiGraphicsMixin() {}
}
