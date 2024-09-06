package net.dragonmounts.block;

import com.mojang.serialization.MapCodec;
import net.dragonmounts.block.entity.DragonCoreBlockEntity;
import net.dragonmounts.init.DMBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity.AnimationStatus;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

/**
 * @see net.minecraft.world.level.block.ChestBlock
 * @see net.minecraft.world.level.block.ShulkerBoxBlock
 */
public class DragonCoreBlock extends BaseEntityBlock {
    public static final MapCodec<DragonCoreBlock> CODEC = simpleCodec(DragonCoreBlock::new);

    public static boolean tryPlaceAt(Level level, BlockPos pos, BlockState core, ItemStack stack) {
        var state = level.getBlockState(pos);
        if ((state.canBeReplaced() || state.getPistonPushReaction() != PushReaction.BLOCK) && (state.isAir() || level.destroyBlock(pos, true))) {
            if (level.setBlock(pos, core, 2) && level.getBlockEntity(pos) instanceof DragonCoreBlockEntity entity) {
                entity.setItem(0, stack);
                return true;
            }
        }
        return false;
    }


    public DragonCoreBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public DragonCoreBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DragonCoreBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player.isSpectator()) return InteractionResult.CONSUME;
        if (level.getBlockEntity(pos) instanceof DragonCoreBlockEntity core) {
            AnimationStatus status = core.getAnimationStatus();
            if (status != AnimationStatus.CLOSING && (status != AnimationStatus.CLOSED || level.noCollision(Shulker.getProgressDeltaAabb(1.0F, Direction.UP, 0.0F, 0.5F).move(pos).deflate(1.0E-6)))) {
                player.openMenu(core);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
    }

    @Override
    protected void onRemove(BlockState old, Level level, BlockPos pos, BlockState neo, boolean bl) {
        if (!old.is(neo.getBlock())) {
            level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.3F, level.random.nextFloat() * 0.1F + 0.3F);
            level.playSound(null, pos, SoundEvents.ENDER_EYE_DEATH, SoundSource.BLOCKS, 2.0F, level.random.nextFloat() * 0.1F + 0.3F);
            if (level.getBlockEntity(pos) instanceof DragonCoreBlockEntity core) {
                Containers.dropContents(level, pos, core);
                level.updateNeighbourForOutputSignal(pos, old.getBlock());
            }
            super.onRemove(old, level, pos, neo, bl);
        }
    }

    @Override
    protected @NotNull VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof DragonCoreBlockEntity core) {
            if (!core.isClosed()) {
                return Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
            }
        }
        return Shapes.block();
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return level.getBlockEntity(pos) instanceof DragonCoreBlockEntity core ? Shapes.create(core.getBoundingBox()) : Shapes.block();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 3; ++i) {
            int j = random.nextInt(2) * 2 - 1;
            int k = random.nextInt(2) * 2 - 1;
            //Pos
            double x = pos.getX() + 0.5D + 0.25D * j;
            double y = pos.getY() + random.nextFloat();
            double z = pos.getZ() + 0.75D + 0.25D * k;
            //Speed
            double sx = random.nextFloat() * j;
            double sy = (random.nextFloat() - 0.5D) * 0.125D;
            double sz = random.nextFloat() * k;
            level.addParticle(ParticleTypes.PORTAL, x, y, z, sx, sy, sz);
        }
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
    public @NotNull MapCodec<DragonCoreBlock> codec() {
        return CODEC;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, DMBlockEntities.DRAGON_CORE, DragonCoreBlockEntity::tick);
    }
}
