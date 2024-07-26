package net.dragonmounts.client.render.dragon;

import net.dragonmounts.client.model.dragon.DragonLegConfig;
import net.dragonmounts.client.model.dragon.DragonModel;
import net.dragonmounts.client.variant.VariantAppearances;
import net.dragonmounts.config.ClientConfig;
import net.dragonmounts.entity.dragon.DragonLifeStage;
import net.dragonmounts.init.DMEntities;
import net.dragonmounts.registry.DragonVariant;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class TamableDragonOnShoulderLayer<T extends PlayerEntity> extends FeatureRenderer<T, PlayerEntityModel<T>> {
    private final DragonModel model = new DragonModel();

    public TamableDragonOnShoulderLayer(FeatureRendererContext<T, PlayerEntityModel<T>> renderer) {
        super(renderer);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider buffers, int light, T player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (ClientConfig.INSTANCE.debug.get()) {
            double offsetY = player.isInSneakingPose() ? -1.3D : -1.5D;
            render(player.getShoulderEntityLeft(), matrices, buffers, light, 0.4D, offsetY, 0.0D);
            render(player.getShoulderEntityRight(), matrices, buffers, light, -0.4D, offsetY, 0.0D);
        }
    }

    public void render(NbtCompound tag, MatrixStack matrices, VertexConsumerProvider buffers, int light, double offsetX, double offsetY, double offsetZ) {
        if (EntityType.getId(DMEntities.TAMEABLE_DRAGON).toString().equals(tag.getString("id"))) {
            DragonModel model = this.model;
            DragonVariant variant = DragonVariant.REGISTRY.get(new Identifier(tag.getString(DragonVariant.DATA_PARAMETER_KEY)));
            DragonLegConfig config = variant.type.isSkeleton ? DragonLegConfig.SKELETON : DragonLegConfig.DEFAULT;
            float size = DragonLifeStage.getSize(DragonLifeStage.byName(tag.getString(DragonLifeStage.DATA_PARAMETER_KEY)), tag.getInt("Age"));
            model.foreRightLeg.load(config);
            model.hindRightLeg.load(config);
            model.foreLeftLeg.load(config);
            model.hindLeftLeg.load(config);
            matrices.push();
            matrices.translate(offsetX, offsetY, offsetZ);
            model.renderOnShoulder(variant.getAppearance(VariantAppearances.ENDER_FEMALE), matrices, buffers, light, size);
            matrices.pop();
        }
    }
}
