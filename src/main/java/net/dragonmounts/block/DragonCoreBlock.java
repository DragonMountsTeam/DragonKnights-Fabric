package net.dragonmounts.block;

import net.dragonmounts.block.entity.DragonCoreBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity.AnimationStage;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ShulkerLidCollisions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Random;

import static net.minecraft.state.property.Properties.HORIZONTAL_FACING;

/**
 * @see ChestBlock
 * @see ShulkerBoxBlock
 */
public class DragonCoreBlock extends BlockWithEntity {
    public DragonCoreBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public DragonCoreBlockEntity createBlockEntity(BlockView world) {
        return new DragonCoreBlockEntity();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (player.isSpectator()) return ActionResult.CONSUME;
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof DragonCoreBlockEntity) {
            DragonCoreBlockEntity core = (DragonCoreBlockEntity) entity;
            AnimationStage status = core.getAnimationStage();
            if (status != AnimationStage.CLOSING && (status != AnimationStage.CLOSED || world.isSpaceEmpty(ShulkerLidCollisions.getLidCollisionBox(pos, Direction.UP)))) {
                player.openHandledScreen(core);
                player.incrementStat(Stats.OPEN_SHULKER_BOX);
            }
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomName()) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof DragonCoreBlockEntity) {
                ((DragonCoreBlockEntity) entity).setCustomName(stack.getName());
            }
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlayerFacing().getOpposite());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World level, BlockPos pos, BlockState newState, boolean pIsMoving) {
        if (!state.isOf(newState.getBlock())) {
            level.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 0.3F, level.random.nextFloat() * 0.1F + 0.3F);
            level.playSound(null, pos, SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.BLOCKS, 2.0F, level.random.nextFloat() * 0.1F + 0.3F);
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof DragonCoreBlockEntity) {
                ItemScatterer.spawn(level, pos, (Inventory) entity);
                level.updateComparators(pos, state.getBlock());
            }
            super.onStateReplaced(state, level, pos, newState, pIsMoving);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        BlockEntity entity = world.getBlockEntity(pos);
        return entity instanceof DragonCoreBlockEntity ? VoxelShapes.cuboid(((DragonCoreBlockEntity) entity).getBoundingBox()) : VoxelShapes.fullCube();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput((Inventory) world.getBlockEntity(pos));
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
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
            world.addParticle(ParticleTypes.PORTAL, x, y, z, sx, sy, sz);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(HORIZONTAL_FACING, rotation.rotate(state.get(HORIZONTAL_FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(HORIZONTAL_FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
    }
}
