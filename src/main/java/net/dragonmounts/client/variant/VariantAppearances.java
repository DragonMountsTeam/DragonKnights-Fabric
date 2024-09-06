package net.dragonmounts.client.variant;

import net.dragonmounts.init.DragonVariants;
import net.minecraft.resources.ResourceLocation;

import static net.dragonmounts.DragonMounts.makeId;
import static net.dragonmounts.client.variant.VariantAppearance.TEXTURES_ROOT;

public class VariantAppearances {
    public static AgeableAppearance createAgeableAppearance(ResourceLocation location, boolean hasTailHorns, boolean hasSideTailScale) {
        String path = location.getPath();
        return new AgeableAppearance(
                location.withPath(TEXTURES_ROOT + path + "/baby/body.png"),
                location.withPath(TEXTURES_ROOT + path + "/baby/glow.png"),
                location.withPath(TEXTURES_ROOT + path + "/body.png"),
                location.withPath(TEXTURES_ROOT + path + "/glow.png"),
                hasTailHorns,
                hasSideTailScale
        );
    }

    public static VariantAppearance createDefaultAppearance(ResourceLocation location, boolean hasTailHorns, boolean hasSideTailScale) {
        String path = location.getPath();
        return new DefaultAppearance(
                location.withPath(TEXTURES_ROOT + path + "/body.png"),
                location.withPath(TEXTURES_ROOT + path + "/glow.png"),
                hasTailHorns,
                hasSideTailScale
        );
    }

    public static final VariantAppearance AETHER_FEMALE;
    public static final VariantAppearance AETHER_MALE;
    public static final VariantAppearance AETHER_NEW;
    public static final VariantAppearance ENCHANT_FEMALE = createAgeableAppearance(makeId("enchant/female"), false, false);
    public static final VariantAppearance ENCHANT_MALE = createAgeableAppearance(makeId("enchant/male"), false, false);
    public static final VariantAppearance ENDER_FEMALE;
    public static final VariantAppearance ENDER_MALE;
    public static final VariantAppearance FIRE_FEMALE = createAgeableAppearance(makeId("fire/female"), false, false);
    public static final VariantAppearance FIRE_MALE = createAgeableAppearance(makeId("fire/male"), false, false);
    public static final VariantAppearance FOREST_FEMALE;
    public static final VariantAppearance FOREST_MALE;
    public static final VariantAppearance FOREST_DRY_FEMALE;
    public static final VariantAppearance FOREST_DRY_MALE;
    public static final VariantAppearance FOREST_TAIGA_FEMALE;
    public static final VariantAppearance FOREST_TAIGA_MALE;
    public static final VariantAppearance ICE_FEMALE;
    public static final VariantAppearance ICE_MALE;
    public static final VariantAppearance MOONLIGHT_FEMALE = createDefaultAppearance(makeId("moonlight/female"), false, false);
    public static final VariantAppearance MOONLIGHT_MALE = createDefaultAppearance(makeId("moonlight/male"), false, false);
    public static final VariantAppearance NETHER_FEMALE = createAgeableAppearance(makeId("nether/female"), false, false);
    public static final VariantAppearance NETHER_MALE = createAgeableAppearance(makeId("nether/male"), false, false);
    public static final VariantAppearance SKELETON_FEMALE;
    public static final VariantAppearance SKELETON_MALE;
    public static final VariantAppearance STORM_FEMALE;
    public static final VariantAppearance STORM_MALE = createAgeableAppearance(makeId("storm/male"), true, false);
    public static final VariantAppearance SUNLIGHT_FEMALE = createAgeableAppearance(makeId("sunlight/female"), false, false);
    public static final VariantAppearance SUNLIGHT_MALE = createAgeableAppearance(makeId("sunlight/male"), false, false);
    public static final VariantAppearance TERRA_FEMALE = createAgeableAppearance(makeId("terra/female"), false, false);
    public static final VariantAppearance TERRA_MALE = createAgeableAppearance(makeId("terra/male"), false, false);
    public static final VariantAppearance WATER_FEMALE = createAgeableAppearance(makeId("water/female"), true, false);
    public static final VariantAppearance WATER_MALE = createAgeableAppearance(makeId("water/male"), true, false);
    public static final VariantAppearance WITHER_FEMALE = createDefaultAppearance(makeId("wither/female"), true, false);
    public static final VariantAppearance WITHER_MALE = createDefaultAppearance(makeId("wither/male"), true, false);
    public static final VariantAppearance ZOMBIE_FEMALE;
    public static final VariantAppearance ZOMBIE_MALE;
    public static final VariantAppearance SCULK = createDefaultAppearance(makeId("sculk"), false, false);

