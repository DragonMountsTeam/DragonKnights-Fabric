package net.dragonmounts.util;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlockUtil {
    public static void updateNeighborStates(Level level, BlockPos pos, BlockState state, int flag) {
        state.updateNeighbourShapes(level, pos, flag);
        level.updateNeighborsAt(pos, state.getBlock());
    }
}
