package net.dragonmounts.block.entity;

import net.dragonmounts.init.DMBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED;

/**
 * @see net.minecraft.world.level.block.entity.SkullBlockEntity
 */
public class DragonHeadBlockEntity extends BlockEntity {
    public DragonHeadBlockEntity(BlockPos pos, BlockState state) {
        super(DMBlockEntities.DRAGON_HEAD, pos, state);
    }

    private int ticks;
    private boolean active;

    public float getAnimation(float partialTicks) {
        return this.active ? partialTicks + (float) this.ticks : (float) this.ticks;
    }

    @SuppressWarnings("AssignmentUsedAsCondition")
    public static void animation(Level level, BlockPos pos, BlockState state, DragonHeadBlockEntity entity) {
        if (entity.active /*--> */ = /* <--*/ state.hasProperty(POWERED) && state.getValue(POWERED)) {
            ++entity.ticks;
        }
    }
}
