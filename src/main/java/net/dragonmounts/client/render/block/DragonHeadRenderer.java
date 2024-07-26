package net.dragonmounts.client.render.block;

import net.dragonmounts.block.AbstractDragonHeadBlock;
import net.dragonmounts.block.entity.DragonHeadBlockEntity;
import net.dragonmounts.client.model.dragon.DragonHeadModel;
import net.dragonmounts.client.variant.VariantAppearance;
import net.dragonmounts.item.DragonHeadItem;
import net.dragonmounts.registry.DragonVariant;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.util.math.Direction;

import static net.dragonmounts.client.variant.VariantAppearances.ENDER_FEMALE;
import static net.minecraft.state.property.Properties.HORIZONTAL_FACING;

public class DragonHeadRenderer extends BlockEntityRenderer<DragonHeadBlockEntity> {
    private static final DragonHeadModel DRAGON_HEAD_MODEL = new DragonHeadModel();
    public static final BuiltinItemRendererRegistry.DynamicItemRenderer ITEM_RENDERER = (stack, mode, matrices, provider, light, overlay) -> {
        final Item item = stack.getItem();
        if (item instanceof DragonHeadItem) {
            if (mode == Mode.HEAD) {
                renderHead(((DragonHeadItem) item).variant, 0.5D, 0.4375D, 0.5D, 180F, 180F, 1.425F, true, matrices, provider, light, overlay);
            } else {
                renderHead(((DragonHeadItem) item).variant, 0.5D, 0D, 0.5D, 180F, 0F, 0.75F, true, matrices, provider, light, overlay);
            }
        }
    };

    public DragonHeadRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(DragonHeadBlockEntity entity, float ticks, MatrixStack matrices, VertexConsumerProvider provider, int light, int overlay) {
        final BlockState state = entity.getCachedState();
        final Block block = state.getBlock();
        if (block instanceof AbstractDragonHeadBlock) {
            final AbstractDragonHeadBlock head = (AbstractDragonHeadBlock) block;
            if (head.isOnWall) {
                final Direction direction = state.get(HORIZONTAL_FACING);
                renderHead(
                        head.variant,
                        0.5D - direction.getOffsetX() * 0.25D,
                        0.25D,
                        0.5D - direction.getOffsetZ() * 0.25D,
                        entity.getAnimation(ticks),
                        head.getYRotation(state),
                        0.75F,
                        true,
                        matrices,
                        provider,
                        light,
                        overlay
                );
            } else {
                renderHead(
                        head.variant,
                        0.5D,
                        0D,
                        0.5D,
                        entity.getAnimation(ticks),
                        head.getYRotation(state),
                        0.75F,
                        true,
                        matrices,
                        provider,
                        light,
                        overlay
                );
            }
        }
    }

    public static void renderHead(DragonVariant variant, double offsetX, double offsetY, double offsetZ, float ticks, float yaw, float scale, boolean flip, MatrixStack matrices, VertexConsumerProvider provider, int light, int overlay) {
        VariantAppearance appearance = variant.getAppearance(ENDER_FEMALE);
        DragonHeadModel model = DRAGON_HEAD_MODEL;
        matrices.push();
        matrices.translate(offsetX, offsetY, offsetZ);
        if (flip) {
            matrices.scale(1.0F, -1.0F, -1.0F);
        }
        model.setupAnim(ticks, yaw, 0F, scale);
        model.render(matrices, provider.getBuffer(appearance.getBodyForBlock()), light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        model.render(matrices, provider.getBuffer(appearance.getGlowForBlock()), 15728640, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrices.pop();
    }
}
