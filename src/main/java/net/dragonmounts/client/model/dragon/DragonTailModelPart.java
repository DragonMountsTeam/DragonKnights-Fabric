//package net.dragonmounts.client.model.dragon;
//
//import net.minecraft.client.model.Model;
//import net.minecraft.client.model.ModelPart;
//import net.minecraft.client.render.VertexConsumer;
//import net.minecraft.client.util.math.MatrixStack;
//
//import static net.dragonmounts.util.ModelUtil.applyRotateAngle;
//import static net.dragonmounts.util.math.MathUtil.TO_RAD_FACTOR;
//
//public class DragonTailModelPart extends ScaledModelPart {
//    public static final int TAIL_SIZE = 10;
//    public static final int TAIL_SEGMENT_COUNT_INT = 12;
//    public static final float TAIL_SEGMENT_COUNT_FLOAT = 12F;
//    public static final int HORN_THICK = 3;
//    public static final int HORN_LENGTH = 32;
//    public static final float HORN_OFS = -HORN_THICK / 2F;
//    protected final Segment[] segments = new Segment[TAIL_SEGMENT_COUNT_INT];
//    public final ModelPart middleScale;
//    public final ModelPart leftScale;
//    public final ModelPart rightScale;
//    public final ModelPart leftHorn;
//    public final ModelPart rightHorn;
//
//    public DragonTailModelPart(Model model) {
//        super(model);
//        this.buildTail();
//        final float rot = 45F * TO_RAD_FACTOR;
//        this.addChild(applyRotateAngle(this.leftScale = createScale(model), 0, 0, rot));
//        this.addChild(applyRotateAngle(this.rightScale = createScale(model), 0, 0, -rot));
//        this.addChild(this.middleScale = createScale(model));
//        this.addChild(this.leftHorn = createHorn(model, true));
//        this.addChild(this.rightHorn = createHorn(model, false));
//        for (int i = 0; i < this.segments.length; ++i) {
//            this.segments[i] = new Segment();
//        }
//    }
//
//    public void save(int index) {
//        if (index < 0 || index >= this.segments.length) {
//            throw new IndexOutOfBoundsException();
//        }
//        this.segments[index].save(this);
//    }
//
//    protected void buildTail() {
//        this.setTextureOffset(152, 88).addCuboid(-5, -5, -5, TAIL_SIZE, TAIL_SIZE, TAIL_SIZE);
//    }
//
//    protected ModelPart createScale(Model model) {
//        return new ModelPart(model)
//                .setTextureOffset(0, 0)
//                .addCuboid(-1, -8, -3, 2, 4, 6);
//    }
//
//    protected ModelPart createHorn(Model model, boolean mirror) {
//        final ModelPart part = new ModelPart(model).setTextureOffset(0, 117).addCuboid(HORN_OFS, HORN_OFS, HORN_OFS, HORN_THICK, HORN_THICK, HORN_LENGTH, mirror);
//        part.setPivot(0, HORN_OFS, TAIL_SIZE / 2F);
//        part.mirror = mirror;
//        return applyRotateAngle(part, Math.toRadians(-15), Math.toRadians(mirror ? 145 : -145), 0);
//    }
//
//    @Override
//    public void render(MatrixStack matrices, VertexConsumer builder, int light, int overlay, float red, float green, float blue, float alpha) {
//        for (Segment segment : this.segments) {
//            super.render(matrices, builder, light, overlay, red, green, blue, alpha);
//        }
//    }
//
//    public static class Segment extends ModelSegment<DragonTailModelPart> {
//        public boolean middleScaleVisible;
//        public boolean leftScaleVisible;
//        public boolean rightScaleVisible;
//        public boolean leftHornVisible;
//        public boolean rightHornVisible;
//
//        @Override
//        public Segment save(DragonTailModelPart model) {
//            super.save(model);
//            this.middleScaleVisible = model.middleScale.visible;
//            this.leftScaleVisible = model.leftScale.visible;
//            this.rightScaleVisible = model.rightScale.visible;
//            this.leftHornVisible = model.leftHorn.visible;
//            this.rightHornVisible = model.rightHorn.visible;
//            return this;
//        }
//
//
//    }
//}
