package net.dragonmounts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static net.dragonmounts.DragonMounts.makeId;

public record SyncEggAgePayload(int id, int age) implements CustomPacketPayload {
    public static final Type<SyncEggAgePayload> TYPE = new Type<>(makeId("sync_egg"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEggAgePayload> CODEC = CustomPacketPayload.codec(SyncEggAgePayload::encode, SyncEggAgePayload::decode);

    public static SyncEggAgePayload decode(FriendlyByteBuf buffer) {
        return new SyncEggAgePayload(buffer.readVarInt(), buffer.readVarInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.id).writeVarInt(this.age);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
