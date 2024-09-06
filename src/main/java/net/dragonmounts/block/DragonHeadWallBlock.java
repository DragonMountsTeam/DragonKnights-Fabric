package net.dragonmounts.block;

import com.mojang.serialization.MapCodec;
import net.dragonmounts.registry.DragonVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class DragonHeadWallBlock extends AbstractDragonHeadBlock {
    public static final MapCodec<DragonHeadWallBlock> CODEC = makeCodec(DragonHeadWallBlock::new);
    private static final EnumMap<Direction, VoxelShape> AABBS = new EnumMap<>(Direction.class);

    static {
        AABBS.put(Direction.NORTH, box(4.0D, 4.0D, 8.0D, 12.0D, 12.0D, 16.0D));
        AABBS.put(Direction.SOUTH, box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 8.0D));
        AABBS.put(Direction.EAST, box(0.0D, 4.0D, 4.0D, 8.0D, 12.0D, 12.0D));
        AABBS.put(Direction.WEST, box(8.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D));
    }

    public DragonHeadWallBlock(DragonVariant variant, Properties props) {
        super(variant, props, true);
        this.registerDefaultState(this.defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public float getYRotation(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).toYRot();
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AABBS.get(state.getValue(HORIZONTAL_FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var pos = context.getClickedPos();
        var level = context.getLevel();
        for (var direction : context.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal() && !level.getBlockState(pos.relative(direction)).canBeReplaced(context)) {
                //noinspection DataFlowIssue
                return super.getStateForPlacement(context).setValue(HORIZONTAL_FACING, direction.getOpposite());
            }
        }
        return null;
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(HORIZONTAL_FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HORIZONTAL_FACING);
    }

    @Override
    protected @NotNull MapCodec<? extends DragonHeadWallBlock> codec() {
        return CODEC;
    }
}
