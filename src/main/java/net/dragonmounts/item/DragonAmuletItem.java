package net.dragonmounts.item;

import net.dragonmounts.api.IDragonTypified;
import net.dragonmounts.entity.dragon.ServerDragonEntity;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.init.DMItems;
import net.dragonmounts.registry.DragonType;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static net.dragonmounts.entity.dragon.TameableDragonEntity.FLYING_DATA_PARAMETER_KEY;
import static net.dragonmounts.util.EntityUtil.*;

public class DragonAmuletItem extends AmuletItem<TameableDragonEntity> implements IDragonTypified {
    private static final Logger LOGGER = LogManager.getLogger();
    public final DragonType type;

    public DragonAmuletItem(DragonType type, Settings props) {
        super(TameableDragonEntity.class, props);
        this.type = type;
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
                level.spawnEntity(this.loadEntity(
                        (ServerWorld) level,
                        player,
                        stack.getTag(),
                        target.getBlockPos(),
                        SpawnReason.BUCKET,
                        null,
                        false,
                        false
                ));
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
                SpawnReason.BUCKET,
                null,
                true,
                !Objects.equals(pos, spawnPos) && direction == Direction.UP
        ));
        if (player != null) {
            consume(player, context.getHand(), stack, new ItemStack(DMItems.AMULET));
            player.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        return ActionResult.CONSUME;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        BlockHitResult result = raycast(level, player, RaycastContext.FluidHandling.SOURCE_ONLY);
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
                    SpawnReason.BUCKET,
                    null,
                    false,
                    false
            ));
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.success(consume(player, hand, stack, new ItemStack(DMItems.AMULET)));
        }
        return TypedActionResult.fail(stack);

    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltips, TooltipContext flag) {
        NbtCompound tag = stack.getTag();
        tooltips.add(new TranslatableText("tooltip.dragonmounts.type", this.type.getName()).formatted(Formatting.GRAY));
        if (tag != null) {
            try {
                String string = tag.getString("CustomName");
                if (!string.isEmpty()) {
                    tooltips.add(new TranslatableText("tooltip.dragonmounts.custom_name", Text.Serializer.fromJson(string)).formatted(Formatting.GRAY));
                }
                tooltips.add(new TranslatableText("tooltip.dragonmounts.health", new LiteralText(Float.toString(tag.getFloat("Health"))).formatted(Formatting.GREEN)).formatted(Formatting.GRAY));
                if (tag.contains("Owner")) {
                    string = tag.getString("OwnerName");
                    if (!string.isEmpty()) {
                        tooltips.add(new TranslatableText("tooltip.dragonmounts.owner_name", Text.Serializer.fromJson(string)).formatted(Formatting.GRAY));
                    }
                    return;
                }
            } catch (Exception exception) {
                LOGGER.warn(exception);
            }
        }
        tooltips.add(new TranslatableText("tooltip.dragonmounts.missing").formatted(Formatting.RED));
    }

    @Override
    public ItemStack saveEntity(TameableDragonEntity entity) {
        ItemStack stack = new ItemStack(this);
        entity.removeAllPassengers();
        NbtCompound tag = IEntityContainer.simplifyData(entity.writeNbt(new NbtCompound()));
        tag.remove(FLYING_DATA_PARAMETER_KEY);
        tag.remove("UUID");
        LivingEntity owner = entity.getOwner();
        if (owner != null) {
            tag.putString("OwnerName", Text.Serializer.toJson(owner.getName()));
        }
        stack.setTag(saveScoreboard(entity, tag));
        return stack;
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
            loadScores(dragon, tag).setDragonType(this.type, false);
        }
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

    @Override
    public DragonType getDragonType() {
        return this.type;
    }
}
