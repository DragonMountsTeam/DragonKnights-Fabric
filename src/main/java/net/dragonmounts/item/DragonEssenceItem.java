package net.dragonmounts.item;

import net.dragonmounts.api.DragonTypified;
import net.dragonmounts.entity.dragon.DragonLifeStage;
import net.dragonmounts.entity.dragon.ServerDragonEntity;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.init.DMDataComponents;
import net.dragonmounts.inventory.DragonInventory;
import net.dragonmounts.registry.DragonType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;
import static net.dragonmounts.entity.dragon.TameableDragonEntity.FLYING_DATA_PARAMETER_KEY;
import static net.dragonmounts.util.EntityUtil.*;
import static net.dragonmounts.util.ScoreboardInfo.applyScores;
import static net.minecraft.world.InteractionResultHolder.*;

public class DragonEssenceItem extends Item implements DragonTypified, IEntityContainer<TameableDragonEntity> {
    private static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_essence";

    public final DragonType type;

    public DragonEssenceItem(DragonType type, Item.Properties props) {
        super(props.stacksTo(1).component(DMDataComponents.DRAGON_TYPE, type));
        this.type = type;
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
            stack.shrink(1);
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
            stack.consume(1, player);
            player.awardStat(Stats.ITEM_USED.get(this));
            return success(stack);
        }
        return fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag flag) {
        tooltips.add(this.type.getName());
    }

    @Override
    public @NotNull String getDescriptionId() {
        return TRANSLATION_KEY;
    }

    @Override
    public DragonType getDragonType() {
        return this.type;
    }

    @Override
    public @NotNull ItemStack saveEntity(TameableDragonEntity entity, DataComponentPatch patch) {
        var stack = new ItemStack(this);
        var tag = saveWithId(entity, new CompoundTag());
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
        tag.remove("Sitting");
        stack.set(DataComponents.ENTITY_DATA, IEntityContainer.simplifyData(tag));
        stack.applyComponents(patch);
        return stack;
    }

    @Override
    public ServerDragonEntity loadEntity(
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
        dragon.setLifeStage(DragonLifeStage.NEWBORN, true, false);
        applyScores(level.getScoreboard(), stack, dragon);
        return dragon;
    }

    @Override
    public final Class<TameableDragonEntity> getContentType() {
        return TameableDragonEntity.class;
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        return false;
    }
}
