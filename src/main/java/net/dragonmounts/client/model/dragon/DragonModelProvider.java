package net.dragonmounts.client.model.dragon;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import static net.dragonmounts.util.math.MathUtil.TO_RAD_FACTOR;

public enum DragonModelProvider implements EntityModelLayerRegistry.TexturedModelDataProvider {
    INSTANCE;
    public static final int HEAD_SIZE = 16;
    public static final int HEAD_OFS = -16;
    public static final int JAW_WIDTH = 12;
    public static final int JAW_HEIGHT = 5;
    public static final int JAW_LENGTH = 16;
    public static final int HORN_THICK = 3;
    public static final int HORN_LENGTH = 12;
    public static final float HORN_OFS = -HORN_THICK / 2F;


    @Override
    public LayerDefinition createModelData() {
        MeshDefinition model = new MeshDefinition();
        PartDefinition root = model.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .addBox("head", -8.0F, -8.0F, 6.0F + HEAD_OFS, HEAD_SIZE, HEAD_SIZE, HEAD_SIZE)
                        .texOffs(56, 88)
                        .addBox("upper_jaw", -6.0F, -1.0F, -8.0F + HEAD_OFS, JAW_WIDTH, JAW_HEIGHT, JAW_LENGTH)
                        .texOffs(48, 0)
                        .addBox("nostril", -5.0F, -3.0F, -6.0F + HEAD_OFS, 2.0F, 2.0F, 4.0F).mirror()
                        .addBox("nostril", 3.0F, -3.0F, -6.0F + HEAD_OFS, 2.0F, 2.0F, 4.0F),
                PartPose.ZERO
        );
        float rad30 = 30 * TO_RAD_FACTOR;
        head.addOrReplaceChild("left_horn", CubeListBuilder.create().addBox("horn", HORN_OFS, HORN_OFS, HORN_OFS, HORN_THICK, HORN_THICK, HORN_LENGTH, 28, 32), PartPose.offsetAndRotation(-5, -8, 0, rad30, -rad30, 0));
        head.addOrReplaceChild("right_horn", CubeListBuilder.create().mirror().addBox("horn", HORN_OFS, HORN_OFS, HORN_OFS, HORN_THICK, HORN_THICK, HORN_LENGTH, 28, 32), PartPose.offsetAndRotation(5, -8, 0, rad30, rad30, 0));
        head.addOrReplaceChild("jaw", CubeListBuilder.create().addBox("lower_jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16, 0, 88), PartPose.offset(0.0F, 4.0F, 8.0F + HEAD_OFS));
        return LayerDefinition.create(model, 256, 256);
    }
}
