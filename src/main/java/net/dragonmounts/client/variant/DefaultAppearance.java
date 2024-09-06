package net.dragonmounts.client.variant;

import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import static net.dragonmounts.util.RenderStateAccessor.ENTITY_TRANSLUCENT_EMISSIVE_DECAL;

public class DefaultAppearance extends VariantAppearance {
    public final ResourceLocation body;
    public final RenderType bodyForShoulder;
    public final RenderType bodyForBlock;
    public final RenderType decal;
    public final RenderType glow;
    public final RenderType glowDecal;
    public final boolean hasTailHorns;
    public final boolean hasSideTailScale;

    public DefaultAppearance(ResourceLocation body, ResourceLocation glow, boolean hasTailHorns, boolean hasSideTailScale) {
        super(MODEL, 1.6F);
        this.body = body;
        this.bodyForShoulder = RenderType.entityCutoutNoCull(body);
        this.bodyForBlock = RenderType.entityCutoutNoCullZOffset(body);
        this.decal = RenderType.entityDecal(body);
        this.glow = RenderType.entityTranslucentEmissive(glow);
        this.glowDecal = ENTITY_TRANSLUCENT_EMISSIVE_DECAL.apply(glow);
        this.hasTailHorns = hasTailHorns;
        this.hasSideTailScale = hasSideTailScale;
    }

    @Override
    public boolean hasTailHorns(TameableDragonEntity dragon) {
        return this.hasTailHorns;
    }

    @Override
    public boolean hasSideTailScale(TameableDragonEntity dragon) {
        return this.hasSideTailScale;
    }

    @Override
    public boolean hasTailHornsOnShoulder() {
        return this.hasTailHorns;
    }

    @Override
    public boolean hasSideTailScaleOnShoulder() {
        return this.hasSideTailScale;
    }

    @Override
    public ResourceLocation getBody(TameableDragonEntity dragon) {
        return this.body;
    }

    @Override
    public RenderType getGlow(TameableDragonEntity dragon) {
        return this.glow;
    }

    @Override
    public RenderType getDecal(TameableDragonEntity dragon) {
        return this.decal;
    }

    @Override
    public RenderType getGlowDecal(TameableDragonEntity dragon) {
        return this.glowDecal;
    }

    @Override
    public RenderType getBodyForShoulder() {
        return this.bodyForShoulder;
    }

    @Override
    public RenderType getGlowForShoulder() {
        return this.glow;
    }

    @Override
    public RenderType getBodyForBlock() {
        return this.bodyForBlock;
    }

    @Override
    public RenderType getGlowForBlock() {
        return this.glow;
    }
}