    static {
        ResourceLocation babyGlow = makeId(TEXTURES_ROOT + "aether/baby_glow.png");
        ResourceLocation glow = makeId(TEXTURES_ROOT + "aether/glow.png");
        AETHER_FEMALE = new AgeableAppearance(makeId(TEXTURES_ROOT + "aether/female/baby_body.png"), babyGlow, makeId(TEXTURES_ROOT + "aether/female/body.png"), glow, false, false);
        AETHER_MALE = new AgeableAppearance(makeId(TEXTURES_ROOT + "aether/male/baby_body.png"), babyGlow, makeId(TEXTURES_ROOT + "aether/male/body.png"), glow, false, false);
        AETHER_NEW = new DefaultAppearance(makeId(TEXTURES_ROOT + "aether/new/body.png"), makeId(TEXTURES_ROOT + "aether/new/glow.png"), false, false);
    }

    static {
        ResourceLocation glow = makeId(TEXTURES_ROOT + "ender/glow.png");
        ENDER_FEMALE = new DefaultAppearance(makeId(TEXTURES_ROOT + "ender/female/body.png"), glow, false, false);
        ENDER_MALE = new AgeableAppearance(makeId(TEXTURES_ROOT + "ender/male/baby_body.png"), glow, makeId(TEXTURES_ROOT + "ender/male/body.png"), glow, false, false);
    }

    static {
        ResourceLocation glow = makeId(TEXTURES_ROOT + "forest/glow.png");
        ResourceLocation babyBody = makeId(TEXTURES_ROOT + "forest/forest/baby_body.png");
        FOREST_FEMALE = new AgeableAppearance(babyBody, glow, makeId(TEXTURES_ROOT + "forest/forest/female_body.png"), glow, false, false);
        FOREST_MALE = new AgeableAppearance(babyBody, glow, makeId(TEXTURES_ROOT + "forest/forest/male_body.png"), glow, false, false);
        babyBody = makeId(TEXTURES_ROOT + "forest/dry/baby_body.png");
        FOREST_DRY_FEMALE = new AgeableAppearance(babyBody, glow, makeId(TEXTURES_ROOT + "forest/dry/female_body.png"), glow, false, false);
        FOREST_DRY_MALE = new AgeableAppearance(babyBody, glow, makeId(TEXTURES_ROOT + "forest/dry/male_body.png"), glow, false, false);
        FOREST_TAIGA_FEMALE = new DefaultAppearance(makeId(TEXTURES_ROOT + "forest/taiga/female_body.png"), glow, false, false);
        FOREST_TAIGA_MALE = new DefaultAppearance(makeId(TEXTURES_ROOT + "forest/taiga/male_body.png"), glow, false, false);
    }

    static {
        ResourceLocation babyGlow = makeId(TEXTURES_ROOT + "ice/baby_glow.png");
        ResourceLocation maleBody = makeId(TEXTURES_ROOT + "ice/male/body.png");
        ICE_FEMALE = new AgeableAppearance(
                makeId(TEXTURES_ROOT + "ice/female/baby_body.png"),
                babyGlow,
                makeId(TEXTURES_ROOT + "ice/female/body.png"),
                makeId(TEXTURES_ROOT + "ice/female/glow.png"),
                false,
                true
        );
        ICE_MALE = new AgeableAppearance(maleBody, babyGlow, maleBody, makeId(TEXTURES_ROOT + "ice/male/glow.png"), false, true);
    }

    static {
        ResourceLocation glow = makeId(TEXTURES_ROOT + "skeleton/glow.png");
        SKELETON_FEMALE = new DefaultAppearance(makeId(TEXTURES_ROOT + "skeleton/female_body.png"), glow, false, false);
        SKELETON_MALE = new DefaultAppearance(makeId(TEXTURES_ROOT + "skeleton/male_body.png"), glow, false, false);
    }

