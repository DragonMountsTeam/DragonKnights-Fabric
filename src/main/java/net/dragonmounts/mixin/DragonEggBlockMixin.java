package net.dragonmounts.mixin;

import net.dragonmounts.init.DragonTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.dragonmounts.block.HatchableDragonEggBlock.spawn;
import static net.dragonmounts.init.DMGameRules.IS_EGG_OVERRIDDEN;

@Mixin(DragonEggBlock.class)
public abstract class DragonEggBlockMixin extends FallingBlock {
    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    public void tryHatchDragonEgg(BlockState state, Level level, BlockPos pos, Player d, BlockHitResult e, CallbackInfoReturnable<InteractionResult> info) {
        if (this == Blocks.DRAGON_EGG && !level.isClientSide && !level.dimension().equals(Level.END) && level.getGameRules().getBoolean(IS_EGG_OVERRIDDEN)) {
            info.setReturnValue(spawn(level, pos, DragonTypes.ENDER));
        }
    }

    private DragonEggBlockMixin(Properties props) {super(props);}
}
