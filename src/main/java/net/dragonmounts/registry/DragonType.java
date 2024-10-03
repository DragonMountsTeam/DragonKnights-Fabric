package net.dragonmounts.registry;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMultimap;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.dragonmounts.api.ArmorMaterialContext;
import net.dragonmounts.api.PassengerLocator;
import net.dragonmounts.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.item.DragonScalesItem;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.Util;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.VarInt;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.dragonmounts.DragonMounts.DRAGON_TYPE;
import static net.dragonmounts.DragonMounts.makeId;
import static net.dragonmounts.util.math.MathUtil.getColorVector;

public class DragonType implements TooltipProvider {
    public static final String DATA_PARAMETER_KEY = "DragonType";
    public static final ResourceLocation DEFAULT_KEY = makeId("ender");
    public static final DefaultedMappedRegistry<DragonType> REGISTRY = FabricRegistryBuilder.createDefaulted(DRAGON_TYPE, DEFAULT_KEY).buildAndRegister();
    public static final Codec<DragonType> CODEC = REGISTRY.byNameCodec();
    public static final StreamCodec<ByteBuf, DragonType> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull DragonType decode(ByteBuf buffer) {
            return REGISTRY.byId(VarInt.read(buffer));
        }

        @Override
        public void encode(ByteBuf buffer, DragonType type) {
            VarInt.write(buffer, REGISTRY.getId(type));
        }
    };
    public static final EntityDataSerializer<DragonType> SERIALIZER = EntityDataSerializer.forValueType(STREAM_CODEC);
    public final int color;
    public final Vector3f colorVector;
    public final int durabilityFactor;
    public final boolean convertible;
    public final boolean isSkeleton;
    public final ResourceLocation identifier;
    public final ImmutableMultimap<Holder<Attribute>, AttributeModifier> attributes;
    public final Predicate<HatchableDragonEggEntity> isHabitatEnvironment;
    public final PassengerLocator passengerLocator;
    public final SimpleParticleType sneezeParticle;
    public final SimpleParticleType eggParticle;
    public final DragonVariant.Manager variants = new DragonVariant.Manager(this);
    public final TranslatableContents name;
    public final Holder<ArmorMaterial> material;
    private final Reference2ObjectOpenHashMap<Class<?>, Object> map = new Reference2ObjectOpenHashMap<>();
    private final Style style;
    private final Set<ResourceKey<DamageType>> immunities;
    private final Set<Block> blocks;
    private final List<ResourceKey<Biome>> biomes;
    private ResourceKey<LootTable> lootTable;

    public DragonType(Builder builder, ResourceLocation identifier, int durabilityFactor, Function<DragonType, Holder<ArmorMaterial>> factory) {
        Registry.register(REGISTRY, this.identifier = identifier, this);
        this.colorVector = getColorVector(this.color = builder.color);
        this.durabilityFactor = durabilityFactor;
        this.convertible = builder.convertible;
        this.isSkeleton = builder.isSkeleton;
        this.style = Style.EMPTY.withColor(TextColor.fromRgb(this.color));
        this.attributes = builder.attributes.build();
        this.immunities = new HashSet<>(builder.immunities);
        this.blocks = new HashSet<>(builder.blocks);
        this.biomes = new ArrayList<>(builder.biomes);
        this.sneezeParticle = builder.sneezeParticle;
        this.eggParticle = builder.eggParticle;
        this.passengerLocator = builder.passengerLocator;
        this.isHabitatEnvironment = builder.isHabitatEnvironment;
        this.name = new TranslatableContents(this.makeDescriptionId(), null, TranslatableContents.NO_ARGS);
        this.material = factory.apply(this);
    }

    public final ResourceLocation getId() {
        return this.identifier;
    }

    protected String makeDescriptionId() {
        return Util.makeDescriptionId("dragon_type", this.identifier);
    }

    protected ResourceLocation makeLootLocation() {
        return this.identifier.withPath("entities/dragon/" + this.identifier.getPath());
    }

    public final ResourceKey<LootTable> getLootTable() {
        return this.lootTable == null ? this.lootTable = ResourceKey.create(Registries.LOOT_TABLE, this.makeLootLocation()) : this.lootTable;
    }

    public MutableComponent getName() {
        return MutableComponent.create(this.name).setStyle(this.style);
    }

    public MutableComponent getFormattedName(String pattern) {
        return Component.translatable(pattern, MutableComponent.create(this.name));
    }

    public boolean isInvulnerableTo(DamageSource source) {
        return !this.immunities.isEmpty() && source.typeHolder().is(this.immunities::contains);
    }

    public boolean isHabitat(Block block) {
        return !this.blocks.isEmpty() && this.blocks.contains(block);
    }

    public boolean isHabitat(ResourceKey<Biome> biome) {
        return biome != null && !this.biomes.isEmpty() && this.biomes.contains(biome);
    }

    @SuppressWarnings("UnusedReturnValue")
    public <T> T bindInstance(Class<T> clazz, T instance) {
        return clazz.cast(this.map.put(clazz, instance));
    }

    public <T> T getInstance(Class<T> clazz, T defaultValue) {
        return clazz.cast(this.map.getOrDefault(clazz, defaultValue));
    }

    public <T> void ifPresent(Class<T> clazz, Consumer<? super T> consumer) {
        var value = this.map.get(clazz);
        if (value != null) {
            consumer.accept(clazz.cast(value));
        }
    }

    public <T, V> V ifPresent(Class<T> clazz, Function<? super T, V> function, V defaultValue) {
        var value = this.map.get(clazz);
        if (value != null) {
            return function.apply(clazz.cast(value));
        }
        return defaultValue;
    }

    public Ingredient getRepairIngredient() {
        Object scales = this.map.get(DragonScalesItem.class);
        return scales == null ? Ingredient.EMPTY : Ingredient.of((DragonScalesItem) scales);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag) {
        consumer.accept(this.getName());
    }

    public static class Builder {
        public static final ResourceLocation BONUS_ID = makeId("dragon_type_bonus");
        public final ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> attributes = ImmutableMultimap.builder();
        public final int color;
        public final Set<ResourceKey<DamageType>> immunities = new HashSet<>();
        public final Set<Block> blocks = new HashSet<>();
        public final Set<ResourceKey<Biome>> biomes = new HashSet<>();
        public boolean convertible = true;
        public boolean isSkeleton = false;
        public SimpleParticleType sneezeParticle = ParticleTypes.LARGE_SMOKE;
        public SimpleParticleType eggParticle = ParticleTypes.MYCELIUM;
        public PassengerLocator passengerLocator = PassengerLocator.DEFAULT;
        public Predicate<HatchableDragonEggEntity> isHabitatEnvironment = Predicates.alwaysFalse();

        public Builder(int color) {
            this.color = color;
            // ignore suffocation damage
            this.addImmunity(DamageTypes.ON_FIRE).addImmunity(DamageTypes.IN_FIRE)
                    .addImmunity(DamageTypes.HOT_FLOOR)
                    .addImmunity(DamageTypes.LAVA)
                    .addImmunity(DamageTypes.DROWN)
                    .addImmunity(DamageTypes.IN_WALL)
                    .addImmunity(DamageTypes.CACTUS) // assume that cactus needles don't do much damage to animals with horned scales
                    .addImmunity(DamageTypes.DRAGON_BREATH); // ignore damage from vanilla ender dragon. I kinda disabled this because it wouldn't make any sense, feel free to re enable
        }

        public Builder notConvertible() {
            this.convertible = false;
            return this;
        }

        public Builder isSkeleton() {
            this.isSkeleton = true;
            return this;
        }

        public Builder putAttributeModifier(Holder<Attribute> attribute, ResourceLocation identifier, double value, AttributeModifier.Operation operation) {
            this.attributes.put(attribute, new AttributeModifier(identifier, value, operation));
            return this;
        }

        public Builder addImmunity(ResourceKey<DamageType> type) {
            this.immunities.add(type);
            return this;
        }

        public Builder addHabitat(Block block) {
            this.blocks.add(block);
            return this;
        }

        public Builder addHabitat(ResourceKey<Biome> biome) {
            this.biomes.add(biome);
            return this;
        }

        public Builder setSneezeParticle(SimpleParticleType particle) {
            this.sneezeParticle = particle;
            return this;
        }

        public Builder setEggParticle(SimpleParticleType particle) {
            this.eggParticle = particle;
            return this;
        }

        public Builder setPassengerOffset(PassengerLocator offset) {
            this.passengerLocator = offset;
            return this;
        }

        public Builder setEnvironmentPredicate(Predicate<HatchableDragonEggEntity> predicate) {
            this.isHabitatEnvironment = predicate;
            return this;
        }

        public DragonType build(ResourceLocation identifier) {
            return new DragonType(this, identifier, 50, $ -> ArmorMaterials.ARMADILLO);
        }

        public DragonType build(ResourceLocation identifier, ArmorMaterialContext context) {
            return this.build(identifier, context, List.of(new ArmorMaterial.Layer(identifier)));
        }

        public DragonType build(ResourceLocation identifier, ArmorMaterialContext context, List<ArmorMaterial.Layer> layers) {
            return new DragonType(this, identifier, context.durabilityFactor, type ->
                    Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL, identifier, new ArmorMaterial(
                            new EnumMap<>(context.defense),
                            context.enchantmentValue,
                            context.sound,
                            type::getRepairIngredient,
                            layers,
                            context.toughness,
                            context.knockbackResistance
                    ))
            );
        }
    }
}
