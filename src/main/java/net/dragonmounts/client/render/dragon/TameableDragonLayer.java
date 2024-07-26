package net.dragonmounts.client.render.dragon;

import net.dragonmounts.client.ClientDragonEntity;
import net.dragonmounts.client.model.dragon.DragonModel;
import net.dragonmounts.client.variant.VariantAppearance;
import net.dragonmounts.client.variant.VariantAppearances;
import net.dragonmounts.item.DragonArmorItem;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TameableDragonLayer extends FeatureRenderer<ClientDragonEntity, DragonModel> {
    public TameableDragonLayer(FeatureRendererContext<ClientDragonEntity, DragonModel> renderer) {
        super(renderer);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider buffer, int light, ClientDragonEntity dragon, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        DragonModel model = this.getContextModel();
        VariantAppearance appearance = dragon.getVariant().getAppearance(VariantAppearances.ENDER_FEMALE);
        int defaultUV = OverlayTexture.DEFAULT_UV;
        if (dragon.deathTime > 0) {
            model.render(matrices, buffer.getBuffer(appearance.getDissolve(dragon)), light, defaultUV, 1.0F, 1.0F, 1.0F, 1.0F);
            model.render(matrices, buffer.getBuffer(appearance.getDecal(dragon)), light, defaultUV, 1.0F, 1.0F, 1.0F, 1.0F);
            model.render(matrices, buffer.getBuffer(appearance.getGlowDecal(dragon)), 15728640, defaultUV, 1.0F, 1.0F, 1.0F, 1.0F);
            return;
        }
        //saddle
        if (dragon.isSaddled()) {
            renderModel(model, appearance.getSaddle(dragon), matrices, buffer, light, dragon, 1.0F, 1.0F, 1.0F);
        }
        //chest
        if (dragon.hasChest()) {
            renderModel(model, appearance.getChest(dragon), matrices, buffer, light, dragon, 1.0F, 1.0F, 1.0F);
        }
        //armor
        ItemStack stack = dragon.getArmorStack();
        Item item = stack.getItem();
        if (item instanceof DragonArmorItem) {
            VertexConsumer builder = ItemRenderer.getArmorGlintConsumer(buffer, RenderLayer.getArmorCutoutNoCull(((DragonArmorItem) item).getDragonArmorTexture(stack, dragon)), false, stack.hasGlint());
            model.render(matrices, builder, light, defaultUV, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        //glow
        model.render(matrices, buffer.getBuffer(appearance.getGlow(dragon)), 15728640, defaultUV, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
