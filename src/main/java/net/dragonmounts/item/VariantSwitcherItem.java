package net.dragonmounts.item;

import net.dragonmounts.block.AbstractDragonHeadBlock;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.init.DMSounds;
import net.dragonmounts.init.DragonVariants;
import net.dragonmounts.registry.DragonVariant;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.ROTATION_16;

public class VariantSwitcherItem extends Item {
    public static DragonVariant draw(RandomSource random, DragonVariant current) {
        return current.type.variants.draw(random, current, false);
    }

    public VariantSwitcherItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (entity instanceof TameableDragonEntity dragon) {
            var level = dragon.level();
            if (level.isClientSide) return InteractionResult.SUCCESS;
            if (dragon.isOwnedBy(player)) {
                level.playSound(player, dragon, DMSounds.VARIANT_SWITCHER, SoundSource.PLAYERS, 1.0F, 1.0F);
                dragon.setVariant(draw(dragon.getRandom(), dragon.getVariant()));
                stack.shrink(1);
                return InteractionResult.CONSUME;
            }
            player.displayClientMessage(Component.translatable("message.dragonmounts.not_owner"), true);
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var player = context.getPlayer();
        var old = level.getBlockState(pos);
        var block = old.getBlock();
        BlockState neo;
        if (block == Blocks.DRAGON_HEAD) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            neo = draw(level.random, DragonVariants.ENDER_FEMALE).headBlock.defaultBlockState().setValue(ROTATION_16, old.getValue(ROTATION_16));
        } else if (block == Blocks.DRAGON_WALL_HEAD) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            neo = draw(level.random, DragonVariants.ENDER_FEMALE).headWallBlock.defaultBlockState().setValue(HORIZONTAL_FACING, old.getValue(HORIZONTAL_FACING));
        } else if (block instanceof AbstractDragonHeadBlock head) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            neo = head.isOnWall
                    ? draw(level.random, head.variant).headWallBlock.defaultBlockState().setValue(HORIZONTAL_FACING, old.getValue(HORIZONTAL_FACING))
                    : draw(level.random, head.variant).headBlock.defaultBlockState().setValue(ROTATION_16, old.getValue(ROTATION_16));
        } else return InteractionResult.PASS;
        level.playSound(player, pos, DMSounds.VARIANT_SWITCHER, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.setBlock(pos, neo, 0b1011);
        context.getItemInHand().consume(1, player);
        return InteractionResult.CONSUME;
    }
}
