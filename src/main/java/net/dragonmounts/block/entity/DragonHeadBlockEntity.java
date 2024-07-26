package net.dragonmounts.block.entity;

import net.dragonmounts.init.DMBlockEntities;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Tickable;

/**
 * @see net.minecraft.block.entity.SkullBlockEntity
 */
public class DragonHeadBlockEntity extends BlockEntity implements Tickable {
    public DragonHeadBlockEntity() {
        super(DMBlockEntities.DRAGON_HEAD);
    }

    private int ticks;
    private boolean active;

    @SuppressWarnings("AssignmentUsedAsCondition")
    @Override
    public void tick() {
        //noinspection DataFlowIssue
        if (this.active /*--> */ = /* <--*/ this.world.isReceivingRedstonePower(this.pos)) {
            ++this.ticks;
        }
    }

    public float getAnimation(float partialTicks) {
        return this.active ? partialTicks + (float) this.ticks : (float) this.ticks;
    }
}
