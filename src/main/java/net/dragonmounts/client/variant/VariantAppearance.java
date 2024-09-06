package net.dragonmounts.client.variant;

import net.dragonmounts.client.model.dragon.DragonModel;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import static net.dragonmounts.DragonMounts.makeId;

public abstract class VariantAppearance implements InvalidateRenderStateCallback {
    public final static String TEXTURES_ROOT = "textures/entity/dragon/";
    public static final ModelLayerLocation MODEL = new ModelLayerLocation(makeId("dragon"), "main");
    public final static ResourceLocation DEFAULT_CHEST = makeId(TEXTURES_ROOT + "chest.png");
    public final static ResourceLocation DEFAULT_SADDLE = makeId(TEXTURES_ROOT + "saddle.png");
    public final static ResourceLocation DEFAULT_DISSOLVE = makeId(TEXTURES_ROOT + "dissolve.png");
    public final ModelLayerLocation modelLocation;
    public final float positionScale;
    public final float renderScale;

    private DragonModel model;

    public VariantAppearance(ModelLayerLocation location, float modelScale) {
        this.modelLocation = location;
        this.renderScale = modelScale;
        this.positionScale = modelScale / 16.0F;
        InvalidateRenderStateCallback.EVENT.register(this);
    }

    @Override
    public final void onInvalidate() {
        this.model = new DragonModel(Minecraft.getInstance().getEntityModels().bakeLayer(this.modelLocation));
    }

    public DragonModel getModel() {
        return this.model;
    }

    public abstract boolean hasTailHorns(TameableDragonEntity dragon);

    public abstract boolean hasSideTailScale(TameableDragonEntity dragon);

    public abstract boolean hasTailHornsOnShoulder();

    public abstract boolean hasSideTailScaleOnShoulder();

    public abstract ResourceLocation getBody(TameableDragonEntity dragon);

    public abstract RenderType getGlow(TameableDragonEntity dragon);

    public abstract RenderType getDecal(TameableDragonEntity dragon);

    public abstract RenderType getGlowDecal(TameableDragonEntity dragon);

    public abstract RenderType getBodyForShoulder();

    public abstract RenderType getGlowForShoulder();

    public abstract RenderType getBodyForBlock();

    public abstract RenderType getGlowForBlock();

    public ResourceLocation getChest(TameableDragonEntity dragon) {
        return DEFAULT_CHEST;
    }

    public ResourceLocation getSaddle(TameableDragonEntity dragon) {
        return DEFAULT_SADDLE;
    }

    public RenderType getDissolve(TameableDragonEntity dragon) {
        return RenderType.dragonExplosionAlpha(DEFAULT_DISSOLVE);
    }
}
