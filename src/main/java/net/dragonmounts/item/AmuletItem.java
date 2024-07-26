package net.dragonmounts.item;

import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;
import static net.dragonmounts.util.EntityUtil.consume;
import static net.dragonmounts.util.EntityUtil.finalizeSpawn;


public class AmuletItem<T extends Entity> extends Item implements IEntityContainer<T> {
    private static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_amulet";

    public AmuletItem(Settings props) {
        super(props.maxCount(1));
    }

    @Nullable
    @Override
    public Entity loadEntity(
            ServerWorld level,
            @Nullable PlayerEntity player,
            NbtCompound tag,
            BlockPos pos,
            SpawnReason reason,
            @Nullable EntityData data,
            boolean yOffset,
            boolean extraOffset
    ) {
        NbtCompound info = tag.getCompound("EntityTag");
        if (info.contains("id", 8)) {
            Entity entity = Registry.ENTITY_TYPE.get(Identifier.tryParse(info.getString("id"))).create(level);
            if (entity != null) {
                finalizeSpawn(level, entity, pos, reason, data, tag, yOffset, extraOffset);
                if (this.canSetNbt(level.getServer(), entity, player)) {
                    UUID uuid = entity.getUuid();
                    entity.readNbt(entity.writeNbt(new NbtCompound()).copyFrom(info));
                    entity.setUuid(uuid);
                }
                return entity;
            }
        }
        return null;
    }

    @Override
    public ItemStack saveEntity(T entity) {
        ItemStack stack = new ItemStack(this);
        NbtCompound tag = new NbtCompound();
        entity.removeAllPassengers();
        entity.saveSelfNbt(tag);
        stack.setTag(IEntityContainer.simplifyData(tag));
        return stack;
    }

    @Override
    public boolean isEmpty(@Nullable NbtCompound tag) {
        return tag == null || !tag.contains("EntityTag", 10);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        if (target instanceof TameableDragonEntity) {
            World level = target.world;
            if (level.isClient) return ActionResult.SUCCESS;
            TameableDragonEntity dragon = (TameableDragonEntity) target;
            if (dragon.isOwner(player)) {
                DragonAmuletItem amulet = dragon.getDragonType().getInstance(DragonAmuletItem.class, null);
                if (amulet == null) return ActionResult.FAIL;
                NbtCompound tag = stack.getTag();
                if (tag != null && !this.isEmpty(tag)) {
                    Entity entity = this.loadEntity(
                            (ServerWorld) level,
                            player,
                            tag,
                            target.getBlockPos(),
                            SpawnReason.BUCKET,
                            null,
                            false,
                            false
                    );
                    if (entity != null) level.spawnEntity(entity);
                }
                dragon.inventory.dropContents(true, 0);
                consume(player, hand, stack, amulet.saveEntity(dragon));
                player.incrementStat(Stats.USED.getOrCreateStat(this));
                dragon.remove();
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
        ItemStack stack = context.getStack();
        NbtCompound tag = stack.getTag();
        if (tag == null || this.isEmpty(tag)) return ActionResult.PASS;
        World world = context.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;
        ServerWorld level = (ServerWorld) world;
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getBlockPos();
        Direction direction = context.getSide();
        BlockPos spawnPos = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() ? pos : pos.offset(direction);
        Entity entity = this.loadEntity(level,
                player,
                tag,
                spawnPos,
                SpawnReason.BUCKET,
                null,
                true,
                !Objects.equals(pos, spawnPos) && direction == Direction.UP
        );
        if (entity != null) {
            level.spawnEntity(entity);
            if (player != null) {
                consume(player, context.getHand(), stack, new ItemStack(this));
                player.incrementStat(Stats.USED.getOrCreateStat(this));
            }
        }
        return ActionResult.CONSUME;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        NbtCompound tag = stack.getTag();
        if (tag == null || this.isEmpty(tag)) return TypedActionResult.pass(stack);
        BlockHitResult result = raycast(level, player, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (result.getType() != BlockHitResult.Type.BLOCK) return TypedActionResult.pass(stack);
        if (level.isClient) return TypedActionResult.success(stack);
        BlockPos pos = result.getBlockPos();
        if (!(level.getBlockState(pos).getBlock() instanceof FluidBlock)) return TypedActionResult.pass(stack);
        if (level.canPlayerModifyAt(player, pos) && player.canPlaceOn(pos, result.getSide(), stack)) {
            Entity entity = this.loadEntity((ServerWorld) level, player, tag, pos, SpawnReason.BUCKET, null, false, false);
            if (entity == null) return TypedActionResult.pass(stack);
            level.spawnEntity(entity);
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.success(consume(player, hand, stack, new ItemStack(this)));
        }
        return TypedActionResult.fail(stack);
    }

    @Override
    public String getTranslationKey() {
        return TRANSLATION_KEY;
    }
}
