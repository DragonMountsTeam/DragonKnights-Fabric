package net.dragonmounts.init;

import net.dragonmounts.api.ArmorMaterialContext;
import net.dragonmounts.registry.DragonType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;

import static net.dragonmounts.DragonMounts.makeId;
import static net.dragonmounts.registry.DragonType.Builder.BONUS_ID;
import static net.minecraft.world.item.ArmorItem.Type.*;

public class DragonTypes {
    public static final DragonType AETHER;
    public static final DragonType ENCHANT;
    public static final DragonType ENDER;
    public static final DragonType FIRE;
    public static final DragonType FOREST;
    public static final DragonType ICE;
    public static final DragonType MOONLIGHT;
    public static final DragonType NETHER;
    public static final DragonType SCULK;
    public static final DragonType SKELETON;
    public static final DragonType STORM;
    public static final DragonType SUNLIGHT;
    public static final DragonType TERRA;
    public static final DragonType WATER;
    public static final DragonType WITHER;
    public static final DragonType ZOMBIE;

    static {
        ArmorMaterialContext context = new ArmorMaterialContext(50)
                .setDefense(HELMET, 3)
                .setDefense(CHESTPLATE, 8)
                .setDefense(LEGGINGS, 7)
                .setDefense(BOOTS, 3)
                .setEnchantmentValue(11)
                .setToughness(7.0F);
        MOONLIGHT = new DragonType.Builder(0x2C427C)
                .addHabitat(Blocks.BLUE_GLAZED_TERRACOTTA)
                .build(makeId("moonlight"), context);
        STORM = new DragonType.Builder(0xF5F1E9)
                .build(makeId("storm"), context);
        TERRA = new DragonType.Builder(0xA56C21)
                .addHabitat(Blocks.TERRACOTTA)
                .addHabitat(Blocks.SAND)
                .addHabitat(Blocks.SANDSTONE)
                .addHabitat(Blocks.SANDSTONE_SLAB)
                .addHabitat(Blocks.SANDSTONE_STAIRS)
                .addHabitat(Blocks.SANDSTONE_WALL)
                .addHabitat(Blocks.RED_SAND)
                .addHabitat(Blocks.RED_SANDSTONE)
                .addHabitat(Blocks.RED_SANDSTONE_SLAB)
                .addHabitat(Blocks.RED_SANDSTONE_STAIRS)
                .addHabitat(Blocks.RED_SANDSTONE_WALL)
                //.addHabitat(BiomeKeys.MESA)
                //.addHabitat(BiomeKeys.MESA_ROCK)
                //.addHabitat(BiomeKeys.MESA_CLEAR_ROCK)
                //.addHabitat(BiomeKeys.MUTATED_MESA_CLEAR_ROCK)
                //.addHabitat(BiomeKeys.MUTATED_MESA_ROCK)
                .build(makeId("terra"), context);
        ZOMBIE = new DragonType.Builder(0x5A5602)
                .addImmunity(DamageTypes.MAGIC)
                .addImmunity(DamageTypes.HOT_FLOOR)
                .addImmunity(DamageTypes.LIGHTNING_BOLT)
                .addImmunity(DamageTypes.WITHER)
                .addHabitat(Blocks.SOUL_SAND)
                .addHabitat(Blocks.SOUL_SAND)
                .addHabitat(Blocks.NETHER_WART_BLOCK)
                .addHabitat(Blocks.WARPED_WART_BLOCK)
                .build(makeId("zombie"), context);
        context.setDefense(HELMET, 4).setDefense(BOOTS, 4);
        AETHER = new DragonType.Builder(0x0294BD)
                .addImmunity(DamageTypes.MAGIC)
                .addImmunity(DamageTypes.HOT_FLOOR)
                .addImmunity(DamageTypes.LIGHTNING_BOLT)
                .addImmunity(DamageTypes.WITHER)
                .addHabitat(Blocks.LAPIS_BLOCK)
                .addHabitat(Blocks.LAPIS_ORE)
                .setEnvironmentPredicate(egg -> egg.getY() >= egg.level().getHeight() * 0.625)
                .build(makeId("aether"), context);
        FIRE = new DragonType.Builder(0x960B0F)
                .addImmunity(DamageTypes.MAGIC)
                .addImmunity(DamageTypes.HOT_FLOOR)
                .addImmunity(DamageTypes.LIGHTNING_BOLT)
                .addImmunity(DamageTypes.WITHER)
                .addHabitat(Blocks.FIRE)
                //.addHabitat(Blocks.LIT_FURNACE)
                .addHabitat(Blocks.LAVA)
                //.addHabitat(Blocks.FLOWING_LAVA)
                .build(makeId("fire"), context);
        FOREST = new DragonType.Builder(0x298317)
                .addImmunity(DamageTypes.MAGIC)
                .addImmunity(DamageTypes.HOT_FLOOR)
                .addImmunity(DamageTypes.LIGHTNING_BOLT)
                .addImmunity(DamageTypes.WITHER)
                //.addHabitat(Blocks.YELLOW_FLOWER)
                //.addHabitat(Blocks.RED_FLOWER)
                .addHabitat(Blocks.MOSSY_COBBLESTONE)
                .addHabitat(Blocks.VINE)
                //.addHabitat(Blocks.SAPLING)
                //.addHabitat(Blocks.LEAVES)
                //.addHabitat(Blocks.LEAVES2)
                .addHabitat(Biomes.JUNGLE)
                //.addHabitat(Biomes.JUNGLE_HILLS)
                .build(makeId("forest"), context);
        ICE = new DragonType.Builder(0x00F2FF)
                .addImmunity(DamageTypes.MAGIC)
                .addImmunity(DamageTypes.HOT_FLOOR)
                .addImmunity(DamageTypes.LIGHTNING_BOLT)
                .addImmunity(DamageTypes.WITHER)
                .addHabitat(Blocks.SNOW)
                .addHabitat(Blocks.ICE)
                .addHabitat(Blocks.PACKED_ICE)
                .addHabitat(Blocks.FROSTED_ICE)
                .addHabitat(Biomes.FROZEN_OCEAN)
                .addHabitat(Biomes.FROZEN_RIVER)
                //.addHabitat(BiomeKeys.JUNGLE)
                //.addHabitat(BiomeKeys.JUNGLE_HILLS)
                .build(makeId("ice"), context);
        SUNLIGHT = new DragonType.Builder(0xFFDE00)
                .addHabitat(Blocks.GLOWSTONE)
                .addHabitat(Blocks.JACK_O_LANTERN)
                .addHabitat(Blocks.SHROOMLIGHT)
                .addHabitat(Blocks.YELLOW_GLAZED_TERRACOTTA)
                .build(makeId("sunlight"), context);
        WATER = new DragonType.Builder(0x4F69A8)
                .addImmunity(DamageTypes.DROWN)
                .addImmunity(DamageTypes.MAGIC)
                .addImmunity(DamageTypes.HOT_FLOOR)
                .addImmunity(DamageTypes.LIGHTNING_BOLT)
                .addImmunity(DamageTypes.WITHER)
                .addHabitat(Blocks.WATER)
                .addHabitat(Biomes.OCEAN)
                .addHabitat(Biomes.RIVER)
                .build(makeId("water"), context);
        //modify context
        ENCHANT = new DragonType.Builder(0x8359AE)
                .addImmunity(DamageTypes.MAGIC)
                .addImmunity(DamageTypes.HOT_FLOOR)
                .addImmunity(DamageTypes.LIGHTNING_BOLT)
                .addImmunity(DamageTypes.WITHER)
                .addHabitat(Blocks.BOOKSHELF)
                .addHabitat(Blocks.ENCHANTING_TABLE)
                .build(makeId("enchant"), context.setEnchantmentValue(30));
        context.setDurabilityFactor(70)
                .setDefense(CHESTPLATE, 9)
                .setEnchantmentValue(11)
                .setToughness(9.0F);
        ENDER = new DragonType.Builder(0xAB39BE)
                .notConvertible()
                .putAttributeModifier(Attributes.MAX_HEALTH, BONUS_ID, 10.0D, AttributeModifier.Operation.ADD_VALUE)
                .addImmunity(DamageTypes.MAGIC)
                .addImmunity(DamageTypes.HOT_FLOOR)
                .addImmunity(DamageTypes.LIGHTNING_BOLT)
                .addImmunity(DamageTypes.WITHER)
                .setSneezeParticle(ParticleTypes.PORTAL)
                .setEggParticle(ParticleTypes.PORTAL)
                .build(DragonType.DEFAULT_KEY, context);
        SCULK = new DragonType.Builder(0x29DFEB)
                .notConvertible()
                .putAttributeModifier(Attributes.MAX_HEALTH, BONUS_ID, 10.0D, AttributeModifier.Operation.ADD_VALUE)
                .addImmunity(DamageTypes.MAGIC)
                .addImmunity(DamageTypes.HOT_FLOOR)
                .addImmunity(DamageTypes.LIGHTNING_BOLT)
                .addImmunity(DamageTypes.WITHER)
                .build(makeId("sculk"), context);
        //modify context
        NETHER = new DragonType.Builder(0xE5B81B)
                .putAttributeModifier(Attributes.MAX_HEALTH, BONUS_ID, 5.0D, AttributeModifier.Operation.ADD_VALUE)
                .addImmunity(DamageTypes.MAGIC)
                .addImmunity(DamageTypes.HOT_FLOOR)
                .addImmunity(DamageTypes.LIGHTNING_BOLT)
                .addImmunity(DamageTypes.WITHER)
                //.addHabitat(BiomeKeys.HELL)
                .setEggParticle(ParticleTypes.DRIPPING_LAVA)
                .build(makeId("nether"), context.setDurabilityFactor(55).setToughness(8.0F));
        //no context
        SKELETON = new DragonType.Builder(0xFFFFFF)
                .isSkeleton()
                .putAttributeModifier(Attributes.MAX_HEALTH, BONUS_ID, -15.0D, AttributeModifier.Operation.ADD_VALUE)
                .addImmunity(DamageTypes.LIGHTNING_BOLT)
                .addImmunity(DamageTypes.WITHER)
                .addHabitat(Blocks.BONE_BLOCK)
                .setEnvironmentPredicate(egg -> egg.getY() <= egg.level().getHeight() * 0.25 || egg.level().getLightEngine().getRawBrightness(egg.blockPosition(), 0) < 4)
                .build(makeId("skeleton"));
        WITHER = new DragonType.Builder(0x50260A)
                .notConvertible()
                .isSkeleton()
                .putAttributeModifier(Attributes.MAX_HEALTH, BONUS_ID, -10.0D, AttributeModifier.Operation.ADD_VALUE)
                .addImmunity(DamageTypes.MAGIC)
                .addImmunity(DamageTypes.HOT_FLOOR)
                .addImmunity(DamageTypes.LIGHTNING_BOLT)
                .addImmunity(DamageTypes.WITHER)
                .build(makeId("wither"));
    }
}
