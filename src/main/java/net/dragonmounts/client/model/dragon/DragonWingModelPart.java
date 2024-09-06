//package net.dragonmounts.client.model.dragon;
//
//import net.minecraft.client.model.Model;
//import net.minecraft.client.model.ModelPart;
//
//public class DragonWingModelPart extends ModelPart {
//    public static final int FINGER_COUNT = 4;
//    public final ModelPart forearm;
//    protected final ModelPart[] fingers = new ModelPart[FINGER_COUNT];
//
//    public DragonWingModelPart(Model model) {
//        super(model);
//        this.buildWing();
//        this.addChild(this.forearm = this.createForearm(model));
//        for (int i = 0; i < FINGER_COUNT; ++i) {
//            this.forearm.addChild(this.fingers[i] = createFinger(model, i + 1 == FINGER_COUNT));
//        }
//    }
//
//    protected void buildWing() {
//        //setRenderScale(1.1F);
//        this.setPivot(-10, 5, 4);
//        this
//                .setTextureOffset(0, 152)
//                .addCuboid(-28, -3, -3, 28, 6, 6)
//                .setTextureOffset(116, 232)
//                .addCuboid(-28, 0, 2, 28, 0, 24);
//    }
//
//    protected ModelPart createForearm(Model model) {
//        final ModelPart part = new ModelPart(model);
//        part.setPivot(-28, 0, 0);
//        return part.setTextureOffset(0, 164).addCuboid(-48, -2, -2, 48, 4, 4);
//    }
//
//    protected ModelPart createFinger(Model model, boolean small) {
//        final ModelPart part = new ModelPart(model);
//        part.setPivot(-47, 0, 0);
//        if (small) {
//            part.setTextureOffset(-32, 224).addCuboid(-70, 0, 1, 70, 0, 32);
//        } else {
//            part.setTextureOffset(-49, 176).addCuboid(-70, 0, 1, 70, 0, 48);
//        }
//        return part.setTextureOffset(0, 172).addCuboid(-70, -1, -1, 70, 2, 2);
//    }
//
//    public ModelPart getFinger(int index) {
//        if (index >= 0 && index < 4) {
//            return this.fingers[index];
//        }
//        throw new IndexOutOfBoundsException();
//    }
//}
