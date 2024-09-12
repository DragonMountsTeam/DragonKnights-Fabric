package net.dragonmounts.registry;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMultimap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.dragonmounts.api.PassengerLocator;
import net.dragonmounts.entity.dragon.HatchableDragonEggEntity;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.block.Block;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.dragonmounts.DragonMounts.MOD_ID;

public class DragonType {
    public static final String DATA_PARAMETER_KEY = "DragonType";
    public static final Identifier DEFAULT_KEY = new Identifier(MOD_ID, "ender");
    public static final DefaultedRegistry<DragonType> REGISTRY = FabricRegistryBuilder.createDefaulted(DragonType.class, new Identifier(MOD_ID, "dragon_type"), DEFAULT_KEY).buildAndRegister();
    public static final TrackedDataHandler<DragonType> SERIALIZER = new TrackedDataHandler<DragonType>() {
        @Override
        public void write(PacketByteBuf buffer, DragonType value) {
            buffer.writeVarInt(REGISTRY.getRawId(value));
        }

        @Override
        public DragonType read(PacketByteBuf buffer) {
            return REGISTRY.get(buffer.readVarInt());
        }

        @Override
        public DragonType copy(DragonType value) {
            return value;
        }
    };
    public final int color;
    public final boolean convertible;
    public final boolean isSkeleton;
    public final Identifier identifier;
    public final ImmutableMultimap<EntityAttribute, EntityAttributeModifier> attributes;
    public final Predicate<HatchableDragonEggEntity> isHabitatEnvironment;
    public final PassengerLocator passengerLocator;
    public final ParticleEffect sneezeParticle;
    public final ParticleEffect eggParticle;
    public final DragonVariant.Manager variants = new DragonVariant.Manager(this);
    public final String translationKey;
    public final Identifier lootTable;
    private final Reference2ObjectOpenHashMap<Class<?>, Object> map = new Reference2ObjectOpenHashMap<>();
    private final Style style;
    private final Set<DamageSource> immunities;
    private final Set<Block> blocks;
    private final List<RegistryKey<Biome>> biomes;

    public DragonType(Identifier identifier, Properties props) {
        Registry.register(REGISTRY, this.identifier = identifier, this);
        this.color = props.color;
        this.convertible = props.convertible;
        this.isSkeleton = props.isSkeleton;
        this.style = Style.EMPTY.withColor(TextColor.fromRgb(this.color));
        this.attributes = props.attributes.build();
        this.immunities = new HashSet<>(props.immunities);
        this.blocks = new HashSet<>(props.blocks);
        this.biomes = new ArrayList<>(props.biomes);
        this.sneezeParticle = props.sneezeParticle;
        this.eggParticle = props.eggParticle;
        this.passengerLocator = props.passengerLocator;
        this.isHabitatEnvironment = props.isHabitatEnvironment;
        this.translationKey = createTranslationKey();
        this.lootTable = createLootTable();
    }

    protected String createTranslationKey() {
        return Util.createTranslationKey("dragon_type", this.identifier);
    }

    protected Identifier createLootTable() {
        return new Identifier(this.identifier.getNamespace(), "entities/dragon/" + this.identifier.getPath());
    }

    public TranslatableText getName() {
        return (TranslatableText) new TranslatableText(this.translationKey).fillStyle(this.style);
    }

    public TranslatableText getFormattedName(String pattern) {
        return new TranslatableText(pattern, new TranslatableText(this.translationKey));
    }

    public boolean isInvulnerableTo(DamageSource source) {
        return !this.immunities.isEmpty() && this.immunities.contains(source);
    }

    public boolean isHabitat(Block block) {
        return !this.blocks.isEmpty() && this.blocks.contains(block);
    }

    public boolean isHabitat(RegistryKey<Biome> biome) {
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
        Object value = this.map.get(clazz);
        if (value != null) {
            consumer.accept(clazz.cast(value));
        }
    }

    public <T, V> V ifPresent(Class<T> clazz, Function<? super T, V> function, V defaultValue) {
        Object value = this.map.get(clazz);
        if (value != null) {
            return function.apply(clazz.cast(value));
        }
        return defaultValue;
    }

    public static class Properties {
        protected static final UUID MODIFIER_UUID = UUID.fromString("12e4cc82-db6d-5676-afc5-86498f0f6464");
        public final ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> attributes = ImmutableMultimap.builder();
        public final int color;
        public final Set<DamageSource> immunities = new HashSet<>();
        public final Set<Block> blocks = new HashSet<>();
        public final Set<RegistryKey<Biome>> biomes = new HashSet<>();
        public boolean convertible = true;
        public boolean isSkeleton = false;
        public ParticleEffect sneezeParticle = ParticleTypes.LARGE_SMOKE;
        public ParticleEffect eggParticle = ParticleTypes.MYCELIUM;
        public PassengerLocator passengerLocator = PassengerLocator.DEFAULT;
        public Predicate<HatchableDragonEggEntity> isHabitatEnvironment = Predicates.alwaysFalse();

        public Properties(int color) {
            this.color = color;
            // ignore suffocation damage
            this.addImmunity(DamageSource.ON_FIRE).addImmunity(DamageSource.IN_FIRE)
                    .addImmunity(DamageSource.HOT_FLOOR)
                    .addImmunity(DamageSource.LAVA)
                    .addImmunity(DamageSource.DROWN)
                    .addImmunity(DamageSource.IN_WALL)
                    .addImmunity(DamageSource.CACTUS) // assume that cactus needles don't do much damage to animals with horned scales
                    .addImmunity(DamageSource.DRAGON_BREATH); // ignore damage from vanilla ender dragon. I kinda disabled this because it wouldn't make any sense, feel free to re enable
        }

        public Properties notConvertible() {
            this.convertible = false;
            return this;
        }

        public Properties isSkeleton() {
            this.isSkeleton = true;
            return this;
        }

        public Properties putAttributeModifier(EntityAttribute attribute, String name, double value, EntityAttributeModifier.Operation operation) {
            this.attributes.put(attribute, new EntityAttributeModifier(MODIFIER_UUID, name, value, operation));
            return this;
        }

        public Properties addImmunity(DamageSource source) {
            this.immunities.add(source);
            return this;
        }

        public Properties addHabitat(Block block) {
            this.blocks.add(block);
            return this;
        }

        public Properties addHabitat(RegistryKey<Biome> block) {
            this.biomes.add(block);
            return this;
        }

        public Properties setSneezeParticle(ParticleEffect particle) {
            this.sneezeParticle = particle;
            return this;
        }

        public Properties setEggParticle(ParticleEffect particle) {
            this.eggParticle = particle;
            return this;
        }

        public Properties setPassengerLocator(PassengerLocator locator) {
            this.passengerLocator = locator;
            return this;
        }

        public Properties setEnvironmentPredicate(Predicate<HatchableDragonEggEntity> predicate) {
            this.isHabitatEnvironment = predicate;
            return this;
        }
    }
}
