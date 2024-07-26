package net.dragonmounts.client.model.dragon;

import net.dragonmounts.util.ScaledModelPart;
import net.dragonmounts.util.math.MathUtil;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import static net.dragonmounts.util.ModelUtil.applyRotateAngle;

public class DragonHeadModel extends Model {
    private final Part head;

    public DragonHeadModel() {
        super(RenderLayer::getEntityTranslucent);
        this.textureWidth = this.textureHeight = 256;
        this.head = new Part(this);
    }

    public void setupAnim(float ticks, float yaw, float pitch, float scale) {
        this.head.scaleX = this.head.scaleY = this.head.scaleZ = scale;
        this.head.lowerJaw.pitch = MathHelper.sin(ticks * MathUtil.PI * 0.2F) * 0.2F + 0.2F;
        this.head.yaw = yaw * MathUtil.TO_RAD_FACTOR;
        this.head.pitch = pitch * MathUtil.TO_RAD_FACTOR;
        this.head.pivotY = -6F;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer builder, int light, int overlay, float red, float green, float blue, float alpha) {
        this.head.render(matrices, builder, light, overlay, red, green, blue, alpha);
    }

    public static class Part extends ScaledModelPart {
        public static final int HEAD_SIZE = 16;
        public static final int HEAD_OFS = -16;
        public static final int JAW_WIDTH = 12;
        public static final int JAW_HEIGHT = 5;
        public static final int JAW_LENGTH = 16;
        public static final int HORN_THICK = 3;
        public static final int HORN_LENGTH = 12;
        public static final float HORN_OFS = -HORN_THICK / 2F;
        public final ModelPart lowerJaw;

        public Part(Model model) {
            super(model);
            this.buildHead();
            this.addChild(this.createHorn(model, false));
            this.addChild(this.createHorn(model, true));
            this.addChild(this.lowerJaw = this.createLowerJaw(model));
        }

        protected void buildHead() {
            this
                    //main head
                    .setTextureOffset(0, 0)
                    .addCuboid(-8, -8, 6 + HEAD_OFS, HEAD_SIZE, HEAD_SIZE, HEAD_SIZE)
                    //upper jaw
                    .setTextureOffset(56, 88)
                    .addCuboid(-6, -1, -8 + HEAD_OFS, JAW_WIDTH, JAW_HEIGHT, JAW_LENGTH)
                    //nostril
                    .setTextureOffset(48, 0)
                    .addCuboid(-5, -3, -6 + HEAD_OFS, 2, 2, 4, false)
                    .addCuboid(3, -3, -6 + HEAD_OFS, 2, 2, 4, true);
        }

        protected ModelPart createHorn(Model model, boolean mirror) {
            final ModelPart part = applyRotateAngle(new ModelPart(model), Math.toRadians(30), Math.toRadians(mirror ? 30 : -30), 0);
            part.setPivot(mirror ? 5 : -5, -8, 0);
            return part.setTextureOffset(28, 32).addCuboid(HORN_OFS, HORN_OFS, HORN_OFS, HORN_THICK, HORN_THICK, HORN_LENGTH, mirror);
        }

        protected ModelPart createLowerJaw(Model model) {
            final ModelPart part = new ModelPart(model);
            part.setPivot(0, 4, 8 + HEAD_OFS);
            return part.setTextureOffset(0, 88).addCuboid(-6, 0, -16, 12, 4, 16);
        }
    }
}
