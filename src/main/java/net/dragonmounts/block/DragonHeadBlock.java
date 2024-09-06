package net.dragonmounts.block;

import com.mojang.serialization.MapCodec;
import net.dragonmounts.registry.DragonVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.ROTATION_16;

public class DragonHeadBlock extends AbstractDragonHeadBlock {
    public static final MapCodec<DragonHeadBlock> CODEC = makeCodec(DragonHeadBlock::new);

    protected static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D);

    public DragonHeadBlock(DragonVariant variant, Properties props) {
        super(variant, props, false);
        this.registerDefaultState(this.defaultBlockState().setValue(ROTATION_16, 0));
    }

    @Override
    public float getYRotation(BlockState state) {
        return 180F + 22.5F * state.getValue(ROTATION_16);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @NotNull VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        //noinspection DataFlowIssue
        return super.getStateForPlacement(context).setValue(ROTATION_16, RotationSegment.convertToSegment(context.getRotation()));
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ROTATION_16, rotation.rotate(state.getValue(ROTATION_16), 16));
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ROTATION_16, mirror.mirror(state.getValue(ROTATION_16), 16));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ROTATION_16);
    }

    @Override
    protected @NotNull MapCodec<? extends DragonHeadBlock> codec() {
        return CODEC;
    }
}
