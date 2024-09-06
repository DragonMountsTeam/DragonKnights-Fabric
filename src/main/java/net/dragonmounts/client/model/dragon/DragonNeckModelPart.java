//package net.dragonmounts.client.model.dragon;
//
//import net.minecraft.client.model.Model;
//import net.minecraft.client.model.ModelPart;
//import net.minecraft.client.render.VertexConsumer;
//import net.minecraft.client.util.math.MatrixStack;
//
//public class DragonNeckModelPart extends ScaledModelPart {
//    public static final int NECK_SIZE = 10;
//    public static final int NECK_SEGMENT_COUNT_INT = 7;
//    public static final float NECK_SEGMENT_COUNT_FLOAT = 7F;
//    protected final Segment[] segments = new Segment[NECK_SEGMENT_COUNT_INT];
//    public final ModelPart scale;
//
//    public DragonNeckModelPart(Model model) {
//        super(model);
//        this.buildNeck();
//        this.addChild(this.scale = createScale(model));
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
//    protected void buildNeck() {
//        this.setTextureOffset(112, 88).addCuboid(-5, -5, -5, NECK_SIZE, NECK_SIZE, NECK_SIZE);
//    }
//
//    protected ModelPart createScale(Model model) {
//        return new ModelPart(model)
//                .setTextureOffset(0, 0)
//                .addCuboid(-1, -7, -3, 2, 4, 6);
//    }
//
//    @Override
//    public void render(MatrixStack matrices, VertexConsumer builder, int light, int overlay, float red, float green, float blue, float alpha) {
//        for (Segment segment : this.segments) {
//            super.render(matrices, builder, light, overlay, red, green, blue, alpha);
//        }
//    }
//
//    public static class Segment extends ModelSegment<DragonNeckModelPart> {
//        public boolean scaleVisible;
//
//        @Override
//        public Segment save(DragonNeckModelPart model) {
//            super.save(model);
//            this.scaleVisible = model.scale.visible;
//            return this;
//        }
//
//    }
//}
