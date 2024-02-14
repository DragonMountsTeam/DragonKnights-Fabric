package net.dragonmounts.init;

import net.dragonmounts.registry.DragonType;
import net.dragonmounts.registry.DragonVariant;
import net.dragonmounts.util.ImmutableArray;
import net.minecraft.block.AbstractBlock.Settings;

import static net.dragonmounts.DragonMounts.MOD_ID;
import static net.dragonmounts.init.DMItemGroups.block;
import static net.minecraft.block.Material.DECORATION;
import static net.minecraft.util.Rarity.UNCOMMON;

public class DragonVariants {
    public static final DragonVariant AETHER_FEMALE = create(DragonTypes.AETHER, "aether_female");
    public static final DragonVariant AETHER_MALE = create(DragonTypes.AETHER, "aether_male");
    public static final DragonVariant AETHER_NEW = create(DragonTypes.AETHER, "aether_new");
    public static final DragonVariant ENCHANT_FEMALE = create(DragonTypes.ENCHANT, "enchant_female");
    public static final DragonVariant ENCHANT_MALE = create(DragonTypes.ENCHANT, "enchant_male");
    public static final DragonVariant ENDER_FEMALE = create(DragonTypes.ENDER, DragonVariant.DEFAULT_KEY.getPath());
    public static final DragonVariant ENDER_MALE = create(DragonTypes.ENDER, "ender_male");
    public static final DragonVariant FIRE_FEMALE = create(DragonTypes.FIRE, "fire_female");
    public static final DragonVariant FIRE_MALE = create(DragonTypes.FIRE, "fire_male");
    public static final DragonVariant FOREST_FEMALE = create(DragonTypes.FOREST, "forest_female");
    public static final DragonVariant FOREST_MALE = create(DragonTypes.FOREST, "forest_male");
    public static final DragonVariant FOREST_DRY_FEMALE = create(DragonTypes.FOREST, "forest_dry_female");
    public static final DragonVariant FOREST_DRY_MALE = create(DragonTypes.FOREST, "forest_dry_male");
    public static final DragonVariant FOREST_TAIGA_FEMALE = create(DragonTypes.FOREST, "forest_taiga_female");
    public static final DragonVariant FOREST_TAIGA_MALE = create(DragonTypes.FOREST, "forest_taiga_male");
    public static final DragonVariant ICE_FEMALE = create(DragonTypes.ICE, "ice_female");
    public static final DragonVariant ICE_MALE = create(DragonTypes.ICE, "ice_male");
    public static final DragonVariant MOONLIGHT_FEMALE = create(DragonTypes.MOONLIGHT, "moonlight_female");
    public static final DragonVariant MOONLIGHT_MALE = create(DragonTypes.MOONLIGHT, "moonlight_male");
    public static final DragonVariant NETHER_FEMALE = create(DragonTypes.NETHER, "nether_female");
    public static final DragonVariant NETHER_MALE = create(DragonTypes.NETHER, "nether_male");
    public static final DragonVariant SCULK = create(DragonTypes.SCULK, "sculk");
    public static final DragonVariant SKELETON_FEMALE = create(DragonTypes.SKELETON, "skeleton_female");
    public static final DragonVariant SKELETON_MALE = create(DragonTypes.SKELETON, "skeleton_male");
    public static final DragonVariant STORM_FEMALE = create(DragonTypes.STORM, "storm_female");
    public static final DragonVariant STORM_MALE = create(DragonTypes.STORM, "storm_male");
    public static final DragonVariant SUNLIGHT_FEMALE = create(DragonTypes.SUNLIGHT, "sunlight_female");
    public static final DragonVariant SUNLIGHT_MALE = create(DragonTypes.SUNLIGHT, "sunlight_male");
    public static final DragonVariant TERRA_FEMALE = create(DragonTypes.TERRA, "terra_female");
    public static final DragonVariant TERRA_MALE = create(DragonTypes.TERRA, "terra_male");
    public static final DragonVariant WATER_FEMALE = create(DragonTypes.WATER, "water_female");
    public static final DragonVariant WATER_MALE = create(DragonTypes.WATER, "water_male");
    public static final DragonVariant WITHER_FEMALE = create(DragonTypes.WITHER, "wither_female");
    public static final DragonVariant WITHER_MALE = create(DragonTypes.WITHER, "wither_male");
    public static final DragonVariant ZOMBIE_FEMALE = create(DragonTypes.ZOMBIE, "zombie_female");
    public static final DragonVariant ZOMBIE_MALE = create(DragonTypes.ZOMBIE, "zombie_male");
    public static final ImmutableArray<DragonVariant> VALUES = new ImmutableArray<>(
            DragonVariant.class,
            AETHER_FEMALE,
            AETHER_MALE,
            AETHER_NEW,
            ENCHANT_FEMALE,
            ENCHANT_MALE,
            ENDER_FEMALE,
            ENDER_MALE,
            FIRE_FEMALE,
            FIRE_MALE,
            FOREST_DRY_FEMALE,
            FOREST_DRY_MALE,
            FOREST_TAIGA_FEMALE,
            FOREST_TAIGA_MALE,
            FOREST_FEMALE,
            FOREST_MALE,
            ICE_FEMALE,
            ICE_MALE,
            MOONLIGHT_FEMALE,
            MOONLIGHT_MALE,
            NETHER_FEMALE,
            NETHER_MALE,
            SCULK,
            SKELETON_FEMALE,
            SKELETON_MALE,
            STORM_FEMALE,
            STORM_MALE,
            SUNLIGHT_FEMALE,
            SUNLIGHT_MALE,
            TERRA_FEMALE,
            TERRA_MALE,
            WATER_FEMALE,
            WATER_MALE,
            WITHER_FEMALE,
            WITHER_MALE,
            ZOMBIE_FEMALE,
            ZOMBIE_MALE
    );

    public static void init() {}

    private static DragonVariant create(DragonType type, String name) {
        return new DragonVariant(type, MOD_ID, name, Settings.of(DECORATION).strength(1F), block().rarity(UNCOMMON));
    }
}
