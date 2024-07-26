package net.dragonmounts.item;

import net.dragonmounts.api.IDragonTypified;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.init.DMEntities;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.registry.DragonVariant;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.MobSpawnerEntry;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;

public final class DragonSpawnEggItem extends SpawnEggItem implements IDragonTypified {
    private static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_spawn_egg";
    public final DragonType type;

    public DragonSpawnEggItem(DragonType type, int background, int highlight, Settings settings) {
        super(DMEntities.TAMEABLE_DRAGON, background, highlight, settings);
        this.type = type;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;
        ItemStack stack = context.getStack();
        BlockPos pos = context.getBlockPos();
        Direction direction = context.getSide();
        BlockState state = world.getBlockState(pos);
        EntityType<?> $type = this.getEntityType(stack.getTag());
        if (state.isOf(Blocks.SPAWNER)) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof MobSpawnerBlockEntity) {
                MobSpawnerLogic spawner = ((MobSpawnerBlockEntity) entity).getLogic();
                if ($type == DMEntities.TAMEABLE_DRAGON) {
                    NbtCompound tag = new NbtCompound();
                    tag.putString("id", DMEntities.TAMEABLE_DRAGON_ID.toString());
                    tag.putString(DragonVariant.DATA_PARAMETER_KEY, this.type.variants.draw(RANDOM, null).identifier.toString());
                    MobSpawnerEntry spawnerEntity = new MobSpawnerEntry(1, tag);
                    spawner.setSpawnEntry(spawnerEntity);
                } else {
                    spawner.setEntityId($type);
                }
                entity.markDirty();
                world.updateListeners(pos, state, state, 3);
                stack.decrement(1);
                return ActionResult.CONSUME;
            }
        }
        BlockPos spawnPos = state.getCollisionShape(world, pos).isEmpty() ? pos : pos.offset(direction);
        Entity entity = $type.spawnFromItemStack((ServerWorld) world, stack, context.getPlayer(), spawnPos, SpawnReason.SPAWN_EGG, true, !Objects.equals(pos, spawnPos) && direction == Direction.UP);
        if (entity instanceof TameableDragonEntity) ((TameableDragonEntity) entity).setDragonType(this.type, true);
        stack.decrement(1);
        return ActionResult.CONSUME;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        BlockHitResult result = raycast(world, player, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (result.getType() != BlockHitResult.Type.BLOCK) return TypedActionResult.pass(stack);
        if (!world.isClient) {
            BlockPos pos = result.getBlockPos();
            if (!(world.getBlockState(pos).getBlock() instanceof FluidBlock)) return TypedActionResult.pass(stack);
            else if (world.canPlayerModifyAt(player, pos) && player.canPlaceOn(pos, result.getSide(), stack)) {
                Entity entity = this.getEntityType(stack.getTag()).spawnFromItemStack((ServerWorld) world, stack, player, pos, SpawnReason.SPAWN_EGG, false, false);
                if (entity == null) return TypedActionResult.pass(stack);
                if (entity instanceof TameableDragonEntity) {
                    ((TameableDragonEntity) entity).setDragonType(this.type, true);
                }
                if (!player.abilities.creativeMode) {
                    stack.decrement(1);
                }
                player.incrementStat(Stats.USED.getOrCreateStat(this));
                return TypedActionResult.consume(stack);
            }
            return TypedActionResult.fail(stack);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> tooltips, TooltipContext flag) {
        tooltips.add(this.type.getName());
    }

    @Override
    public String getTranslationKey() {
        return TRANSLATION_KEY;
    }

    @Override
    public DragonType getDragonType() {
        return this.type;
    }
}
