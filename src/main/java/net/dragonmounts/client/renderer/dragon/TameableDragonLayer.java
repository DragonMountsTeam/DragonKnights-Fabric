package net.dragonmounts.client.renderer.dragon;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dragonmounts.client.ClientDragonEntity;
import net.dragonmounts.client.model.dragon.DragonModel;
import net.dragonmounts.client.variant.VariantAppearance;
import net.dragonmounts.client.variant.VariantAppearances;
import net.dragonmounts.item.DragonArmorItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import static net.minecraft.client.renderer.RenderType.armorCutoutNoCull;
import static net.minecraft.client.renderer.entity.ItemRenderer.getArmorFoilBuffer;

public class TameableDragonLayer extends RenderLayer<ClientDragonEntity, DragonModel> {
    public TameableDragonLayer(RenderLayerParent<ClientDragonEntity, DragonModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack matrices, MultiBufferSource buffers, int light, ClientDragonEntity dragon, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        DragonModel model = this.getParentModel();
        VariantAppearance appearance = dragon.getVariant().getAppearance(VariantAppearances.ENDER_FEMALE);
        int onOverlay = OverlayTexture.NO_OVERLAY;
        if (dragon.deathTime > 0) {
            boolean hurt = dragon.hurtTime > 0;
            model.renderToBuffer(matrices, buffers.getBuffer(appearance.getDissolve(dragon)), light, onOverlay, FastColor.ARGB32.color(
                    Mth.floor(dragon.deathTime * 255.0F / dragon.getMaxDeathTime()), -1
            ));
            model.renderToBuffer(matrices, buffers.getBuffer(appearance.getDecal(dragon)), light, OverlayTexture.pack(0.0F, hurt));
            model.renderToBuffer(matrices, buffers.getBuffer(appearance.getGlowDecal(dragon)), 15728640, OverlayTexture.pack(0.0F, hurt));
            return;
        }
        //saddle
        if (dragon.isSaddled()) {
            renderColoredCutoutModel(model, appearance.getSaddle(dragon), matrices, buffers, light, dragon, -1);
        }
        //chest
        if (dragon.hasChest()) {
            renderColoredCutoutModel(model, appearance.getChest(dragon), matrices, buffers, light, dragon, -1);
        }
        //armor
        var stack = dragon.getBodyArmorItem();
        if (stack.getItem() instanceof DragonArmorItem armor) {
            model.renderToBuffer(matrices, getArmorFoilBuffer(buffers, armorCutoutNoCull(armor.texture), stack.hasFoil()), light, onOverlay);
        }
        //glow
        model.renderToBuffer(matrices, buffers.getBuffer(appearance.getGlow(dragon)), 15728640, onOverlay);
    }
}
