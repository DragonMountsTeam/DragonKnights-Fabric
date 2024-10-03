package net.dragonmounts.network;

import net.dragonmounts.registry.FluteCommand;
import net.dragonmounts.util.FluteSound;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

import static net.dragonmounts.DragonMounts.makeId;

public record FluteCommandPayload(UUID source, int command) implements CustomPacketPayload {
    public static final Type<FluteCommandPayload> TYPE = new Type<>(makeId("flute_command"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluteCommandPayload> CODEC = CustomPacketPayload.codec(
            FluteCommandPayload::encode, FluteCommandPayload::decode
    );

    public static FluteCommandPayload decode(FriendlyByteBuf buffer) {
        return new FluteCommandPayload(buffer.readUUID(), buffer.readVarInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.source).writeVarInt(this.command);
    }

    public static void handle(FluteCommandPayload payload, ServerPlayNetworking.Context ignored) {
        var command = FluteCommand.REGISTRY.byId(payload.command);
        if (command != null) {
            FluteSound.getOrCreate(payload.source).play(command);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
