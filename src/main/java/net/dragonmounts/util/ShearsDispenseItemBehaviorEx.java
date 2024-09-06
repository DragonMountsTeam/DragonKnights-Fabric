package net.dragonmounts.util;

import net.dragonmounts.api.ConditionalShearable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.function.Consumers;
import org.jetbrains.annotations.NotNull;

public class ShearsDispenseItemBehaviorEx extends ShearsDispenseItemBehavior {
    protected @NotNull ItemStack execute(BlockSource block, ItemStack stack) {
        var level = block.level();
        if (!level.isClientSide()) {
            var pos = block.pos().relative(block.state().getValue(DispenserBlock.FACING));
            this.setSuccess(tryShearBeehive(level, pos) || tryShearLivingEntity(level, pos, stack));
            if (this.isSuccess()) {
                stack.hurtAndBreak(1, level, null, Consumers.nop());
            }
        }
        return stack;
    }

    public static boolean tryShearBeehive(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (state.is(BlockTags.BEEHIVES) && state.hasProperty(BeehiveBlock.HONEY_LEVEL) && state.getBlock() instanceof BeehiveBlock hive) {
            if (state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
                level.playSound(null, pos, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                BeehiveBlock.dropHoneycomb(level, pos);
                hive.releaseBeesAndResetHoneyLevel(level, state, pos, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
                level.gameEvent(null, GameEvent.SHEAR, pos);
                return true;
            }
        }
        return false;
    }

    public static boolean tryShearLivingEntity(ServerLevel level, BlockPos pos, ItemStack stack) {
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos), EntitySelector.NO_SPECTATORS)) {
            switch (entity) {
                case ConditionalShearable shearable:
                    if (shearable.readyForShearing(level, stack) && shearable.shear(level, null, stack, pos, SoundSource.BLOCKS)) {
                        level.gameEvent(null, GameEvent.SHEAR, pos);
                        return true;
                    }
                    continue;
                case Shearable shearable:
                    if (shearable.readyForShearing()) {
                        shearable.shear(SoundSource.BLOCKS);
                        level.gameEvent(null, GameEvent.SHEAR, pos);
                        return true;
                    }
                    continue;
                default:// continue;
            }
        }
        return false;
    }
}
