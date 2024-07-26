package net.dragonmounts.client.render.block;

import net.dragonmounts.block.entity.DragonCoreBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

import static net.dragonmounts.DragonMounts.MOD_ID;
import static net.minecraft.state.property.Properties.HORIZONTAL_FACING;

/**
 * @see net.minecraft.client.render.block.entity.ShulkerBoxBlockEntityRenderer
 */
public class DragonCoreRenderer extends BlockEntityRenderer<DragonCoreBlockEntity> {
    private static final Identifier TEXTURE_LOCATION = new Identifier(MOD_ID, "textures/blocks/dragon_core.png");
    private static final RenderLayer RENDER_LAYER = RenderLayer.getEntityCutoutNoCull(TEXTURE_LOCATION);
    public static final BuiltinItemRendererRegistry.DynamicItemRenderer ITEM_RENDERER;

    static {
        ShulkerEntityModel<?> model = new ShulkerEntityModel<>();
        ITEM_RENDERER = (stack, mode, matrices, vertex, light, overlay) -> DragonCoreRenderer.render(Direction.SOUTH, model, 0.0F, matrices, vertex, light, overlay);
    }

    private final ShulkerEntityModel<?> model = new ShulkerEntityModel<>();

    public DragonCoreRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(DragonCoreBlockEntity core, float ticks, MatrixStack matrices, VertexConsumerProvider vertex, int light, int overlay) {
        render(core.getCachedState().get(HORIZONTAL_FACING), this.model, core.getProgress(ticks), matrices, vertex, light, overlay);
    }

    public static void render(Direction direction, ShulkerEntityModel<?> model, float progress, MatrixStack matrices, VertexConsumerProvider vertex, int light, int overlay) {
        VertexConsumer buffer = vertex.getBuffer(RENDER_LAYER);
        matrices.push();
        matrices.translate(0.5D, 0.5D, 0.5D);
        matrices.scale(0.9995F, 0.9995F, 0.9995F);
        matrices.scale(1.0F, -1.0F, -1.0F);
        matrices.translate(0.0D, -1.0D, 0.0D);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(direction.asRotation()));
        model.getBottomShell().render(matrices, buffer, light, overlay);
        matrices.translate(0.0D, -progress * 0.5D, 0.0D);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(270.0F * progress));
        model.getTopShell().render(matrices, buffer, light, overlay);
        matrices.pop();
    }
}
