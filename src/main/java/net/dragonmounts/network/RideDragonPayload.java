package net.dragonmounts.network;

import net.dragonmounts.entity.dragon.ServerDragonEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static net.dragonmounts.DragonMounts.makeId;

public record RideDragonPayload(int id, int flag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RideDragonPayload> TYPE = new CustomPacketPayload.Type<>(makeId("ride_dragon"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RideDragonPayload> CODEC = CustomPacketPayload.codec(RideDragonPayload::encode, RideDragonPayload::decode);

    public static RideDragonPayload decode(FriendlyByteBuf buffer) {
        return new RideDragonPayload(buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(RideDragonPayload payload, ServerPlayNetworking.Context context) {
        if (context.player().level().getEntity(payload.id()) instanceof ServerDragonEntity dragon) {
            int flag = payload.flag();
            /*dragon.playerControlledGoal.handlePacket(
                    (flag & 0b0001) == 0b0001,
                    (flag & 0b0010) == 0b0010,
                    (flag & 0b0100) == 0b0100,
                    (flag & 0b1000) == 0b1000
            );*/
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.id).writeVarInt(this.flag);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}