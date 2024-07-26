package net.dragonmounts.mixin;

import net.dragonmounts.item.IEntityContainer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow
    private int itemAge;

    @Inject(method = "setStack", at = @At("TAIL"))
    public void persistent(ItemStack stack, CallbackInfo info) {
        Item item = stack.getItem();
        if (item instanceof IEntityContainer && !((IEntityContainer<?>) item).isEmpty(stack.getTag())) {
            this.itemAge = Short.MIN_VALUE;
        }
    }

    private ItemEntityMixin() {}
}