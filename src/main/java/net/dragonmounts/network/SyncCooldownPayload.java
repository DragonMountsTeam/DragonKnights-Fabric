package net.dragonmounts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static net.dragonmounts.DragonMounts.makeId;

public record SyncCooldownPayload(int id, int cd) implements CustomPacketPayload {
    public static final Type<SyncCooldownPayload> TYPE = new Type<>(makeId("sync_cd"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCooldownPayload> CODEC = CustomPacketPayload.codec(SyncCooldownPayload::encode, SyncCooldownPayload::decode);

    public static SyncCooldownPayload decode(FriendlyByteBuf buffer) {
        return new SyncCooldownPayload(buffer.readVarInt(), buffer.readVarInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.id).writeVarInt(this.cd);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
