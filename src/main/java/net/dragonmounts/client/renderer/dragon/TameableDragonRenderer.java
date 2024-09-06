package net.dragonmounts.client.renderer.dragon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.dragonmounts.client.ClientDragonEntity;
import net.dragonmounts.client.variant.VariantAppearances;
import net.dragonmounts.item.DragonArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
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
//    protected RenderLayer getRenderLayer(ClientDragonEntity dragon, boolean visible, boolean invisibleToClient, boolean glowing) {
//        // During death, do not use the standard rendering and let the death layer handle it. Hacky, but better than mixins.
//        return dragon.deathTime > 0 ? null : super.getRenderLayer(dragon, visible, invisibleToClient, glowing);
//    }

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
                float x = (float) (crystal.getX() - Mth.lerp(partialTicks, dragon.xo, dragon.getX()));
                float y = (float) (crystal.getY() - Mth.lerp(partialTicks, dragon.yo, dragon.getY()));
                float z = (float) (crystal.getZ() - Mth.lerp(partialTicks, dragon.zo, dragon.getZ()));
                renderCrystalBeams(x, y + EndCrystalRenderer.getY(crystal, partialTicks), z, partialTicks, dragon.tickCount, matrices, buffers, light);
                matrices.popPose();
            }
        }
        matrices.pushPose();
        var appearance = dragon.getVariant().getAppearance(VariantAppearances.ENDER_FEMALE);
        var model = appearance.getModel();
        model.attackTime = this.getAttackAnim(dragon, partialTicks);
        model.riding = dragon.isPassenger();
        model.young = dragon.isBaby();
        float f = Mth.rotLerp(partialTicks, dragon.yBodyRotO, dragon.yBodyRot);
        float g = Mth.rotLerp(partialTicks, dragon.yHeadRotO, dragon.yHeadRot);
        float h = g - f;
        if (dragon.isPassenger() && dragon.getVehicle() instanceof LivingEntity livingEntity) {
            f = Mth.rotLerp(partialTicks, livingEntity.yBodyRotO, livingEntity.yBodyRot);
            h = g - f;
            float i = Mth.wrapDegrees(h);
            if (i < -85.0F) {
                i = -85.0F;
            } else if (i >= 85.0F) {
                i = 85.0F;
            }

            f = g - i;
            if (i * i > 2500.0F) {
                f += i * 0.2F;
            }

            h = g - f;
        }
        float j = Mth.lerp(partialTicks, dragon.xRotO, dragon.getXRot());
        if (isEntityUpsideDown(dragon)) {
            j *= -1.0F;
            h *= -1.0F;
        }
        h = Mth.wrapDegrees(h);
        if (dragon.hasPose(Pose.SLEEPING)) {
            Direction direction = dragon.getBedOrientation();
            if (direction != null) {
                float k = dragon.getEyeHeight(Pose.STANDING) - 0.1F;
                matrices.translate((float) (-direction.getStepX()) * k, 0.0F, (float) (-direction.getStepZ()) * k);
            }
        }
        float scale = dragon.getScale();
        matrices.scale(scale, scale, scale);
        float k = this.getBob(dragon, partialTicks);
        this.setupRotations(dragon, matrices, k, f, partialTicks, scale);
        matrices.scale(-1.0F, -1.0F, 1.0F);
        matrices.translate(0.0F, -1.501F, 0.0F);
        float l = 0.0F;
        float m = 0.0F;
        if (!dragon.isPassenger() && dragon.isAlive()) {
            l = dragon.walkAnimation.speed(partialTicks);
            m = dragon.walkAnimation.position(partialTicks);
            if (dragon.isBaby()) {
                m *= 3.0F;
            }

            if (l > 1.0F) {
                l = 1.0F;
            }
        }
        model.prepareMobModel(dragon, m, l, partialTicks);
        model.setupAnim(dragon, m, l, k, h, j);
        Minecraft minecraft = Minecraft.getInstance();
        boolean bodyVisible = this.isBodyVisible(dragon);
        boolean translucent = !bodyVisible && !dragon.isInvisibleTo(minecraft.player);
        RenderType renderType;
        ResourceLocation resourceLocation = this.getTextureLocation(dragon);
        if (!bodyVisible && !dragon.isInvisibleTo(minecraft.player)) {
            renderType = RenderType.itemEntityTranslucentCull(resourceLocation);
        } else if (bodyVisible) {
            renderType = model.renderType(resourceLocation);
        } else {
            renderType = minecraft.shouldEntityAppearGlowing(dragon) ? RenderType.outline(resourceLocation) : null;
        }
        if (renderType != null) {
            VertexConsumer vertexConsumer = buffers.getBuffer(renderType);
            int n = getOverlayCoords(dragon, this.getWhiteOverlayProgress(dragon, partialTicks));
            model.renderToBuffer(matrices, vertexConsumer, light, n, translucent ? 654311423 : -1);
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
            return;
        }
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
        //----layer end
        matrices.popPose();
        super.render(dragon, entityYaw, partialTicks, matrices, buffers, light);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(ClientDragonEntity dragon) {
        return dragon.getVariant().getAppearance(VariantAppearances.ENDER_FEMALE).getBody(dragon);
    }

    protected boolean isBodyVisible(ClientDragonEntity dragon) {
        return !dragon.isInvisible();
    }

    private static float sleepDirectionToRotation(Direction facing) {
        return switch (facing) {
            case SOUTH -> 90.0F;
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            default -> 0.0F;
        };
    }

    protected boolean isShaking(ClientDragonEntity dragon) {
        return dragon.isFullyFrozen();
    }

    protected void setupRotations(ClientDragonEntity dragon, PoseStack matrices, float bob, float yBodyRot, float partialTick, float scale) {
        if (this.isShaking(dragon)) {
            yBodyRot += (float) (Math.cos((double) dragon.tickCount * 3.25) * Math.PI * 0.4F);
        }

        if (!dragon.hasPose(Pose.SLEEPING)) {
            matrices.mulPose(Axis.YP.rotationDegrees(180.0F - yBodyRot));
        }

        if (dragon.deathTime <= 0) {
            if (dragon.isAutoSpinAttack()) {
                matrices.mulPose(Axis.XP.rotationDegrees(-90.0F - dragon.getXRot()));
                matrices.mulPose(Axis.YP.rotationDegrees(((float) dragon.tickCount + partialTick) * -75.0F));
            } else if (dragon.hasPose(Pose.SLEEPING)) {
                Direction direction = dragon.getBedOrientation();
                float g = direction != null ? sleepDirectionToRotation(direction) : yBodyRot;
                matrices.mulPose(Axis.YP.rotationDegrees(g));
                matrices.mulPose(Axis.YP.rotationDegrees(270.0F));
            } else if (isEntityUpsideDown(dragon)) {
                matrices.translate(0.0F, (dragon.getBbHeight() + 0.1F) / scale, 0.0F);
                matrices.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
        }
    }

    protected float getAttackAnim(ClientDragonEntity dragon, float partialTickTime) {
        return dragon.getAttackAnim(partialTickTime);
    }

    protected float getBob(ClientDragonEntity dragon, float partialTick) {
        return (float) dragon.tickCount + partialTick;
    }

    protected float getWhiteOverlayProgress(ClientDragonEntity dragon, float partialTicks) {
        return 0.0F;
    }

    @Override
    protected boolean shouldShowName(ClientDragonEntity dragon) {
        if (!dragon.shouldShowName() || (
                dragon.hasCustomName() && dragon != this.entityRenderDispatcher.crosshairPickEntity
        )) {
            return false;
        }
        double distance = this.entityRenderDispatcher.distanceToSqr(dragon);
        if (dragon.isDiscrete() ? distance >= 1024 : distance >= 4096) return false;
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        assert player != null;
        boolean visible = !dragon.isInvisibleTo(player);
        var self = dragon.getTeam();
        if (self == null) return
                Minecraft.renderNames() && dragon != minecraft.getCameraEntity() && visible && !dragon.isVehicle();
        var team = player.getTeam();
        return switch (self.getNameTagVisibility()) {
            case ALWAYS -> visible;
            case NEVER -> false;
            case HIDE_FOR_OTHER_TEAMS ->
                    team == null ? visible : self.isAlliedTo(team) && (self.canSeeFriendlyInvisibles() || visible);
            case HIDE_FOR_OWN_TEAM -> team == null ? visible : !self.isAlliedTo(team) && visible;
        };
    }

    @Override
    protected float getShadowRadius(ClientDragonEntity dragon) {
        return super.getShadowRadius(dragon) * dragon.getAgeScale();
    }
}
