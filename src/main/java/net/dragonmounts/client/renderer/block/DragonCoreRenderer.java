package net.dragonmounts.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dragonmounts.block.DragonCoreBlock;
import net.dragonmounts.block.entity.DragonCoreBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import static net.dragonmounts.DragonMounts.makeId;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

/**
 * @see net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer
 */
public class DragonCoreRenderer implements BlockEntityRenderer<DragonCoreBlockEntity> {
    private static final ResourceLocation TEXTURE_LOCATION = makeId("textures/block/dragon_core.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE_LOCATION);
    public static final ItemRenderer ITEM_RENDERER = new ItemRenderer();
    private final ShulkerModel<?> model;

    public DragonCoreRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ShulkerModel<>(context.bakeLayer(ModelLayers.SHULKER));
    }

    @Override
    public void render(DragonCoreBlockEntity core, float ticks, PoseStack matrices, MultiBufferSource buffers, int light, int overlay) {
        Direction direction = Direction.SOUTH;
        if (core.hasLevel()) {
            //noinspection DataFlowIssue
            BlockState state = core.getLevel().getBlockState(core.getBlockPos());
            if (state.getBlock() instanceof DragonCoreBlock) {
                direction = state.getValue(HORIZONTAL_FACING);
            }
        }
        renderCore(this.model, direction, core.getProgress(ticks), matrices, buffers, light, overlay);
    }

    public static void renderCore(ShulkerModel<?> model, Direction direction, float progress, PoseStack matrices, MultiBufferSource buffers, int light, int overlay) {
        matrices.pushPose();
        matrices.translate(0.5F, 0.5F, 0.5F);
        matrices.scale(0.9995F, 0.9995F, 0.9995F);
        matrices.mulPose(Axis.YP.rotation((direction.get2DDataValue() & 3) * 1.5707964F));// 1.5707964F = MathUtil.PI / 180.0F * 90.0F
        matrices.scale(1.0F, -1.0F, -1.0F);
        matrices.translate(0.0F, -1.0F, 0.0F);
        ModelPart lid = model.getLid();
        lid.setPos(0.0F, 24.0F - progress * 8.0F, 0.0F);
        lid.yRot = 270.0F * progress * 0.017453292F;// 0.017453292F = MathUtil.PI / 180.0F
        model.renderToBuffer(matrices, buffers.getBuffer(RENDER_TYPE), light, overlay);
        matrices.popPose();
    }

    public static class ItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer, InvalidateRenderStateCallback {
        private ShulkerModel<?> model;

        public ItemRenderer() {
            InvalidateRenderStateCallback.EVENT.register(this);
        }

        @Override
        public void onInvalidate() {
            this.model = new ShulkerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.SHULKER));
        }

        @Override
        public void render(ItemStack stack, ItemDisplayContext context, PoseStack matrices, MultiBufferSource buffers, int light, int overlay) {
            DragonCoreRenderer.renderCore(this.model, Direction.SOUTH, 0.0F, matrices, buffers, light, overlay);
        }
    }
}
