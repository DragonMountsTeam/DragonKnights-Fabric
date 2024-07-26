package net.dragonmounts.mixin;

import net.dragonmounts.client.render.block.DragonHeadRenderer;
import net.dragonmounts.item.DragonHeadItem;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HeadFeatureRenderer.class)
public abstract class HeadFeatureRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/ModelPart;rotate(Lnet/minecraft/client/util/math/MatrixStack;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    public void renderDragonHead(
            MatrixStack matrices,
            VertexConsumerProvider buffer,
            int light,
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo info,
            ItemStack stack,
            Item item,
            boolean flag
    ) {
        if (item instanceof DragonHeadItem) {
            DragonHeadRenderer.renderHead(
                    ((DragonHeadItem) item).variant,
                    0D,
                    flag ? -0.0078125D : -0.0703125D,
                    0D,
                    limbSwing,
                    0F,
                    0.890625F,//0.5F * 1.1875F  0.0078125
                    false,
                    matrices,
                    buffer,
                    light,
                    OverlayTexture.DEFAULT_UV
            );
            info.cancel();
            matrices.pop();
        }
    }
}
