package net.dragonmounts.item;

import com.mojang.serialization.MapCodec;
import net.dragonmounts.api.ScoreboardAccessor;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.init.DMDataComponents;
import net.dragonmounts.init.DMItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;
import static net.dragonmounts.util.EntityUtil.*;
import static net.dragonmounts.util.ScoreboardInfo.applyScores;
import static net.minecraft.world.InteractionResultHolder.*;

/**
 * @see net.minecraft.world.item.SpawnEggItem
 */
public class AmuletItem<T extends Entity> extends Item implements IEntityContainer<T> {
    public static final MapCodec<EntityType<?>> ENTITY_TYPE_FIELD_CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("id");
    private static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_amulet";
    public final Class<T> contentType;

    public AmuletItem(Class<T> contentType, Properties props) {
        super(props.stacksTo(1));
        this.contentType = contentType;
    }

    @Override
    public @Nullable Entity loadEntity(
            ServerLevel level,
            ItemStack stack,
            @Nullable Player player,
            BlockPos pos,
            MobSpawnType reason,
            boolean yOffset,
            boolean extraOffset
    ) {
        var data = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        var type = data.read(ENTITY_TYPE_FIELD_CODEC).result().orElse(null);
        if (type == null) return null;
        var entity = type.create(level, null, pos, reason, yOffset, extraOffset);
        if (entity == null) return null;
        handleEntityData(entity, level, player, data);
        applyScores(level.getScoreboard(), stack, entity);
        return entity;
    }

    @Override
    public final Class<T> getContentType() {
        return this.contentType;
    }

    @Override
    public ItemStack saveEntity(T entity, DataComponentPatch patch) {
        var type = entity.getType();
        if (type.canSerialize()) {
            var stack = new ItemStack(this);
            stack.set(DataComponents.ENTITY_DATA, IEntityContainer.simplifyData(saveWithId(entity, new CompoundTag())));
            stack.set(DMDataComponents.SCORES, ((ScoreboardAccessor) entity.level().getScoreboard()).dragonmounts$getInfo(entity));
            stack.applyComponents(patch);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof TameableDragonEntity dragon)) return InteractionResult.PASS;
        if (!(dragon.level() instanceof ServerLevel world)) return InteractionResult.SUCCESS;
        if (dragon.isOwnedBy(player)) {
            var amulet = dragon.getDragonType().getInstance(DragonAmuletItem.class, null);
            if (amulet == null) return InteractionResult.FAIL;
            if (!this.isEmpty(stack)) {
                var pos = dragon.blockPosition();
                var entity = this.loadEntity(world, stack, player, pos, MobSpawnType.BUCKET, false, false);
                if (entity != null) {
                    world.addFreshEntityWithPassengers(entity);
                    world.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
                }
            }
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
    public InteractionResult useOn(UseOnContext context) {
        var stack = context.getItemInHand();
        if (this.isEmpty(stack)) return InteractionResult.PASS;
        if (context.getLevel() instanceof ServerLevel level) {
            var pos = context.getClickedPos();
            var direction = context.getClickedFace();
            var spawnPos = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() ? pos : pos.relative(direction);
            var player = context.getPlayer();
            var entity = this.loadEntity(
                    level,
                    stack,
                    player,
                    spawnPos,
                    MobSpawnType.BUCKET,
                    true,
                    !Objects.equals(pos, spawnPos) && direction == Direction.UP
            );
            if (entity != null) {
                level.addFreshEntityWithPassengers(entity);
                level.gameEvent(player, GameEvent.ENTITY_PLACE, spawnPos);
                if (player != null) {
                    consumeStack(player, context.getHand(), stack, new ItemStack(DMItems.AMULET));
                }
                // stat will be awarded at `ItemStack#useOn`
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (this.isEmpty(stack)) return pass(stack);
        var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != BlockHitResult.Type.BLOCK) return pass(stack);
        if (!(level instanceof ServerLevel world)) return success(stack);
        var pos = hit.getBlockPos();
        if (!(world.getBlockState(pos).getBlock() instanceof LiquidBlock)) return pass(stack);
        if (world.mayInteract(player, pos) && player.mayUseItemAt(pos, hit.getDirection(), stack)) {
            var entity = this.loadEntity(world, stack, player, pos, MobSpawnType.BUCKET, false, false);
            if (entity == null) return pass(stack);
            world.addFreshEntityWithPassengers(entity);
            world.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
            player.awardStat(Stats.ITEM_USED.get(this));
            return success(consumeStack(player, hand, stack, new ItemStack(DMItems.AMULET)));
        }
        return fail(stack);
    }

    @Override
    public String getDescriptionId() {
        return TRANSLATION_KEY;
    }

    @Override
    public void onDestroyed(ItemEntity item) {
        var stack = item.getItem();
        if (this.isEmpty(stack)) return;
        var level = (ServerLevel) item.level();
        var entity = this.loadEntity(level, stack, null, item.getOnPos(), MobSpawnType.BUCKET, true, false);
        if (entity != null) {
            level.addFreshEntityWithPassengers(entity);
        }
    }
}
