package net.dragonmounts.registry;

import io.netty.buffer.ByteBuf;
import net.dragonmounts.entity.CarriageEntity;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static net.dragonmounts.DragonMounts.CARRIAGE_TYPE;
import static net.dragonmounts.DragonMounts.makeId;

public abstract class CarriageType {
    public static final ResourceLocation DEFAULT_KEY = makeId("oak");
    public static final DefaultedMappedRegistry<CarriageType> REGISTRY = FabricRegistryBuilder.createDefaulted(CARRIAGE_TYPE, DEFAULT_KEY).buildAndRegister();
    public static final StreamCodec<ByteBuf, CarriageType> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull CarriageType decode(ByteBuf buffer) {
            return REGISTRY.byId(VarInt.read(buffer));
        }

        @Override
        public void encode(ByteBuf buffer, CarriageType type) {
            VarInt.write(buffer, REGISTRY.getId(type));
        }
    };
    public static final EntityDataSerializer<CarriageType> SERIALIZER = EntityDataSerializer.forValueType(STREAM_CODEC);

    public abstract Item getItem(CarriageEntity entity);

    public abstract ResourceLocation getTexture(CarriageEntity entity);

    public static class Default extends CarriageType {
        public final Supplier<? extends Item> item;
        public final ResourceLocation texture;

        public Default(Supplier<? extends Item> item, ResourceLocation texture) {
            this.item = item;
            this.texture = texture;
        }

        @Override
        public Item getItem(CarriageEntity entity) {
            return this.item.get();
        }

        @Override
        public ResourceLocation getTexture(CarriageEntity entity) {
            return this.texture;
        }
    }
}
