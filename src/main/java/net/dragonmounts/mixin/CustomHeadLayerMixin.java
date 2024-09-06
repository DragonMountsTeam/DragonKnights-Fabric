package net.dragonmounts.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dragonmounts.client.renderer.block.DragonHeadRenderer;
import net.dragonmounts.client.variant.VariantAppearances;
import net.dragonmounts.item.DragonHeadItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin {
    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V"),
            cancellable = true
    )
    public void renderDragonHead(
            PoseStack matrices,
            MultiBufferSource buffers,
            int light,
            LivingEntity d,
            float limbSwing,
            float e,
            float f,
            float g,
            float h,
            float i,
            CallbackInfo info,
            @Local Item item,
            @Local boolean flag
    ) {
        if (item instanceof DragonHeadItem head) {
            var appearance = head.variant.getAppearance(VariantAppearances.ENDER_FEMALE);
            var model = appearance.getModel();
            model.setupBlock(limbSwing, 0.0F, 0.890625F);//0.5F * 1.1875F  0.0078125
            DragonHeadRenderer.renderHead(model.head, appearance, matrices, buffers, false, 0.0D, flag ? -0.0078125D : -0.0703125D, 0.0D, light, OverlayTexture.NO_OVERLAY);
            info.cancel();
            matrices.popPose();
        }
    }
}
