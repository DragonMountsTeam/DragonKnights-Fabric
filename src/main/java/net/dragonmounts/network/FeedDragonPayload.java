package net.dragonmounts.network;

import net.dragonmounts.entity.dragon.DragonLifeStage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import static net.dragonmounts.DragonMounts.makeId;

public record FeedDragonPayload(int id, int age, DragonLifeStage stage, Item food) implements CustomPacketPayload {
    public static final Type<FeedDragonPayload> TYPE = new Type<>(makeId("feed_dragon"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FeedDragonPayload> CODEC = CustomPacketPayload.codec(FeedDragonPayload::encode, FeedDragonPayload::decode);

    public static FeedDragonPayload decode(FriendlyByteBuf buffer) {
        return new FeedDragonPayload(buffer.readVarInt(), buffer.readVarInt(), DragonLifeStage.byId(buffer.readVarInt()), Item.byId(buffer.readVarInt()));
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.id).writeVarInt(this.age).writeVarInt(this.stage.ordinal()).writeVarInt(Item.getId(this.food));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}