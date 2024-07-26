package net.dragonmounts.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.dragonmounts.init.DragonTypes;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.dragonmounts.block.HatchableDragonEggBlock.spawn;
import static net.dragonmounts.init.DMGameRules.IS_EGG_OVERRIDDEN;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin extends State<Block, BlockState> {
    private AbstractBlockStateMixin(Block owner, ImmutableMap<Property<?>, Comparable<?>> entries, MapCodec<BlockState> codec) {
        super(owner, entries, codec);
    }

    @Shadow
    public abstract Block getBlock();

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    public void tryHatchDragonEgg(World world, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> info) {
        if (!world.isClient && this.getBlock() == Blocks.DRAGON_EGG && !world.getRegistryKey().equals(World.END) && world.getGameRules().get(IS_EGG_OVERRIDDEN).get())
            info.setReturnValue(spawn(world, hit.getBlockPos(), DragonTypes.ENDER));
    }
}
