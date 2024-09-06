package net.dragonmounts.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.dragonmounts.entity.dragon.TameableDragonEntity.FLYING_DATA_PARAMETER_KEY;

public interface IEntityContainer<T extends Entity> {
    static CustomData simplifyData(CompoundTag tag) {
        tag.remove("Air");
        tag.remove("DeathTime");
        tag.remove("FallDistance");
        tag.remove("FallFlying");
        tag.remove("Fire");
        tag.remove("HurtByTimestamp");
        tag.remove("HurtTime");
        tag.remove("InLove");
        tag.remove("Leash");
        tag.remove("Motion");
        tag.remove("OnGround");
        tag.remove("Passengers");
        tag.remove("PortalCooldown");
        tag.remove("Pos");
        tag.remove("Rotation");
        tag.remove("Sitting");
        tag.remove("SleepingX");
        tag.remove("SleepingY");
        tag.remove("SleepingZ");
        tag.remove("TicksFrozen");
        return CustomData.of(tag);
    }

    static ItemStack saveEntityData(Item item, CompoundTag tag, DataComponentPatch patch) {
        var stack = new ItemStack(item);
        tag.remove(FLYING_DATA_PARAMETER_KEY);
        tag.remove("UUID");
        stack.set(DataComponents.ENTITY_DATA, IEntityContainer.simplifyData(tag));
        stack.applyComponents(patch);
        return stack;
    }

    @NotNull
    ItemStack saveEntity(T entity, DataComponentPatch patch);

    /**
     * @see net.minecraft.world.entity.EntityType#spawn(ServerLevel, ItemStack, Player, BlockPos, MobSpawnType, boolean, boolean)
     * @see net.dragonmounts.util.EntityUtil#finalizeSpawn(ServerLevel, Entity, BlockPos, MobSpawnType, boolean, boolean)
     */
    @Nullable
    Entity loadEntity(
            ServerLevel level,
            ItemStack stack,
            @Nullable Player player,
            BlockPos pos,
            MobSpawnType reason,
            boolean yOffset,
            boolean extraOffset
    );

    Class<T> getContentType();

    default boolean isEmpty(ItemStack stack) {
        return !stack.has(DataComponents.ENTITY_DATA);
    }
}
