package net.dragonmounts.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import net.dragonmounts.block.HatchableDragonEggBlock;
import net.dragonmounts.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.init.DMBlocks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.RenderShape;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import static net.dragonmounts.entity.dragon.HatchableDragonEggEntity.EGG_CRACK_THRESHOLD;
import static net.dragonmounts.entity.dragon.HatchableDragonEggEntity.MIN_HATCHING_TIME;
import static net.dragonmounts.util.math.MathUtil.HALF_RAD_FACTOR;
import static net.minecraft.client.renderer.ItemBlockRenderTypes.getMovingBlockRenderType;

/**
 * @see net.minecraft.client.renderer.entity.FallingBlockRenderer
 */
public class DragonEggRenderer extends EntityRenderer<HatchableDragonEggEntity> {
    protected final BlockRenderDispatcher dispatcher;

    public DragonEggRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    public void render(HatchableDragonEggEntity entity, float entityYaw, float partialTicks, PoseStack matrices, MultiBufferSource buffers, int light) {
        var block = entity.getDragonType().getInstance(HatchableDragonEggBlock.class, DMBlocks.ENDER_DRAGON_EGG);
        var state = block.defaultBlockState();
        if (state.getRenderShape() != RenderShape.INVISIBLE) {
            var level = entity.level();
            var pos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
            var renderer = this.dispatcher.getModelRenderer();
            var model = this.dispatcher.getBlockModel(state);
            var random = level.random;
            long seed = state.getSeed(pos);
            matrices.pushPose();
            float angle = entity.getAmplitude(partialTicks);
            if (angle != 0) {
                float axis = entity.getRotationAxis();
                float half = angle * HALF_RAD_FACTOR;
                float sin = Mth.sin(half);
                matrices.mulPose(new Quaternionf(Mth.cos(axis) * sin, 0.0F, Mth.sin(axis) * sin, Mth.cos(half)));
            }
            matrices.translate(-0.5D, 0.0D, -0.5D);
            renderer.tesselateBlock(level, model, state, pos, matrices, buffers.getBuffer(getMovingBlockRenderType(state)), false, random, seed, OverlayTexture.NO_OVERLAY);
            int stage = entity.getAge();
            if (stage >= EGG_CRACK_THRESHOLD) {
                renderer.tesselateBlock(level, model, state, pos, matrices, new SheetedDecalTextureGenerator(buffers.getBuffer(
                        ModelBakery.DESTROY_TYPES.get(Math.min((stage - EGG_CRACK_THRESHOLD) * 90 / MIN_HATCHING_TIME, 9))
                ), matrices.last(), 1.0F), false, random, seed, OverlayTexture.NO_OVERLAY);
            }
            matrices.popPose();
            super.render(entity, entityYaw, partialTicks, matrices, buffers, light);
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(HatchableDragonEggEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
