package net.dragonmounts.client.variant;

import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import static net.dragonmounts.util.RenderStateAccessor.ENTITY_TRANSLUCENT_EMISSIVE_DECAL;

public class AgeableAppearance extends VariantAppearance {
    public final ResourceLocation body;
    public final ResourceLocation babyBody;
    public final RenderType bodyForShoulder;
    public final RenderType bodyForBlock;
    public final RenderType decal;
    public final RenderType babyDecal;
    public final RenderType glow;
    public final RenderType babyGlow;
    public final RenderType glowDecal;
    public final RenderType babyGlowDecal;
    public final boolean hasTailHorns;
    public final boolean hasSideTailScale;

    public AgeableAppearance(
            ResourceLocation babyBody,
            ResourceLocation babyGlow,
            ResourceLocation body,
            ResourceLocation glow,
            boolean hasTailHorns,
            boolean hasSideTailScale
    ) {
        super(MODEL, 1.6F);
        this.body = body;
        this.bodyForShoulder = RenderType.entityCutoutNoCull(babyBody);
        this.bodyForBlock = RenderType.entityCutoutNoCullZOffset(body);
        this.decal = RenderType.entityDecal(body);
        this.babyBody = babyBody;
        this.babyDecal = RenderType.entityDecal(babyBody);
        this.glow = RenderType.entityTranslucentEmissive(glow, false);
        this.glowDecal = ENTITY_TRANSLUCENT_EMISSIVE_DECAL.apply(glow);
        this.babyGlow = RenderType.entityTranslucentEmissive(babyGlow, false);
        this.babyGlowDecal = ENTITY_TRANSLUCENT_EMISSIVE_DECAL.apply(babyGlow);
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
        return dragon.isBaby() ? this.babyBody : this.body;
    }

    @Override
    public RenderType getGlow(TameableDragonEntity dragon) {
        return dragon.isBaby() ? this.babyGlow : this.glow;
    }

    @Override
    public RenderType getDecal(TameableDragonEntity dragon) {
        return dragon.isBaby() ? this.babyDecal : this.decal;
    }

    @Override
    public RenderType getGlowDecal(TameableDragonEntity dragon) {
        return dragon.isBaby() ? this.babyGlowDecal : this.glowDecal;
    }

    @Override
    public RenderType getBodyForShoulder() {
        return this.bodyForShoulder;
    }

    @Override
    public RenderType getGlowForShoulder() {
        return this.babyGlow;
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
