//package net.dragonmounts.client.model.dragon;
//
//import net.minecraft.client.model.Model;
//import net.minecraft.client.model.geom.ModelPart;
//
//public class DragonBodyModelPart extends ModelPart {
//    public final ModelPart back;
//    public final ModelPart chest;
//    public final ModelPart saddle;
//
//    public DragonBodyModelPart(Model model) {
//        super(model);
//        this.buildBody();
//        this.addChild(this.back = this.createBack(model));
//        this.addChild(this.chest = this.createChest(model));
//        this.addChild(this.saddle = this.createSaddle(model));
//    }
//
//    protected void buildBody() {
//        this.setPivot(0, 4, 8);
//        this
//                //body
//                .setTextureOffset(0, 0)
//                .addCuboid(-12, 0, -16, 24, 24, 64)
//                //scale
//                .setTextureOffset(0, 32)
//                .addCuboid(-1, -6, 10, 2, 6, 12)
//                .setTextureOffset(0, 32)
//                .addCuboid(-1, -6, 30, 2, 6, 12)
//                //heart
//                .setTextureOffset(130, 110)
//                .addCuboid(-4, 12, -5, 8, 6, 15);
//    }
//
//    protected ModelPart createBack(Model model) {
//        return new ModelPart(model)
//                .setTextureOffset(0, 32)
//                .addCuboid(-1, -6, -10, 2, 6, 12);
//    }
//
//    protected ModelPart createChest(Model model) {
//        return new ModelPart(model)
//                .setTextureOffset(192, 132)
//                .addCuboid(12, 0, 21, 4, 12, 12)
//                .setTextureOffset(224, 132)
//                .addCuboid(-16, 0, 21, 4, 12, 12);
//    }
//
//    protected ModelPart createSaddle(Model model) {
//        return new ModelPart(model)
//                .setTextureOffset(184, 98)
//                .addCuboid(-7, -2, -15, 15, 3, 20)
//                .setTextureOffset(214, 120)
//                .addCuboid(-3, -3, -14, 6, 1, 2)
//                .addCuboid(-6, -4, 2, 13, 2, 2)
//                .setTextureOffset(220, 100)
//                .addCuboid(12, 0, -14, 1, 14, 2)
//                .addCuboid(-13, 0, -14, 1, 10, 2)
//                .setTextureOffset(224, 132)
//                .addCuboid(12, 14, -15, 1, 5, 4)
//                .addCuboid(-13, 10, -15, 1, 5, 4);
//    }
//}
