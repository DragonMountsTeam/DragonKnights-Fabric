package net.dragonmounts.registry;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.dragonmounts.api.DragonTypified;
import net.dragonmounts.block.DragonHeadBlock;
import net.dragonmounts.block.DragonHeadWallBlock;
import net.dragonmounts.client.variant.VariantAppearance;
import net.dragonmounts.init.DMDataComponents;
import net.dragonmounts.init.DragonVariants;
import net.dragonmounts.item.DragonHeadItem;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE;
import static net.dragonmounts.DragonMounts.DRAGON_VARIANT;
import static net.dragonmounts.DragonMounts.makeId;

public class DragonVariant implements DragonTypified {
    public static final String DATA_PARAMETER_KEY = "Variant";
    public static final ResourceLocation DEFAULT_KEY = makeId("ender_female");
    public static final DefaultedMappedRegistry<DragonVariant> REGISTRY = FabricRegistryBuilder.createDefaulted(DRAGON_VARIANT, DEFAULT_KEY).buildAndRegister();
    public static final Codec<DragonVariant> CODEC = REGISTRY.byNameCodec();
    public static final StreamCodec<ByteBuf, DragonVariant> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull DragonVariant decode(ByteBuf buffer) {
            return REGISTRY.byId(VarInt.read(buffer));
        }
        @Override
        public void encode(ByteBuf buffer, DragonVariant type) {
            VarInt.write(buffer, REGISTRY.getId(type));
        }
    };
    public static final EntityDataSerializer<DragonVariant> SERIALIZER = EntityDataSerializer.forValueType(STREAM_CODEC);

    public static @NotNull DragonVariant draw(DragonType type, RandomSource random) {
        return type.variants.draw(random, DragonVariants.ENDER_FEMALE, true);
    }

    public static @NotNull DragonVariant draw(DragonType type, RandomSource random, String current) {
        if (current.isEmpty()) return type.variants.draw(random, DragonVariants.ENDER_FEMALE, true);
        var variant = DragonVariant.REGISTRY.getOptional(ResourceLocation.tryParse(current)).orElse(null);
        return variant == null
                ? type.variants.draw(random, DragonVariants.ENDER_FEMALE, true)
                : type.variants.draw(random, variant, false);
    }

    public final DragonType type;
    public final ResourceLocation identifier;
    public final DragonHeadItem headItem;
    public final DragonHeadBlock headBlock;
    public final DragonHeadWallBlock headWallBlock;
    int index = -1;// non-private to simplify nested class access
    private VariantAppearance appearance;

    public DragonVariant(DragonType type, String namespace, String name, BlockBehaviour.Properties block, Item.Properties item) {
        this.identifier = ResourceLocation.fromNamespaceAndPath(namespace, name);
        this.type = type;
        type.variants.register(this);
        var standing = this.identifier.withPath(name + "_dragon_head");
        block.instrument(NoteBlockInstrument.DRAGON).pushReaction(PushReaction.DESTROY);
        this.headBlock = Registry.register(BuiltInRegistries.BLOCK, standing, new DragonHeadBlock(this, block));
        this.headWallBlock = Registry.register(BuiltInRegistries.BLOCK, this.identifier.withPath(name + "_dragon_head_wall"), new DragonHeadWallBlock(this, BlockBehaviour.Properties.ofFullCopy(this.headBlock)));
        this.headItem = Registry.register(BuiltInRegistries.ITEM, standing, new DragonHeadItem(this, this.headBlock, this.headWallBlock, item.component(DMDataComponents.DRAGON_TYPE, type)));
    }

    @Override
    public final DragonType getDragonType() {
        return this.type;
    }

    public VariantAppearance getAppearance(VariantAppearance defaultValue) {
        return this.appearance == null ? defaultValue : this.appearance;
    }

    @SuppressWarnings("UnusedReturnValue")
    public VariantAppearance setAppearance(VariantAppearance value) {
        var old = this.appearance;
        this.appearance = value;
        return old;
    }

    public DragonHeadItem getHeadItem() {
        return this.headItem;
    }

    public DragonHeadBlock getHeadBlock() {
        return this.headBlock;
    }

    public DragonHeadWallBlock getHeadWallBlock() {
        return this.headWallBlock;
    }

    /**
     * Simplified {@link it.unimi.dsi.fastutil.objects.ReferenceArrayList}
     */
    public static final class Manager implements DragonTypified {
        public static final int DEFAULT_INITIAL_CAPACITY = 8;
        public final DragonType type;
        private DragonVariant[] variants = {};
        private int size;

        public Manager(DragonType type) {
            this.type = type;
        }

        private void grow(int capacity) {
            if (capacity <= this.variants.length) return;
            if (this.variants.length > 0)
                capacity = (int) Math.max(Math.min((long) this.variants.length + (this.variants.length >> 1), MAX_ARRAY_SIZE), capacity);
            else if (capacity < DEFAULT_INITIAL_CAPACITY)
                capacity = DEFAULT_INITIAL_CAPACITY;
            final DragonVariant[] array = new DragonVariant[capacity];
            System.arraycopy(this.variants, 0, array, 0, size);
            this.variants = array;
            assert this.size <= this.variants.length;
        }

        @SuppressWarnings("UnusedReturnValue")
        boolean add(final DragonVariant variant) {
            if (variant.type != this.type || variant.index >= 0) return false;
            this.grow(this.size + 1);
            variant.index = this.size;
            this.variants[this.size++] = variant;
            assert this.size <= this.variants.length;
            return true;
        }

        public DragonVariant draw(RandomSource random, @Nullable DragonVariant current, boolean acceptSelf) {
            switch (this.size) {
                case 0: return current;
                case 1: return this.variants[0];
            }
            if (acceptSelf || current == null || current.type != this.type) {
                return this.variants[random.nextInt(this.size)];
            }
            if (this.size == 2) return this.variants[(current.index ^ 1) & 1];//current.index == 0 ? 1 : 0
            int index = random.nextInt(this.size - 1);
            return this.variants[index < current.index ? index : index + 1];
        }

        public int size() {
            return this.size;
        }

        @Override
        public DragonType getDragonType() {
            return this.type;
        }

        public void register(DragonVariant variant) {
            if (variant.type == this.type) this.add(Registry.register(REGISTRY, variant.identifier, variant));
        }
    }
}
