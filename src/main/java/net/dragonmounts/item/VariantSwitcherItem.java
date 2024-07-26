package net.dragonmounts.item;

import net.dragonmounts.block.AbstractDragonHeadBlock;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.init.DragonVariants;
import net.dragonmounts.registry.DragonVariant;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.stat.Stats;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.minecraft.state.property.Properties.HORIZONTAL_FACING;
import static net.minecraft.state.property.Properties.ROTATION;


public class VariantSwitcherItem extends Item {
    private static DragonVariant draw(DragonVariant variant) {
        return variant.type.variants.draw(RANDOM, variant);
    }

    public VariantSwitcherItem(Settings props) {
        super(props);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (entity instanceof TameableDragonEntity) {
            if (player.world.isClient) return ActionResult.SUCCESS;
            TameableDragonEntity dragon = (TameableDragonEntity) entity;
            if (dragon.isOwner(player)) {
                dragon.setVariant(draw(dragon.getVariant()));
                if (!player.abilities.creativeMode) {
                    stack.decrement(1);
                }
                player.incrementStat(Stats.USED.getOrCreateStat(this));
                return ActionResult.CONSUME;
            } else {
                player.sendMessage(new TranslatableText("message.dragonmounts.not_owner"), true);
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World level = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getBlockPos();
        BlockState old = level.getBlockState(pos);
        Block block = old.getBlock();
        BlockState neo;
        if (block == Blocks.DRAGON_HEAD) {
            if (level.isClient) return ActionResult.SUCCESS;
            neo = draw(DragonVariants.ENDER_FEMALE).headBlock.getDefaultState().with(ROTATION, old.get(ROTATION));
        } else if (block == Blocks.DRAGON_WALL_HEAD) {
            if (level.isClient) return ActionResult.SUCCESS;
            neo = draw(DragonVariants.ENDER_FEMALE).headWallBlock.getDefaultState().with(HORIZONTAL_FACING, old.get(HORIZONTAL_FACING));
        } else if (block instanceof AbstractDragonHeadBlock) {
            if (level.isClient) return ActionResult.SUCCESS;
            AbstractDragonHeadBlock head = (AbstractDragonHeadBlock) block;
            neo = head.isOnWall
                    ? draw(head.variant).headWallBlock.getDefaultState().with(HORIZONTAL_FACING, old.get(HORIZONTAL_FACING))
                    : draw(head.variant).headBlock.getDefaultState().with(ROTATION, old.get(ROTATION));
        } else return ActionResult.PASS;
        level.setBlockState(pos, neo, 0b1011);
        if (player != null) {
            if (!player.abilities.creativeMode) {
                context.getStack().decrement(1);
            }
            player.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        return ActionResult.CONSUME;
    }
}
