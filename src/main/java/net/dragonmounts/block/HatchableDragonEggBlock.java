package net.dragonmounts.block;

import net.dragonmounts.api.DragonTypified;
import net.dragonmounts.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.registry.DragonType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import static net.dragonmounts.DragonMounts.BLOCK_TRANSLATION_KEY_PREFIX;

public class HatchableDragonEggBlock extends DragonEggBlock implements DragonTypified {
    public static InteractionResult spawn(Level level, BlockPos pos, DragonType type) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
        var entity = new HatchableDragonEggEntity(level);
        entity.setDragonType(type, true);
        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        level.addFreshEntity(entity);
        return InteractionResult.CONSUME;
    }

    private static final String TRANSLATION_KEY = BLOCK_TRANSLATION_KEY_PREFIX + "dragon_egg";
    protected DragonType type;

    public HatchableDragonEggBlock(DragonType type, Properties props) {
        super(props);
        this.type = type;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.dimension().equals(Level.END)) {
            super.attack(state, level, pos, player);
        }
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (player.level().dimension().equals(Level.END)) return 0.0F;
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (level.dimension().equals(Level.END)) return super.useWithoutItem(state, level, pos, player, hit);
        return spawn(level, pos, this.type);
    }

    @Override
    public @NotNull String getDescriptionId() {
        return TRANSLATION_KEY;
    }

    @Override
    public DragonType getDragonType() {
        return this.type;
    }
}
