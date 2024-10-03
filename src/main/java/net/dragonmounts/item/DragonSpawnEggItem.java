package net.dragonmounts.item;

import net.dragonmounts.api.DragonTypified;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.init.DMDataComponents;
import net.dragonmounts.init.DMEntities;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.registry.DragonVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;
import static net.dragonmounts.util.EntityUtil.handleEntityData;
import static net.dragonmounts.util.EntityUtil.saveWithId;
import static net.dragonmounts.util.ScoreboardInfo.applyScores;
import static net.minecraft.world.InteractionResultHolder.*;

public class DragonSpawnEggItem extends SpawnEggItem implements IEntityContainer<Entity>, DragonTypified {
    private static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_spawn_egg";
    public final TranslatableContents name;
    public final DragonType type;

    public DragonSpawnEggItem(EntityType<? extends TameableDragonEntity> defaultType, DragonType dragonType, int background, int highlight, Properties props) {
        super(defaultType, FastColor.ARGB32.opaque(background), FastColor.ARGB32.opaque(highlight), props.component(DMDataComponents.DRAGON_TYPE, dragonType));
        this.name = new TranslatableContents(TRANSLATION_KEY + ".name", null, new Object[]{MutableComponent.create(dragonType.name)});
        this.type = dragonType;
    }

    public DragonSpawnEggItem(DragonType type, int background, int highlight, Properties props) {
        this(DMEntities.TAMEABLE_DRAGON, type, background, highlight, props.component(DMDataComponents.DRAGON_TYPE, type));
    }

    protected void putDragonData(SpawnData data, EntityType<?> type, RandomSource random) {
        var tag = data.entityToSpawn();
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        if (id.equals(tag.getString("id"))) {
            tag.putString(DragonVariant.DATA_PARAMETER_KEY, DragonVariant.draw(this.type, random, tag.getString(DragonVariant.DATA_PARAMETER_KEY)).identifier.toString());
        } else {
            tag.putString("id", id);
            tag.putString(DragonVariant.DATA_PARAMETER_KEY, DragonVariant.draw(this.type, random).identifier.toString());
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        var stack = context.getItemInHand();
        var pos = context.getClickedPos();
        var direction = context.getClickedFace();
        var state = level.getBlockState(pos);
        var random = level.getRandom();
        EntityType<?> type;
        switch (level.getBlockEntity(pos)) {
            case TrialSpawnerBlockEntity spawner:
                type = this.getType(stack);
                if (DMEntities.TAMEABLE_DRAGON == type) {
                    var impl = spawner.getTrialSpawner();
                    this.putDragonData(impl.getData().getOrCreateNextSpawnData(impl, random), type, random);
                } else {
                    spawner.setEntityId(type, random);
                }
                break;
            case SpawnerBlockEntity spawner:
                type = this.getType(stack);
                if (DMEntities.TAMEABLE_DRAGON == type) {
                    this.putDragonData(spawner.getSpawner().getOrCreateNextSpawnData(level, random, pos), type, random);
                } else {
                    spawner.setEntityId(type, random);
                }
                break;
            case Spawner spawner:
                spawner.setEntityId(this.getType(stack), random);
                break;
            case null:
            default:
                var spawnPos = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() ? pos : pos.relative(direction);
                var player = context.getPlayer();
                var entity = this.loadEntity(level, stack, player, spawnPos, MobSpawnType.SPAWN_EGG, true, !Objects.equals(pos, spawnPos) && direction == Direction.UP);
                if (entity != null) {
                    if (entity instanceof TameableDragonEntity dragon) {
                        dragon.setDragonType(this.type);
                    }
                    level.addFreshEntityWithPassengers(entity);
                    level.gameEvent(player, GameEvent.ENTITY_PLACE, spawnPos);
                    stack.shrink(1);
                    // stat will be awarded at `ItemStack#useOn`
                }
                return InteractionResult.CONSUME;
        }
        level.sendBlockUpdated(pos, state, state, 3);
        level.gameEvent(context.getPlayer(), GameEvent.BLOCK_CHANGE, pos);
        stack.shrink(1);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != BlockHitResult.Type.BLOCK) return pass(stack);
        if (!(level instanceof ServerLevel world)) return success(stack);
        var pos = hit.getBlockPos();
        if (!(world.getBlockState(pos).getBlock() instanceof LiquidBlock)) return pass(stack);
        if (world.mayInteract(player, pos) && player.mayUseItemAt(pos, hit.getDirection(), stack)) {
            var entity = this.loadEntity(world, stack, player, pos, MobSpawnType.SPAWN_EGG, false, false);
            if (entity == null) return pass(stack);
            if (entity instanceof TameableDragonEntity dragon) {
                dragon.setDragonType(this.type);
            }
            world.addFreshEntityWithPassengers(entity);
            stack.consume(1, player);
            player.awardStat(Stats.ITEM_USED.get(this));
            world.gameEvent(player, GameEvent.ENTITY_PLACE, entity.position());
            return consume(stack);
        }
        return fail(stack);
    }

    @Override
    public Optional<Mob> spawnOffspringFromSpawnEgg(Player player, Mob mob, EntityType<? extends Mob> type, ServerLevel level, Vec3 pos, ItemStack stack) {
        if (!this.spawnsEntity(stack, type)) {
            return Optional.empty();
        }
        Mob neo = mob instanceof AgeableMob old ? old.getBreedOffspring(level, old) : type.create(level);
        if (neo == null) {
            return Optional.empty();
        }
        neo.setBaby(true);
        if (!neo.isBaby()) return Optional.empty();
        neo.moveTo(pos.x(), pos.y(), pos.z(), 0.0F, 0.0F);
        level.addFreshEntityWithPassengers(neo);
        neo.setCustomName(stack.get(DataComponents.CUSTOM_NAME));
        applyScores(level.getScoreboard(), stack, neo);
        stack.consume(1, player);
        return Optional.of(neo);
    }

    @Override
    public String getDescriptionId() {
        return TRANSLATION_KEY;
    }

    public Component getName(ItemStack stack) {
        return MutableComponent.create(this.name);
    }

    @Override
    public final DragonType getDragonType() {
        return this.type;
    }

    public ItemStack saveEntity(TameableDragonEntity dragon) {
        return IEntityContainer.saveEntityData(this, saveWithId(dragon, new CompoundTag()), DataComponentPatch.EMPTY);
    }

    @Override
    public ItemStack saveEntity(Entity entity, DataComponentPatch patch) {
        if (entity instanceof TameableDragonEntity) {
            return IEntityContainer.saveEntityData(this, saveWithId(entity, new CompoundTag()), patch);
        }
        var item = SpawnEggItem.byId(entity.getType());
        return item == null ? ItemStack.EMPTY : IEntityContainer.saveEntityData(item, saveWithId(entity, new CompoundTag()), patch);
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
        var type = this.getType(stack);
        var entity = type.create(level, null, pos, reason, yOffset, extraOffset);
        if (entity == null) return null;
        handleEntityData(entity, level, player, stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY));
        entity.setCustomName(stack.get(DataComponents.CUSTOM_NAME));
        applyScores(level.getScoreboard(), stack, entity);
        return entity;
    }

    @Override
    public final Class<Entity> getContentType() {
        return Entity.class;
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        return true;
    }
}
