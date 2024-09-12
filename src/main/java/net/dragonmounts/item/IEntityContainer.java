package net.dragonmounts.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface IEntityContainer<T extends Entity> {
    static NbtCompound simplifyData(NbtCompound tag) {
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
        return tag;
    }

    @Nullable
    Entity loadEntity(
            ServerWorld level,
            @Nullable PlayerEntity player,
            NbtCompound tag,
            BlockPos pos,
            SpawnReason reason,
            @Nullable EntityData data,
            boolean yOffset,
            boolean extraOffset
    );

    ItemStack saveEntity(T entity);

    Class<T> getContentType();

    boolean isEmpty(@Nullable NbtCompound tag);

    default boolean canSetNbt(MinecraftServer server, Entity entity, PlayerEntity player) {
        return !entity.entityDataRequiresOperator() || player != null && server.getPlayerManager().isOperator(player.getGameProfile());
    }
}
