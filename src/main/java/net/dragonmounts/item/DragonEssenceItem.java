package net.dragonmounts.item;

import net.dragonmounts.api.IDragonTypified;
import net.dragonmounts.entity.dragon.DragonLifeStage;
import net.dragonmounts.entity.dragon.ServerDragonEntity;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.inventory.DragonInventory;
import net.dragonmounts.registry.DragonType;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;
import static net.dragonmounts.entity.dragon.TameableDragonEntity.FLYING_DATA_PARAMETER_KEY;
import static net.dragonmounts.util.EntityUtil.finalizeSpawn;

public class DragonEssenceItem extends Item implements IDragonTypified, IEntityContainer<TameableDragonEntity> {
    private static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_essence";

    public final DragonType type;

    public DragonEssenceItem(DragonType type, Settings props) {
        super(props.maxCount(1));
        this.type = type;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World level = context.getWorld();
        if (level.isClient) return ActionResult.SUCCESS;
        ItemStack stack = context.getStack();
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getBlockPos();
        Direction direction = context.getSide();
        BlockPos spawnPos = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() ? pos : pos.offset(direction);
        level.spawnEntity(this.loadEntity(
                (ServerWorld) level,
                player,
                stack.getTag(),
                spawnPos,
                SpawnReason.SPAWN_EGG,
                null,
                true,
                !Objects.equals(pos, spawnPos) && direction == Direction.UP
        ));
        if (player != null) {
            if (!player.abilities.creativeMode) {
                stack.decrement(1);
            }
            player.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        return ActionResult.CONSUME;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        BlockHitResult result = raycast(level, player, RaycastContext.FluidHandling.SOURCE_ONLY);
        ItemStack stack = player.getStackInHand(hand);
        if (result.getType() != BlockHitResult.Type.BLOCK) return TypedActionResult.pass(stack);
        if (level.isClient) return TypedActionResult.success(stack);
        BlockPos pos = result.getBlockPos();
        if (!(level.getBlockState(pos).getBlock() instanceof FluidBlock)) return TypedActionResult.pass(stack);
        if (level.canPlayerModifyAt(player, pos) && player.canPlaceOn(pos, result.getSide(), stack)) {
            level.spawnEntity(this.loadEntity(
                    (ServerWorld) level,
                    player,
                    stack.getTag(),
                    pos,
                    SpawnReason.SPAWN_EGG,
                    null,
                    false,
                    false
            ));
            if (!player.abilities.creativeMode) {
                stack.decrement(1);
            }
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.success(stack);
        }
        return TypedActionResult.fail(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltips, TooltipContext flag) {
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

    @Override
    public ItemStack saveEntity(TameableDragonEntity entity) {
        ItemStack stack = new ItemStack(this);
        NbtCompound tag = IEntityContainer.simplifyData(entity.writeNbt(new NbtCompound()));
        tag.remove(FLYING_DATA_PARAMETER_KEY);
        tag.remove(DragonInventory.DATA_PARAMETER_KEY);
        tag.remove("UUID");
        tag.remove("AbsorptionAmount");
        tag.remove("Age");
        tag.remove("AgeLocked");
        tag.remove("ArmorDropChances");
        tag.remove("ArmorItems");
        tag.remove("Attributes");
        tag.remove("Brain");
        tag.remove("ForcedAge");
        tag.remove("HandDropChances");
        tag.remove("HandItems");
        tag.remove("Health");
        tag.remove("LifeStage");
        tag.remove("LoveCause");
        tag.remove("ShearCooldown");
        LivingEntity owner = entity.getOwner();
        if (owner != null) tag.putString("OwnerName", Text.Serializer.toJson(owner.getName()));
        stack.setTag(tag);
        return stack;
    }

    @Override
    public Class<TameableDragonEntity> getContentType() {
        return TameableDragonEntity.class;
    }

    @Override
    public ServerDragonEntity loadEntity(
            ServerWorld level,
            @Nullable PlayerEntity player,
            @Nullable NbtCompound tag,
            BlockPos pos,
            SpawnReason reason,
            @Nullable EntityData data,
            boolean yOffset,
            boolean extraOffset
    ) {
        ServerDragonEntity dragon = new ServerDragonEntity(level);
        if (tag == null) {
            finalizeSpawn(level, dragon, pos, reason, null, null, yOffset, extraOffset);
            dragon.setDragonType(this.type, true);
        } else {
            tag.remove("Passengers");
            finalizeSpawn(level, dragon, pos, reason, null, tag, yOffset, extraOffset);
            dragon.readNbt(dragon.writeNbt(new NbtCompound()).copyFrom(tag));
            dragon.setDragonType(this.type, false);
        }
        dragon.setLifeStage(DragonLifeStage.NEWBORN, true, false);
        return dragon;
    }

    @Override
    public boolean isEmpty(@Nullable NbtCompound tag) {
        return false;
    }

    @Override
    public boolean canSetNbt(MinecraftServer server, Entity entity, @Nullable PlayerEntity player) {
        return true;
    }
}
