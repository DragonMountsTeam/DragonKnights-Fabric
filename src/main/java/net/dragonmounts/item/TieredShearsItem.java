package net.dragonmounts.item;

import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.util.ShearsDispenseItemBehaviorEx;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import static net.dragonmounts.util.EntityUtil.getSlotForHand;

public class TieredShearsItem extends ShearsItem {
    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new ShearsDispenseItemBehaviorEx();

    protected final Tier tier;
    private final float speedFactor;

    public TieredShearsItem(Tier tier, Properties props) {
        super(props.durability((int) (tier.getUses() * 0.952F)));
        this.tier = tier;
        this.speedFactor = tier.getSpeed() / Tiers.IRON.getSpeed();
    }

    public Tier getTier() {
        return this.tier;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        var level = entity.level();
        if (level.isClientSide) return super.interactLivingEntity(stack, player, entity, hand);
        if (entity instanceof TameableDragonEntity dragon) {
            var pos = dragon.blockPosition();
            if (dragon.isOwnedBy(player)) {
                if (dragon.readyForShearing(level, stack) && dragon.shear(level, player, stack, pos, SoundSource.PLAYERS)) {
                    stack.hurtAndBreak(20, dragon, getSlotForHand(hand));
                    return InteractionResult.SUCCESS;
                }
            } else {
                player.displayClientMessage(Component.translatable("message.dragonmounts.not_owner"), true);
            }
            return InteractionResult.FAIL;
        }
        return super.interactLivingEntity(stack, player, entity, hand);
    }

    @Override
    public int getEnchantmentValue() {
        return this.tier.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack candidate) {
        return this.tier.getRepairIngredient().test(candidate) || super.isValidRepairItem(stack, candidate);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float speed = super.getDestroySpeed(stack, state);
        return speed > 1.0F ? speed * this.speedFactor : speed;
    }
}
