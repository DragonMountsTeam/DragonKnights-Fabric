package net.dragonmounts.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dragonmounts.block.AbstractDragonHeadBlock;
import net.dragonmounts.block.entity.DragonHeadBlockEntity;
import net.dragonmounts.client.variant.VariantAppearance;
import net.dragonmounts.item.DragonHeadItem;
import net.dragonmounts.registry.DragonVariant;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static net.dragonmounts.client.variant.VariantAppearances.ENDER_FEMALE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public enum DragonHeadRenderer implements BlockEntityRenderer<DragonHeadBlockEntity>, BuiltinItemRendererRegistry.DynamicItemRenderer, BlockEntityRendererProvider<DragonHeadBlockEntity>, Consumer<DragonVariant> {
    INSTANCE;

    public static void renderHead(ModelPart head, VariantAppearance appearance, PoseStack matrices, MultiBufferSource buffers, boolean flip, double offsetX, double offsetY, double offsetZ, int light, int overlay) {
        matrices.pushPose();
        matrices.translate(offsetX, offsetY, offsetZ);
        if (flip) {
            matrices.scale(1.0F, -1.0F, -1.0F);
        }
        head.render(matrices, buffers.getBuffer(appearance.getBodyForBlock()), light, overlay);
        head.render(matrices, buffers.getBuffer(appearance.getGlowForBlock()), 15728640, OverlayTexture.NO_OVERLAY);
        matrices.popPose();
    }

    @Override
    public void render(DragonHeadBlockEntity entity, float ticks, PoseStack matrices, MultiBufferSource buffers, int light, int overlay) {
        var state = entity.getBlockState();
        if (state.getBlock() instanceof AbstractDragonHeadBlock head) {
            var appearance = head.variant.getAppearance(ENDER_FEMALE);
            var model = appearance.getModel();
            if (model == null) return;
            model.setupBlock(entity.getAnimation(ticks), head.getYRotation(state), 0.75F);
            if (head.isOnWall) {
                final Direction direction = state.getValue(HORIZONTAL_FACING);
                renderHead(model.head, appearance, matrices, buffers, true, 0.5D - direction.getStepX() * 0.25D, 0.25D, 0.5D - direction.getStepZ() * 0.25D, light, overlay);
            } else {
                renderHead(model.head, appearance, matrices, buffers, true, 0.5D, 0D, 0.5D, light, overlay);
            }
        }
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext context, PoseStack matrices, MultiBufferSource buffers, int light, int overlay) {
        if (stack.getItem() instanceof DragonHeadItem head) {
            var appearance = head.variant.getAppearance(ENDER_FEMALE);
            var model = appearance.getModel();
            if (model == null) return;
            if (context == ItemDisplayContext.HEAD) {
                model.setupBlock(0.0F, 180.0F, 1.425F);
                renderHead(model.head, appearance, matrices, buffers, true, 0.5D, 0.4375D, 0.5D, light, overlay);
            } else {
                model.setupBlock(0.0F, 0.0F, 0.75F);
                renderHead(model.head, appearance, matrices, buffers, true, 0.5D, 0D, 0.5D, light, overlay);
            }
        }
    }

    @Override
    public void accept(DragonVariant variant) {
        BuiltinItemRendererRegistry.INSTANCE.register(variant.headItem, this);
    }

    @Override
    public @NotNull BlockEntityRenderer<DragonHeadBlockEntity> create(Context context) {
        return this;
    }
}
