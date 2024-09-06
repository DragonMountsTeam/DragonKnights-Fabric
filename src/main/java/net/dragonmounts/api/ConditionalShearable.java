package net.dragonmounts.api;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface ConditionalShearable {
    default boolean readyForShearing(Level level, ItemStack stack) {
        return true;
    }

    boolean shear(Level level, @Nullable Player player, ItemStack stack, BlockPos pos, SoundSource source);
}
