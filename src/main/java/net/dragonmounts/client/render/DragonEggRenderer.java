package net.dragonmounts.client.render;

import net.dragonmounts.block.HatchableDragonEggBlock;
import net.dragonmounts.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.init.DMBlocks;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.world.World;

import java.util.Random;

import static net.dragonmounts.entity.dragon.HatchableDragonEggEntity.EGG_CRACK_THRESHOLD;
import static net.dragonmounts.entity.dragon.HatchableDragonEggEntity.MIN_HATCHING_TIME;
import static net.dragonmounts.util.math.MathUtil.HALF_RAD_FACTOR;
import static net.minecraft.client.render.RenderLayers.getMovingBlockLayer;

/**
 * @see net.minecraft.client.render.entity.FallingBlockEntityRenderer
 */
public class DragonEggRenderer extends EntityRenderer<HatchableDragonEggEntity> {
    public DragonEggRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(HatchableDragonEggEntity entity, float entityYaw, float partialTicks, MatrixStack matrices, VertexConsumerProvider buffers, int packedLight) {
        HatchableDragonEggBlock block = entity.getDragonType().getInstance(HatchableDragonEggBlock.class, DMBlocks.ENDER_DRAGON_EGG);
        BlockState state = block.getDefaultState();
        if (state.getRenderType() == BlockRenderType.INVISIBLE) return;
        matrices.push();
        World level = entity.world;
        BlockPos pos = new BlockPos(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        BlockRenderManager manager = MinecraftClient.getInstance().getBlockRenderManager();
        BakedModel model = manager.getModel(state);
        BlockModelRenderer renderer = manager.getModelRenderer();
        Random random = level.random;
        long seed = state.getRenderingSeed(pos);
        int stage = entity.getAge();
        float angle = entity.getAmplitude(partialTicks);
        if (angle != 0) {
            float axis = entity.getRotationAxis();
            float half = angle * HALF_RAD_FACTOR;
            float sin = MathHelper.sin(half);
            matrices.multiply(new Quaternion(MathHelper.cos(axis) * sin, 0.0F, MathHelper.sin(axis) * sin, MathHelper.cos(half)));
        }
        matrices.translate(-0.5D, 0.0D, -0.5D);
        renderer.render(level, model, state, pos, matrices, buffers.getBuffer(getMovingBlockLayer(state)), false, random, seed, OverlayTexture.DEFAULT_UV);
        if (stage >= EGG_CRACK_THRESHOLD) {
            MatrixStack.Entry last = matrices.peek();
            renderer.render(level, model, state, pos, matrices, new OverlayVertexConsumer(
                    buffers.getBuffer(ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(Math.min((stage - EGG_CRACK_THRESHOLD) * 90 / MIN_HATCHING_TIME, 9))), last.getModel(), last.getNormal()
            ), true, random, seed, OverlayTexture.DEFAULT_UV);
        }
        matrices.pop();
        super.render(entity, entityYaw, partialTicks, matrices, buffers, packedLight);

    }

    @Override
    public Identifier getTexture(HatchableDragonEggEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }
}
