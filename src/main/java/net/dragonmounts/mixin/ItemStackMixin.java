package net.dragonmounts.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.dragonmounts.init.DMDataComponents;
import net.dragonmounts.item.IEntityContainer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    protected abstract <T extends TooltipProvider> void addToTooltip(DataComponentType<T> type, Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag);

    @Shadow
    public abstract Item getItem();

    @Inject(method = "getTooltipLines", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/core/component/DataComponents;HIDE_ADDITIONAL_TOOLTIP:Lnet/minecraft/core/component/DataComponentType;"
    ))
    public void appendDragonTypifiedText(
            Item.TooltipContext context,
            @Nullable Player a,
            TooltipFlag flag,
            CallbackInfoReturnable<List<Component>> info,
            @Local Consumer<Component> consumer
    ) {
        if (this.getItem() instanceof IEntityContainer<?>) return;
        this.addToTooltip(DMDataComponents.DRAGON_TYPE, context, consumer, flag);
    }

    private ItemStackMixin() {}
}
