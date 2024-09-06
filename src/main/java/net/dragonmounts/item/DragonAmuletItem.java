package net.dragonmounts.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.dragonmounts.api.DragonTypified;
import net.dragonmounts.api.ScoreboardAccessor;
import net.dragonmounts.entity.dragon.ServerDragonEntity;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.init.DMDataComponents;
import net.dragonmounts.init.DMItems;
import net.dragonmounts.registry.DragonType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static net.dragonmounts.entity.dragon.TameableDragonEntity.FLYING_DATA_PARAMETER_KEY;
import static net.dragonmounts.util.EntityUtil.*;
import static net.dragonmounts.util.ScoreboardInfo.applyScores;
import static net.minecraft.world.InteractionResultHolder.*;

public class DragonAmuletItem extends AmuletItem<TameableDragonEntity> implements DragonTypified {
    public static final MapCodec<Component> NAME_CODEC = ComponentSerialization.CODEC.fieldOf("CustomName");
    public static final MapCodec<Float> HEALTH_CODEC = Codec.FLOAT.fieldOf("Health");
    public final DragonType type;

    public DragonAmuletItem(DragonType type, Properties props) {
        super(TameableDragonEntity.class, props.component(DMDataComponents.DRAGON_TYPE, type));
        this.type = type;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof TameableDragonEntity dragon)) return InteractionResult.PASS;
        if (!(dragon.level() instanceof ServerLevel world)) return InteractionResult.SUCCESS;
        if (dragon.isOwnedBy(player)) {
            var amulet = dragon.getDragonType().getInstance(DragonAmuletItem.class, null);
            if (amulet == null) return InteractionResult.FAIL;
            var pos = dragon.blockPosition();
            world.addFreshEntityWithPassengers(this.loadEntity(world, stack, player, pos, MobSpawnType.BUCKET, false, false));
            world.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
            dragon.inventory.dropContents(true, 0);
            dragon.ejectPassengers();
            consumeStack(player, hand, stack, amulet.saveEntity(dragon, DataComponentPatch.EMPTY));
            player.awardStat(Stats.ITEM_USED.get(this));
            dragon.discard();
            return InteractionResult.CONSUME;
        }
        player.displayClientMessage(Component.translatable("message.dragonmounts.not_owner"), true);
        return InteractionResult.FAIL;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getLevel() instanceof ServerLevel level) {
            var stack = context.getItemInHand();
            var pos = context.getClickedPos();
            var direction = context.getClickedFace();
            var spawnPos = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() ? pos : pos.relative(direction);
            var player = context.getPlayer();
            level.addFreshEntityWithPassengers(this.loadEntity(
                    level,
                    stack,
                    player,
                    spawnPos,
                    MobSpawnType.BUCKET,
                    true,
                    !Objects.equals(pos, spawnPos) && direction == Direction.UP
            ));
            level.gameEvent(player, GameEvent.ENTITY_PLACE, spawnPos);
            if (player != null) {
                consumeStack(player, context.getHand(), stack, new ItemStack(DMItems.AMULET));
            }
            // stat will be awarded at `ItemStack#useOn`
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != BlockHitResult.Type.BLOCK) return pass(stack);
        if (!(level instanceof ServerLevel world)) return success(stack);
        var pos = hit.getBlockPos();
        if (!(world.getBlockState(pos).getBlock() instanceof LiquidBlock)) return pass(stack);
        if (world.mayInteract(player, pos) && player.mayUseItemAt(pos, hit.getDirection(), stack)) {
            world.addFreshEntityWithPassengers(this.loadEntity(world, stack, player, pos, MobSpawnType.BUCKET, false, false));
            world.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
            player.awardStat(Stats.ITEM_USED.get(this));
            return success(consumeStack(player, hand, stack, new ItemStack(DMItems.AMULET)));
        }
        return fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag flag) {
        var data = stack.get(DataComponents.ENTITY_DATA);
        if (data == null) {
            tooltips.add(Component.translatable("tooltip.dragonmounts.missing").withStyle(ChatFormatting.RED));
        } else {
            tooltips.add(Component.translatable("tooltip.dragonmounts.type", this.type.getName()).withStyle(ChatFormatting.GRAY));
            data.read(HEALTH_CODEC).ifSuccess(health -> tooltips.add(
                    Component.translatable("tooltip.dragonmounts.health",
                            Component.literal(Float.toString(health)).withStyle(ChatFormatting.GREEN)
                    ).withStyle(ChatFormatting.GRAY))
            );
            data.read(NAME_CODEC).ifSuccess(name -> tooltips.add(
                    Component.translatable("tooltip.dragonmounts.custom_name", name).withStyle(ChatFormatting.GRAY))
            );
            var player = stack.get(DMDataComponents.PLAYER_NAME);
            if (player != null) {
                tooltips.add(Component.translatable("tooltip.dragonmounts.owner_name", player).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public @NotNull ItemStack saveEntity(TameableDragonEntity entity, DataComponentPatch patch) {
        var level = entity.level();
        var stack = new ItemStack(this);
        var tag = saveWithId(entity, new CompoundTag());
        tag.remove(FLYING_DATA_PARAMETER_KEY);
        tag.remove("UUID");
        stack.set(DataComponents.ENTITY_DATA, IEntityContainer.simplifyData(tag));
        LivingEntity owner = entity.getOwner();
        if (owner != null) {
            stack.set(DMDataComponents.PLAYER_NAME, owner.getDisplayName());
        }
        stack.set(DMDataComponents.SCORES, ((ScoreboardAccessor) level.getScoreboard()).dragonmounts$getInfo(entity));
        stack.applyComponents(patch);
        return stack;
    }

    @Override
    public @NotNull ServerDragonEntity loadEntity(
            ServerLevel level,
            ItemStack stack,
            @Nullable Player player,
            BlockPos pos,
            MobSpawnType reason,
            boolean yOffset,
            boolean extraOffset
    ) {
        ServerDragonEntity dragon = new ServerDragonEntity(level);
        CustomData data = stack.get(DataComponents.ENTITY_DATA);
        finalizeSpawn(level, dragon, pos, reason, yOffset, extraOffset);
        if (data != null) {
            handleEntityData(dragon, level, player, data);
            dragon.setDragonType(this.type, false);
        } else {
            dragon.setDragonType(this.type, true);
        }
        applyScores(level.getScoreboard(), stack, dragon);
        return dragon;
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        return false;
    }

    @Override
    public DragonType getDragonType() {
        return this.type;
    }
}
