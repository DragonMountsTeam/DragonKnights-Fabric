package net.dragonmounts.client.model.dragon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dragonmounts.client.ClientDragonEntity;
import net.dragonmounts.util.math.MathUtil;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class DragonModel extends EntityModel<ClientDragonEntity> {
    public final ModelPart head;
    public final ModelPart jaw;

    public DragonModel(ModelPart root) {
        this.head = root.getChild("head");
        this.jaw = this.head.getChild("jaw");
    }

    @Override
    public void setupAnim(ClientDragonEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer buffer, int light, int overlay, int color) {
        this.head.render(matrices, buffer, light, overlay, color);
    }

    public void setupBlock(float ticks, float yRot, float scale) {
        ModelPart head = this.head;
        head.xScale = head.yScale = head.zScale = scale;
        this.jaw.xRot = Mth.sin(ticks * MathUtil.PI * 0.2F) * 0.2F + 0.2F;
        head.yRot = yRot * MathUtil.TO_RAD_FACTOR;
        head.y = -6F;
    }
}
