package net.dragonmounts.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import static net.dragonmounts.DragonMounts.makeId;

public record InitCooldownPayload(int size, int[] data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InitCooldownPayload> TYPE = new CustomPacketPayload.Type<>(makeId("init_cd"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InitCooldownPayload> CODEC = CustomPacketPayload.codec(InitCooldownPayload::encode, InitCooldownPayload::decode);

    public static InitCooldownPayload decode(FriendlyByteBuf buffer) {
        final int maxSize = buffer.readVarInt();
        final int[] data = new int[maxSize];
        int i = 0;
        while (i < maxSize && buffer.isReadable()) {
            data[i++] = buffer.readVarInt();
        }
        return new InitCooldownPayload(i, data);
    }

    public void encode(FriendlyByteBuf buffer) {
        int[] data = this.data;
        int size = this.size;
        buffer.writeVarInt(size);
        for (int i = 0; i < size; ++i) {
            buffer.writeVarInt(data[i]);
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
