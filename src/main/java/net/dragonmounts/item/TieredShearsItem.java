package net.dragonmounts.item;

import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * @see net.minecraft.item.ToolItem
 */
public class TieredShearsItem extends ShearsItem {
    protected final ToolMaterial tier;
    private final float speedFactor;

    public TieredShearsItem(ToolMaterial tier, Settings settings) {
        super(settings.maxDamageIfAbsent((int) (tier.getDurability() * 0.952F)));
        this.tier = tier;
        this.speedFactor = tier.getMiningSpeedMultiplier() / ToolMaterials.IRON.getMiningSpeedMultiplier();
    }

    public ToolMaterial getTier() {
        return this.tier;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        World level = entity.world;
        if (level.isClient) return super.useOnEntity(stack, player, entity, hand);
        if (entity instanceof TameableDragonEntity) {
            TameableDragonEntity dragon = (TameableDragonEntity) entity;
            BlockPos pos = dragon.getBlockPos();
            if (dragon.isShearable(stack, dragon.world, pos)) {
                if (dragon.isOwner(player)) {
                    Random random = dragon.getRandom();
                    boolean flag = false;
                    for (ItemStack drop : dragon.onSheared(player, stack, level, pos, EnchantmentHelper.getLevel(Enchantments.FORTUNE, stack))) {
                        ItemEntity item = dragon.dropStack(drop, 1.0F);
                        if (item != null) {
                            flag = true;
                            item.setVelocity(item.getVelocity().add((random.nextFloat() - random.nextFloat()) * 0.1D, random.nextFloat() * 0.05D, (random.nextFloat() - random.nextFloat()) * 0.1D));
                        }
                    }
                    if (flag) {
                        stack.damage(20, dragon, e -> e.sendToolBreakStatus(hand));
                        return ActionResult.SUCCESS;
                    }
                } else {
                    player.sendMessage(new TranslatableText("message.dragonmounts.not_owner"), true);
                }
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        }
        return super.useOnEntity(stack, player, entity, hand);
    }

    @Override
    public int getEnchantability() {
        return this.tier.getEnchantability();
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return this.tier.getRepairIngredient().test(ingredient) || super.canRepair(stack, ingredient);
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        float speed = super.getMiningSpeedMultiplier(stack, state);
        return speed > 1.0F ? speed * this.speedFactor : speed;
    }
}
