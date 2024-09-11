package net.dragonmounts.client.renderer.dragon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dragonmounts.client.ClientDragonEntity;
import net.dragonmounts.client.variant.VariantAppearances;
import net.dragonmounts.item.DragonArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.client.renderer.RenderType.armorCutoutNoCull;
import static net.minecraft.client.renderer.entity.EnderDragonRenderer.renderCrystalBeams;
import static net.minecraft.client.renderer.entity.ItemRenderer.getArmorFoilBuffer;
import static net.minecraft.client.renderer.entity.LivingEntityRenderer.getOverlayCoords;
import static net.minecraft.client.renderer.entity.LivingEntityRenderer.isEntityUpsideDown;

/**
 * @see net.minecraft.client.renderer.entity.LivingEntityRenderer
 */
public class TameableDragonRenderer extends EntityRenderer<ClientDragonEntity> {
    private static final float EYE_BED_OFFSET = 0.1F;
    public TameableDragonRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0;
    }

//    @Override
//    protected void setupTransforms(ClientDragonEntity dragon, MatrixStack matrices, float ageInTicks, float rotationYaw, float partialTicks) {
//        super.setupTransforms(dragon, matrices, ageInTicks, rotationYaw, partialTicks);
//        float scale = dragon.getScaleFactor() * dragon.getVariant().getAppearance(VariantAppearances.ENDER_FEMALE).renderScale;
//        matrices.scale(scale, scale, scale);
//        this.shadowRadius = dragon.isBaby() ? 4 * scale : 2 * scale;
//        DragonAnimationContext context = dragon.context;
//        matrices.translate(context.getModelOffsetX(), context.getModelOffsetY(), context.getModelOffsetZ());
//        matrices.translate(0, 1.5, 0.5); // change rotation point
//        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(context.getModelPitch(partialTicks))); // rotate near the saddle so we can support the player
//        matrices.translate(0, -1.5, -0.5); // restore rotation point
//    }


    @Override
    public void render(ClientDragonEntity dragon, float entityYaw, float partialTicks, PoseStack matrices, MultiBufferSource buffers, int light) {
        if (dragon.renderCrystalBeams) {
            var crystal = dragon.nearestCrystal;
            if (crystal != null) {
                matrices.pushPose();
                var start = crystal.position();
                var end = dragon.position();
                renderCrystalBeams(
                        (float) (start.x - Mth.lerp(partialTicks, dragon.xo, end.x)),
                        (float) (start.y - Mth.lerp(partialTicks, dragon.yo, end.y)),
                        (float) (start.z - Mth.lerp(partialTicks, dragon.zo, end.z)),
                        partialTicks,
                        dragon.tickCount,
                        matrices,
                        buffers,
                        light
                );
                matrices.popPose();
            }
        }
        matrices.pushPose();
        var appearance = dragon.getVariant().getAppearance(VariantAppearances.ENDER_FEMALE);
        var model = appearance.getModel();
        model.attackTime = dragon.getAttackAnim(partialTicks);
        model.riding = dragon.isPassenger();
        model.young = dragon.isBaby();
        float headRot = Mth.rotLerp(partialTicks, dragon.yHeadRotO, dragon.yHeadRot);
        float bodyRot;
        float deltaRot;
        if (dragon.isPassenger() && dragon.getVehicle() instanceof LivingEntity entity) {
            bodyRot = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
            deltaRot = headRot - bodyRot;
            float rot = Mth.wrapDegrees(deltaRot);
            if (rot < -85.0F) {
                rot = -85.0F;
            } else if (rot > 85.0F) {
                rot = 85.0F;
            }
            bodyRot = headRot - rot;
            if (rot * rot > 2500.0F) {
                bodyRot += rot * 0.2F;
            }
            deltaRot = headRot - bodyRot;
        } else {
            bodyRot = Mth.rotLerp(partialTicks, dragon.yBodyRotO, dragon.yBodyRot);
            deltaRot = headRot - bodyRot;
        }
        float xRot;
        if (isEntityUpsideDown(dragon)) {
            xRot = -Mth.lerp(partialTicks, dragon.xRotO, dragon.getXRot());
            deltaRot = -deltaRot;
        } else {
            xRot = Mth.lerp(partialTicks, dragon.xRotO, dragon.getXRot());
        }
        deltaRot = Mth.wrapDegrees(deltaRot);
        /*if (dragon.hasPose(Pose.SLEEPING)) {
            Direction direction = dragon.getBedOrientation();
            if (direction != null) {
                float k = dragon.getEyeHeight(Pose.STANDING) - 0.1F;
                matrices.translate((float) (-direction.getStepX()) * k, 0.0F, (float) (-direction.getStepZ()) * k);
            }
        }*/
        float scale = dragon.getScale();
        matrices.scale(scale, scale, scale);
        float ticks = dragon.tickCount + partialTicks;
        this.setupRotations(dragon, matrices, ticks, bodyRot, partialTicks, scale);
        matrices.scale(-1.0F, -1.0F, 1.0F);
        matrices.translate(0.0F, -1.501F, 0.0F);
        if (!dragon.isPassenger() && dragon.isAlive()) {
            float limbSwing = dragon.walkAnimation.position(partialTicks);
            float limbSwingAmount = Math.min(1.0F, dragon.walkAnimation.speed(partialTicks));
            if (dragon.isBaby()) {
                limbSwing *= 3.0F;
            }
            model.prepareMobModel(dragon, limbSwing, limbSwingAmount, partialTicks);
            model.setupAnim(dragon, limbSwing, limbSwingAmount, ticks, deltaRot, xRot);
        } else {
            model.prepareMobModel(dragon, 0.0F, 0.0F, partialTicks);
            model.setupAnim(dragon, 0.0F, 0.0F, ticks, deltaRot, xRot);
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean bodyVisible = !dragon.isInvisible();
        boolean translucent = !bodyVisible && !dragon.isInvisibleTo(minecraft.player);
        var texture = this.getTextureLocation(dragon);
        RenderType renderType;
        if (dragon.deathTime > 0) {
            renderType = null;
        } else if (translucent) {
            renderType = RenderType.itemEntityTranslucentCull(texture);
        } else if (bodyVisible) {
            renderType = model.renderType(texture);
        } else {
            renderType = minecraft.shouldEntityAppearGlowing(dragon) ? RenderType.outline(texture) : null;
        }
        if (renderType != null) {
            model.renderToBuffer(matrices, buffers.getBuffer(renderType), light, getOverlayCoords(dragon, 0.0F), translucent ? 654311423 : -1);
        }
        //----layer start
        int onOverlay = OverlayTexture.NO_OVERLAY;
        if (dragon.deathTime > 0) {
            boolean hurt = dragon.hurtTime > 0;
            model.renderToBuffer(matrices, buffers.getBuffer(appearance.getDissolve(dragon)), light, onOverlay, FastColor.ARGB32.color(
                    Mth.floor(dragon.deathTime * 255.0F / dragon.getMaxDeathTime()), -1
            ));
            model.renderToBuffer(matrices, buffers.getBuffer(appearance.getDecal(dragon)), light, OverlayTexture.pack(0.0F, hurt));
            model.renderToBuffer(matrices, buffers.getBuffer(appearance.getGlowDecal(dragon)), 15728640, OverlayTexture.pack(0.0F, hurt));
        } else {
            //saddle
            if (dragon.isSaddled()) {
                model.renderToBuffer(matrices, buffers.getBuffer(RenderType.entityCutoutNoCull(appearance.getSaddle(dragon))), light, OverlayTexture.NO_OVERLAY);
            }
            //chest
            if (dragon.hasChest()) {
                model.renderToBuffer(matrices, buffers.getBuffer(RenderType.entityCutoutNoCull(appearance.getChest(dragon))), light, OverlayTexture.NO_OVERLAY);
            }
            //armor
            var stack = dragon.getBodyArmorItem();
            if (stack.getItem() instanceof DragonArmorItem armor) {
                model.renderToBuffer(matrices, getArmorFoilBuffer(buffers, armorCutoutNoCull(armor.texture), stack.hasFoil()), light, onOverlay);
            }
            //glow
            model.renderToBuffer(matrices, buffers.getBuffer(appearance.getGlow(dragon)), 15728640, onOverlay);
        }
        //----layer end
        matrices.popPose();
        super.render(dragon, entityYaw, partialTicks, matrices, buffers, light);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(ClientDragonEntity dragon) {
        return dragon.getVariant().getAppearance(VariantAppearances.ENDER_FEMALE).getBody(dragon);
    }

    protected void setupRotations(ClientDragonEntity dragon, PoseStack matrices, float bob, float yBodyRot, float partialTick, float scale) {
        if (dragon.deathTime <= 0) {
            if (dragon.isAutoSpinAttack()) {
                matrices.mulPose(Axis.XP.rotationDegrees(-90.0F - dragon.getXRot()));
                matrices.mulPose(Axis.YP.rotationDegrees(((float) dragon.tickCount + partialTick) * -75.0F));
            } else if (isEntityUpsideDown(dragon)) {
                matrices.translate(0.0F, (dragon.getBbHeight() + 0.1F) / scale, 0.0F);
                matrices.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
        }
    }

    @Override
    protected boolean shouldShowName(ClientDragonEntity dragon) {
        if (!super.shouldShowName(dragon)) return false;
        double distance = this.entityRenderDispatcher.distanceToSqr(dragon);
        if (dragon.isDiscrete() ? distance >= 1024 : distance >= 4096) return false;
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        assert player != null;
        boolean visible = !dragon.isInvisibleTo(player);
        var team = dragon.getTeam();
        if (team == null)
            return visible && dragon != minecraft.getCameraEntity() && Minecraft.renderNames() && !dragon.isVehicle();
        var other = player.getTeam();
        return switch (team.getNameTagVisibility()) {
            case ALWAYS -> visible;
            case NEVER -> false;
            case HIDE_FOR_OTHER_TEAMS ->
                    other == null ? visible : team.isAlliedTo(other) && (team.canSeeFriendlyInvisibles() || visible);
            case HIDE_FOR_OWN_TEAM -> other == null ? visible : !team.isAlliedTo(other) && visible;
        };
    }

    @Override
    protected float getShadowRadius(ClientDragonEntity dragon) {
        return super.getShadowRadius(dragon) * dragon.getAgeScale();
    }
}
