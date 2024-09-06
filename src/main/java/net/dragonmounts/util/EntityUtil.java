package net.dragonmounts.util;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class EntityUtil extends EntityType<Entity> {//to access protected methods

    /**
     * @see EntityType#create(ServerLevel, Consumer, BlockPos, MobSpawnType, boolean, boolean)
     */
    public static void finalizeSpawn(ServerLevel level, Entity entity, BlockPos pos, MobSpawnType reason, boolean yOffset, boolean extraOffset) {
        double offset, x = pos.getX() + 0.5D, y = pos.getY(), z = pos.getZ() + 0.5D;
        if (yOffset) {
            entity.setPos(x, y + 1.0D, z);
            offset = getYOffset(level, pos, extraOffset, entity.getBoundingBox());
        } else {
            offset = 0.0D;
        }
        entity.moveTo(x, y + offset, z, Mth.wrapDegrees(level.random.nextFloat() * 360.0F), 0.0F);
        if (entity instanceof Mob mob) {
            mob.yHeadRot = mob.getYRot();
            mob.yBodyRot = mob.getYRot();
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), reason, null);
            mob.playAmbientSound();
        }
    }

    /**
     * @see EntityType#appendCustomEntityStackConfig(Consumer, ServerLevel, ItemStack, Player)
     */
    public static void handleEntityData(Entity entity, ServerLevel level, Player player, CustomData data) {
        if (!data.isEmpty() && (!entity.onlyOpCanSetNbt() || player != null && level.getServer().getPlayerList().isOp(player.getGameProfile()))) {
            data.loadInto(entity);
        }
    }

    public static boolean addOrMergeEffect(LivingEntity entity, Holder<MobEffect> holder, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon) {
        if (duration < 0) {
            return entity.addEffect(new MobEffectInstance(holder, -1, amplifier, ambient, visible, showIcon, null));
        }
        var effect = entity.getEffect(holder);
        if (effect == null) {
            return entity.addEffect(new MobEffectInstance(holder, duration, amplifier, ambient, visible, showIcon, null));
        }
        if (effect.getAmplifier() < amplifier) {
            do {
                if (effect.isInfiniteDuration()) break;
                effect.duration += duration;
            } while ((effect = effect.hiddenEffect) != null);
            return entity.addEffect(new MobEffectInstance(holder, duration, amplifier, ambient, visible, showIcon, null));
        }
        if (effect.isInfiniteDuration()) return false;
        return entity.addEffect(new MobEffectInstance(holder, duration + effect.getDuration(), amplifier, ambient, visible, showIcon, null));
    }

    public static boolean addOrResetEffect(LivingEntity entity, Holder<MobEffect> holder, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon, int threshold) {
        var effect = entity.getEffect(holder);
        if (effect == null) {
            return entity.addEffect(new MobEffectInstance(holder, duration, amplifier, ambient, visible, showIcon, null));
        }
        while (effect.getAmplifier() > amplifier) {
            if (effect.isInfiniteDuration()) return false;
            if (effect.hiddenEffect == null) {
                return effect.getDuration() < threshold &&
                        entity.addEffect(new MobEffectInstance(holder, duration, amplifier, ambient, visible, showIcon, null));
            }
            effect = effect.hiddenEffect;
        }
        if (effect.getAmplifier() == amplifier) {
            return !effect.isInfiniteDuration() && effect.getDuration() < threshold &&
                    entity.addEffect(new MobEffectInstance(holder, duration, amplifier, ambient, visible, showIcon, null));
        }
        return entity.addEffect(new MobEffectInstance(holder, duration, amplifier, ambient, visible, showIcon, null));
    }

    public static ItemStack consumeStack(Player player, InteractionHand hand, ItemStack stack, ItemStack result) {
        stack.shrink(1);
        if (stack.isEmpty()) {
            player.setItemInHand(hand, result);
            return result;
        }
        if (!result.isEmpty() && !player.getInventory().add(result)) {//Inventory.getFreeSlot() won't check the offhand slot
            player.drop(result, false);
        }
        return stack;
    }

    public static CompoundTag saveWithId(Entity entity, CompoundTag tag) {
        tag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
        entity.saveWithoutId(tag);
        return tag;
    }

    /**
     * @return {@link EquipmentSlot#MAINHAND} by default.
     * @see LivingEntity#getSlotForHand
     */
    public static EquipmentSlot getSlotForHand(@Nullable InteractionHand hand) {
        return hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
    }

    private EntityUtil(EntityFactory<Entity> a, MobCategory b, boolean c, boolean d, boolean e, boolean f, ImmutableSet<Block> g, EntityDimensions h, float i, int j, int k, FeatureFlagSet l) {
        super(a, b, c, d, e, f, g, h, i, j, k, l);
    }
}
