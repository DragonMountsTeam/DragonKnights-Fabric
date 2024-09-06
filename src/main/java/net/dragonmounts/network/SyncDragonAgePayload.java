package net.dragonmounts.network;

import net.dragonmounts.entity.dragon.DragonLifeStage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import static net.dragonmounts.DragonMounts.makeId;

public record SyncDragonAgePayload(int id, int age, DragonLifeStage stage) implements CustomPacketPayload {
    public static final Type<SyncDragonAgePayload> TYPE = new Type<>(makeId("sync_dragon"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDragonAgePayload> CODEC = CustomPacketPayload.codec(SyncDragonAgePayload::encode, SyncDragonAgePayload::decode);

    public static SyncDragonAgePayload decode(FriendlyByteBuf buffer) {
        return new SyncDragonAgePayload(buffer.readVarInt(), buffer.readVarInt(), DragonLifeStage.byId(buffer.readVarInt()));
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.id).writeVarInt(this.age).writeVarInt(this.stage.ordinal());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