    static {
        ResourceLocation body = makeId(TEXTURES_ROOT + "storm/female/body.png");
        STORM_FEMALE = new AgeableAppearance(body, makeId(TEXTURES_ROOT + "storm/female/baby_glow.png"), body, makeId(TEXTURES_ROOT + "storm/female/glow.png"), true, false);
    }

    static {
        ResourceLocation body = makeId(TEXTURES_ROOT + "zombie/body.png");
        ZOMBIE_FEMALE = new DefaultAppearance(body, makeId(TEXTURES_ROOT + "zombie/female_glow.png"), false, false);
        ZOMBIE_MALE = new DefaultAppearance(body, makeId(TEXTURES_ROOT + "zombie/male_glow.png"), false, false);
    }

    public static void bindAppearance() {
        DragonVariants.AETHER_FEMALE.setAppearance(AETHER_FEMALE);
        DragonVariants.AETHER_MALE.setAppearance(AETHER_MALE);
        DragonVariants.AETHER_NEW.setAppearance(AETHER_NEW);
        DragonVariants.ENCHANT_FEMALE.setAppearance(ENCHANT_FEMALE);
        DragonVariants.ENCHANT_MALE.setAppearance(ENCHANT_MALE);
        DragonVariants.ENDER_FEMALE.setAppearance(ENDER_FEMALE);
        DragonVariants.ENDER_MALE.setAppearance(ENDER_MALE);
        DragonVariants.FIRE_FEMALE.setAppearance(FIRE_FEMALE);
        DragonVariants.FIRE_MALE.setAppearance(FIRE_MALE);
        DragonVariants.FOREST_FEMALE.setAppearance(FOREST_FEMALE);
        DragonVariants.FOREST_MALE.setAppearance(FOREST_MALE);
        DragonVariants.FOREST_DRY_FEMALE.setAppearance(FOREST_DRY_FEMALE);
        DragonVariants.FOREST_DRY_MALE.setAppearance(FOREST_DRY_MALE);
        DragonVariants.FOREST_TAIGA_FEMALE.setAppearance(FOREST_TAIGA_FEMALE);
        DragonVariants.FOREST_TAIGA_MALE.setAppearance(FOREST_TAIGA_MALE);
        DragonVariants.ICE_FEMALE.setAppearance(ICE_FEMALE);
        DragonVariants.ICE_MALE.setAppearance(ICE_MALE);
        DragonVariants.MOONLIGHT_FEMALE.setAppearance(MOONLIGHT_FEMALE);
        DragonVariants.MOONLIGHT_MALE.setAppearance(MOONLIGHT_MALE);
        DragonVariants.NETHER_FEMALE.setAppearance(NETHER_FEMALE);
        DragonVariants.NETHER_MALE.setAppearance(NETHER_MALE);
        DragonVariants.SCULK.setAppearance(SCULK);
        DragonVariants.SKELETON_FEMALE.setAppearance(SKELETON_FEMALE);
        DragonVariants.SKELETON_MALE.setAppearance(SKELETON_MALE);
        DragonVariants.STORM_FEMALE.setAppearance(STORM_FEMALE);
        DragonVariants.STORM_MALE.setAppearance(STORM_MALE);
        DragonVariants.SUNLIGHT_FEMALE.setAppearance(SUNLIGHT_FEMALE);
        DragonVariants.SUNLIGHT_MALE.setAppearance(SUNLIGHT_MALE);
        DragonVariants.TERRA_FEMALE.setAppearance(TERRA_FEMALE);
        DragonVariants.TERRA_MALE.setAppearance(TERRA_MALE);
        DragonVariants.WATER_FEMALE.setAppearance(WATER_FEMALE);
        DragonVariants.WATER_MALE.setAppearance(WATER_MALE);
        DragonVariants.WITHER_FEMALE.setAppearance(WITHER_FEMALE);
        DragonVariants.WITHER_MALE.setAppearance(WITHER_MALE);
        DragonVariants.ZOMBIE_FEMALE.setAppearance(ZOMBIE_FEMALE);
        DragonVariants.ZOMBIE_MALE.setAppearance(ZOMBIE_MALE);
    }
}
