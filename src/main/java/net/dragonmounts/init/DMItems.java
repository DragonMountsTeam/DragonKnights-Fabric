package net.dragonmounts.init;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dragonmounts.api.DragonScaleArmorSuit;
import net.dragonmounts.api.DragonScaleTier;
import net.dragonmounts.api.IDragonScaleArmorEffect;
import net.dragonmounts.item.*;
import net.dragonmounts.registry.DragonType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.List;
import java.util.function.Consumer;

import static net.dragonmounts.DragonMounts.makeId;

public class DMItems {
    private static final ObjectArrayList<Item> ITEMS = new ObjectArrayList<>(32);
    private static final ObjectArrayList<Item> TOOLS = new ObjectArrayList<>(80);
    private static final ObjectArrayList<SpawnEggItem> SPAWN_EGGS = new ObjectArrayList<>(16);
    public static final DragonScalesItem AETHER_DRAGON_SCALES = createDragonScales("aether_dragon_scales", DragonTypes.AETHER, new Properties());
    public static final DragonScalesItem ENCHANT_DRAGON_SCALES = createDragonScales("enchant_dragon_scales", DragonTypes.ENCHANT, new Properties());
    public static final DragonScalesItem ENDER_DRAGON_SCALES = createDragonScales("ender_dragon_scales", DragonTypes.ENDER, new Properties());
    public static final DragonScalesItem FIRE_DRAGON_SCALES = createDragonScales("fire_dragon_scales", DragonTypes.FIRE, new Properties());
    public static final DragonScalesItem FOREST_DRAGON_SCALES = createDragonScales("forest_dragon_scales", DragonTypes.FOREST, new Properties());
    public static final DragonScalesItem ICE_DRAGON_SCALES = createDragonScales("ice_dragon_scales", DragonTypes.ICE, new Properties());
    public static final DragonScalesItem MOONLIGHT_DRAGON_SCALES = createDragonScales("moonlight_dragon_scales", DragonTypes.MOONLIGHT, new Properties());
    public static final DragonScalesItem NETHER_DRAGON_SCALES = createDragonScales("nether_dragon_scales", DragonTypes.NETHER, new Properties());
    public static final DragonScalesItem SCULK_DRAGON_SCALES = createDragonScales("sculk_dragon_scales", DragonTypes.SCULK, new Properties().fireResistant());
    public static final DragonScalesItem STORM_DRAGON_SCALES = createDragonScales("storm_dragon_scales", DragonTypes.STORM, new Properties());
    public static final DragonScalesItem SUNLIGHT_DRAGON_SCALES = createDragonScales("sunlight_dragon_scales", DragonTypes.SUNLIGHT, new Properties());
    public static final DragonScalesItem TERRA_DRAGON_SCALES = createDragonScales("terra_dragon_scales", DragonTypes.TERRA, new Properties());
    public static final DragonScalesItem WATER_DRAGON_SCALES = createDragonScales("water_dragon_scales", DragonTypes.WATER, new Properties());
    public static final DragonScalesItem ZOMBIE_DRAGON_SCALES = createDragonScales("zombie_dragon_scales", DragonTypes.ZOMBIE, new Properties());
    //Dragon Armor
    public static final DragonArmorItem IRON_DRAGON_ARMOR = createDragonArmor("iron_dragon_armor", ArmorMaterials.IRON, DragonArmorItem.TEXTURE_PREFIX + "iron.png", new Properties().stacksTo(1));
    public static final DragonArmorItem GOLDEN_DRAGON_ARMOR = createDragonArmor("golden_dragon_armor", ArmorMaterials.GOLD, DragonArmorItem.TEXTURE_PREFIX + "gold.png", new Properties().stacksTo(1));
    public static final DragonArmorItem DIAMOND_DRAGON_ARMOR = createDragonArmor("diamond_dragon_armor", DragonArmorItem.DIAMOND, DragonArmorItem.TEXTURE_PREFIX + "diamond.png", new Properties().stacksTo(1));
    public static final DragonArmorItem EMERALD_DRAGON_ARMOR = createDragonArmor("emerald_dragon_armor", DragonArmorItem.DIAMOND, DragonArmorItem.TEXTURE_PREFIX + "emerald.png", new Properties().stacksTo(1));
    public static final DragonArmorItem NETHERITE_DRAGON_ARMOR = createDragonArmor("netherite_dragon_armor", DragonArmorItem.NETHERITE, DragonArmorItem.TEXTURE_PREFIX + "netherite.png", new Properties().stacksTo(1).fireResistant());
    //Dragon Scale Swords
    public static final DragonScaleSwordItem AETHER_DRAGON_SCALE_SWORD = createDragonScaleSword("aether_dragon_sword", DragonScaleTier.AETHER, new Properties());
    public static final DragonScaleSwordItem WATER_DRAGON_SCALE_SWORD = createDragonScaleSword("water_dragon_sword", DragonScaleTier.WATER, new Properties());
    public static final DragonScaleSwordItem ICE_DRAGON_SCALE_SWORD = createDragonScaleSword("ice_dragon_sword", DragonScaleTier.ICE, new Properties());
    public static final DragonScaleSwordItem FIRE_DRAGON_SCALE_SWORD = createDragonScaleSword("fire_dragon_sword", DragonScaleTier.FIRE, new Properties());
    public static final DragonScaleSwordItem FOREST_DRAGON_SCALE_SWORD = createDragonScaleSword("forest_dragon_sword", DragonScaleTier.FOREST, new Properties());
    public static final DragonScaleSwordItem NETHER_DRAGON_SCALE_SWORD = createDragonScaleSword("nether_dragon_sword", DragonScaleTier.NETHER, new Properties());
    public static final DragonScaleSwordItem ENDER_DRAGON_SCALE_SWORD = createDragonScaleSword("ender_dragon_sword", DragonScaleTier.ENDER, new Properties());
    public static final DragonScaleSwordItem ENCHANT_DRAGON_SCALE_SWORD = createDragonScaleSword("enchant_dragon_sword", DragonScaleTier.ENCHANT, new Properties());
    public static final DragonScaleSwordItem SUNLIGHT_DRAGON_SCALE_SWORD = createDragonScaleSword("sunlight_dragon_sword", DragonScaleTier.SUNLIGHT, new Properties());
    public static final DragonScaleSwordItem MOONLIGHT_DRAGON_SCALE_SWORD = createDragonScaleSword("moonlight_dragon_sword", DragonScaleTier.MOONLIGHT, new Properties());
    public static final DragonScaleSwordItem STORM_DRAGON_SCALE_SWORD = createDragonScaleSword("storm_dragon_sword", DragonScaleTier.STORM, new Properties());
    public static final DragonScaleSwordItem TERRA_DRAGON_SCALE_SWORD = createDragonScaleSword("terra_dragon_sword", DragonScaleTier.TERRA, new Properties());
    public static final DragonScaleSwordItem ZOMBIE_DRAGON_SCALE_SWORD = createDragonScaleSword("zombie_dragon_sword", DragonScaleTier.ZOMBIE, new Properties());
    public static final DragonScaleSwordItem SCULK_DRAGON_SCALE_SWORD = createDragonScaleSword("sculk_dragon_sword", DragonScaleTier.SCULK, new Properties().fireResistant());
    //Dragon Scale Axes
    public static final DragonScaleAxeItem AETHER_DRAGON_SCALE_AXE = createDragonScaleAxe("aether_dragon_axe", DragonScaleTier.AETHER, new Properties());
    public static final DragonScaleAxeItem WATER_DRAGON_SCALE_AXE = createDragonScaleAxe("water_dragon_axe", DragonScaleTier.WATER, new Properties());
    public static final DragonScaleAxeItem ICE_DRAGON_SCALE_AXE = createDragonScaleAxe("ice_dragon_axe", DragonScaleTier.ICE, new Properties());
    public static final DragonScaleAxeItem FIRE_DRAGON_SCALE_AXE = createDragonScaleAxe("fire_dragon_axe", DragonScaleTier.FIRE, new Properties());
    public static final DragonScaleAxeItem FOREST_DRAGON_SCALE_AXE = createDragonScaleAxe("forest_dragon_axe", DragonScaleTier.FOREST, new Properties());
    public static final DragonScaleAxeItem NETHER_DRAGON_SCALE_AXE = createDragonScaleAxe("nether_dragon_axe", DragonScaleTier.NETHER, 6.0F, -2.9F, new Properties());
    public static final DragonScaleAxeItem ENDER_DRAGON_SCALE_AXE = createDragonScaleAxe("ender_dragon_axe", DragonScaleTier.ENDER, 6.0F, -2.9F, new Properties());
    public static final DragonScaleAxeItem ENCHANT_DRAGON_SCALE_AXE = createDragonScaleAxe("enchant_dragon_axe", DragonScaleTier.ENCHANT, new Properties());
    public static final DragonScaleAxeItem SUNLIGHT_DRAGON_SCALE_AXE = createDragonScaleAxe("sunlight_dragon_axe", DragonScaleTier.SUNLIGHT, new Properties());
    public static final DragonScaleAxeItem MOONLIGHT_DRAGON_SCALE_AXE = createDragonScaleAxe("moonlight_dragon_axe", DragonScaleTier.MOONLIGHT, new Properties());
    public static final DragonScaleAxeItem STORM_DRAGON_SCALE_AXE = createDragonScaleAxe("storm_dragon_axe", DragonScaleTier.STORM, new Properties());
    public static final DragonScaleAxeItem TERRA_DRAGON_SCALE_AXE = createDragonScaleAxe("terra_dragon_axe", DragonScaleTier.TERRA, new Properties());
    public static final DragonScaleAxeItem ZOMBIE_DRAGON_SCALE_AXE = createDragonScaleAxe("zombie_dragon_axe", DragonScaleTier.ZOMBIE, new Properties());
    public static final DragonScaleAxeItem SCULK_DRAGON_SCALE_AXE = createDragonScaleAxe("sculk_dragon_axe", DragonScaleTier.SCULK, new Properties().fireResistant());
    //Dragon Scale Bows
    public static final DragonScaleBowItem AETHER_DRAGON_SCALE_BOW = createDragonScaleBow("aether_dragon_scale_bow", DragonScaleTier.AETHER, new Properties());
    public static final DragonScaleBowItem WATER_DRAGON_SCALE_BOW = createDragonScaleBow("water_dragon_scale_bow", DragonScaleTier.WATER, new Properties());
    public static final DragonScaleBowItem ICE_DRAGON_SCALE_BOW = createDragonScaleBow("ice_dragon_scale_bow", DragonScaleTier.ICE, new Properties());
    public static final DragonScaleBowItem FIRE_DRAGON_SCALE_BOW = createDragonScaleBow("fire_dragon_scale_bow", DragonScaleTier.FIRE, new Properties());
    public static final DragonScaleBowItem FOREST_DRAGON_SCALE_BOW = createDragonScaleBow("forest_dragon_scale_bow", DragonScaleTier.FOREST, new Properties());
    public static final DragonScaleBowItem NETHER_DRAGON_SCALE_BOW = createDragonScaleBow("nether_dragon_scale_bow", DragonScaleTier.NETHER, new Properties());
    public static final DragonScaleBowItem ENDER_DRAGON_SCALE_BOW = createDragonScaleBow("ender_dragon_scale_bow", DragonScaleTier.ENDER, new Properties());
    public static final DragonScaleBowItem ENCHANT_DRAGON_SCALE_BOW = createDragonScaleBow("enchant_dragon_scale_bow", DragonScaleTier.ENCHANT, new Properties());
    public static final DragonScaleBowItem SUNLIGHT_DRAGON_SCALE_BOW = createDragonScaleBow("sunlight_dragon_scale_bow", DragonScaleTier.SUNLIGHT, new Properties());
    public static final DragonScaleBowItem MOONLIGHT_DRAGON_SCALE_BOW = createDragonScaleBow("moonlight_dragon_scale_bow", DragonScaleTier.MOONLIGHT, new Properties());
    public static final DragonScaleBowItem STORM_DRAGON_SCALE_BOW = createDragonScaleBow("storm_dragon_scale_bow", DragonScaleTier.STORM, new Properties());
    public static final DragonScaleBowItem TERRA_DRAGON_SCALE_BOW = createDragonScaleBow("terra_dragon_scale_bow", DragonScaleTier.TERRA, new Properties());
    public static final DragonScaleBowItem ZOMBIE_DRAGON_SCALE_BOW = createDragonScaleBow("zombie_dragon_scale_bow", DragonScaleTier.ZOMBIE, new Properties());
    public static final DragonScaleBowItem SCULK_DRAGON_SCALE_BOW = createDragonScaleBow("sculk_dragon_scale_bow", DragonScaleTier.SCULK, new Properties().fireResistant());
    //Dragon Scale Shields
    public static final DragonScaleShieldItem AETHER_DRAGON_SCALE_SHIELD = createDragonScaleShield("aether_dragon_scale_shield", DragonTypes.AETHER, new Properties());
    public static final DragonScaleShieldItem WATER_DRAGON_SCALE_SHIELD = createDragonScaleShield("water_dragon_scale_shield", DragonTypes.WATER, new Properties());
    public static final DragonScaleShieldItem ICE_DRAGON_SCALE_SHIELD = createDragonScaleShield("ice_dragon_scale_shield", DragonTypes.ICE, new Properties());
    public static final DragonScaleShieldItem FIRE_DRAGON_SCALE_SHIELD = createDragonScaleShield("fire_dragon_scale_shield", DragonTypes.FIRE, new Properties());
    public static final DragonScaleShieldItem FOREST_DRAGON_SCALE_SHIELD = createDragonScaleShield("forest_dragon_scale_shield", DragonTypes.FOREST, new Properties());
    public static final DragonScaleShieldItem NETHER_DRAGON_SCALE_SHIELD = createDragonScaleShield("nether_dragon_scale_shield", DragonTypes.NETHER, new Properties());
    public static final DragonScaleShieldItem ENDER_DRAGON_SCALE_SHIELD = createDragonScaleShield("ender_dragon_scale_shield", DragonTypes.ENDER, new Properties());
    public static final DragonScaleShieldItem ENCHANT_DRAGON_SCALE_SHIELD = createDragonScaleShield("enchant_dragon_scale_shield", DragonTypes.ENCHANT, new Properties());
    public static final DragonScaleShieldItem SUNLIGHT_DRAGON_SCALE_SHIELD = createDragonScaleShield("sunlight_dragon_scale_shield", DragonTypes.SUNLIGHT, new Properties());
    public static final DragonScaleShieldItem MOONLIGHT_DRAGON_SCALE_SHIELD = createDragonScaleShield("moonlight_dragon_scale_shield", DragonTypes.MOONLIGHT, new Properties());
    public static final DragonScaleShieldItem STORM_DRAGON_SCALE_SHIELD = createDragonScaleShield("storm_dragon_scale_shield", DragonTypes.STORM, new Properties());
    public static final DragonScaleShieldItem TERRA_DRAGON_SCALE_SHIELD = createDragonScaleShield("terra_dragon_scale_shield", DragonTypes.TERRA, new Properties());
    public static final DragonScaleShieldItem ZOMBIE_DRAGON_SCALE_SHIELD = createDragonScaleShield("zombie_dragon_scale_shield", DragonTypes.ZOMBIE, new Properties());
    public static final DragonScaleShieldItem SCULK_DRAGON_SCALE_SHIELD = createDragonScaleShield("sculk_dragon_scale_shield", DragonTypes.SCULK, new Properties().fireResistant());
    //Dragon Scale Tools - Aether
    public static final DragonScaleShovelItem AETHER_DRAGON_SCALE_SHOVEL = createDragonScaleShovel("aether_dragon_shovel", DragonScaleTier.AETHER, new Properties());
    public static final DragonScalePickaxeItem AETHER_DRAGON_SCALE_PICKAXE = createDragonScalePickaxe("aether_dragon_pickaxe", DragonScaleTier.AETHER, new Properties());
    public static final DragonScaleHoeItem AETHER_DRAGON_SCALE_HOE = createDragonScaleHoe("aether_dragon_hoe", DragonScaleTier.AETHER, new Properties());
    //Dragon Scale Tools - Water
    public static final DragonScaleShovelItem WATER_DRAGON_SCALE_SHOVEL = createDragonScaleShovel("water_dragon_shovel", DragonScaleTier.WATER, new Properties());
    public static final DragonScalePickaxeItem WATER_DRAGON_SCALE_PICKAXE = createDragonScalePickaxe("water_dragon_pickaxe", DragonScaleTier.WATER, new Properties());
    public static final DragonScaleHoeItem WATER_DRAGON_SCALE_HOE = createDragonScaleHoe("water_dragon_hoe", DragonScaleTier.WATER, new Properties());
    //Dragon Scale Tools - Ice
    public static final DragonScaleShovelItem ICE_DRAGON_SCALE_SHOVEL = createDragonScaleShovel("ice_dragon_shovel", DragonScaleTier.ICE, new Properties());
    public static final DragonScalePickaxeItem ICE_DRAGON_SCALE_PICKAXE = createDragonScalePickaxe("ice_dragon_pickaxe", DragonScaleTier.ICE, new Properties());
    public static final DragonScaleHoeItem ICE_DRAGON_SCALE_HOE = createDragonScaleHoe("ice_dragon_hoe", DragonScaleTier.ICE, new Properties());
    //Dragon Scale Tools - Fire
    public static final DragonScaleShovelItem FIRE_DRAGON_SCALE_SHOVEL = createDragonScaleShovel("fire_dragon_shovel", DragonScaleTier.FIRE, new Properties());
    public static final DragonScalePickaxeItem FIRE_DRAGON_SCALE_PICKAXE = createDragonScalePickaxe("fire_dragon_pickaxe", DragonScaleTier.FIRE, new Properties());
    public static final DragonScaleHoeItem FIRE_DRAGON_SCALE_HOE = createDragonScaleHoe("fire_dragon_hoe", DragonScaleTier.FIRE, new Properties());
    //Dragon Scale Tools - Forest
    public static final DragonScaleShovelItem FOREST_DRAGON_SCALE_SHOVEL = createDragonScaleShovel("forest_dragon_shovel", DragonScaleTier.FOREST, new Properties());
    public static final DragonScalePickaxeItem FOREST_DRAGON_SCALE_PICKAXE = createDragonScalePickaxe("forest_dragon_pickaxe", DragonScaleTier.FOREST, new Properties());
    public static final DragonScaleHoeItem FOREST_DRAGON_SCALE_HOE = createDragonScaleHoe("forest_dragon_hoe", DragonScaleTier.FOREST, new Properties());
    //Dragon Scale Tools - Nether
    public static final DragonScaleShovelItem NETHER_DRAGON_SCALE_SHOVEL = createDragonScaleShovel("nether_dragon_shovel", DragonScaleTier.NETHER, new Properties());
    public static final DragonScalePickaxeItem NETHER_DRAGON_SCALE_PICKAXE = createDragonScalePickaxe("nether_dragon_pickaxe", DragonScaleTier.NETHER, new Properties());
    public static final DragonScaleHoeItem NETHER_DRAGON_SCALE_HOE = createDragonScaleHoe("nether_dragon_hoe", DragonScaleTier.NETHER, new Properties());
    //Dragon Scale Tools - Ender
    public static final DragonScaleShovelItem ENDER_DRAGON_SCALE_SHOVEL = createDragonScaleShovel("ender_dragon_shovel", DragonScaleTier.ENDER, new Properties());
    public static final DragonScalePickaxeItem ENDER_DRAGON_SCALE_PICKAXE = createDragonScalePickaxe("ender_dragon_pickaxe", DragonScaleTier.ENDER, new Properties());
    public static final DragonScaleHoeItem ENDER_DRAGON_SCALE_HOE = createDragonScaleHoe("ender_dragon_hoe", DragonScaleTier.ENDER, new Properties());
    //Dragon Scale Tools - Enchant
    public static final DragonScaleShovelItem ENCHANT_DRAGON_SCALE_SHOVEL = createDragonScaleShovel("enchant_dragon_shovel", DragonScaleTier.ENCHANT, new Properties());
    public static final DragonScalePickaxeItem ENCHANT_DRAGON_SCALE_PICKAXE = createDragonScalePickaxe("enchant_dragon_pickaxe", DragonScaleTier.ENCHANT, new Properties());
    public static final DragonScaleHoeItem ENCHANT_DRAGON_SCALE_HOE = createDragonScaleHoe("enchant_dragon_hoe", DragonScaleTier.ENCHANT, new Properties());
    //Dragon Scale Tools - Sunlight
    public static final DragonScaleShovelItem SUNLIGHT_DRAGON_SCALE_SHOVEL = createDragonScaleShovel("sunlight_dragon_shovel", DragonScaleTier.SUNLIGHT, new Properties());
    public static final DragonScalePickaxeItem SUNLIGHT_DRAGON_SCALE_PICKAXE = createDragonScalePickaxe("sunlight_dragon_pickaxe", DragonScaleTier.SUNLIGHT, new Properties());
    public static final DragonScaleHoeItem SUNLIGHT_DRAGON_SCALE_HOE = createDragonScaleHoe("sunlight_dragon_hoe", DragonScaleTier.SUNLIGHT, new Properties());
    //Dragon Scale Tools - Moonlight
    public static final DragonScaleShovelItem MOONLIGHT_DRAGON_SCALE_SHOVEL = createDragonScaleShovel("moonlight_dragon_shovel", DragonScaleTier.MOONLIGHT, new Properties());
    public static final DragonScalePickaxeItem MOONLIGHT_DRAGON_SCALE_PICKAXE = createDragonScalePickaxe("moonlight_dragon_pickaxe", DragonScaleTier.MOONLIGHT, new Properties());
    public static final DragonScaleHoeItem MOONLIGHT_DRAGON_SCALE_HOE = createDragonScaleHoe("moonlight_dragon_hoe", DragonScaleTier.MOONLIGHT, new Properties());
    //Dragon Scale Tools - Storm
    public static final DragonScaleShovelItem STORM_DRAGON_SCALE_SHOVEL = createDragonScaleShovel("storm_dragon_shovel", DragonScaleTier.STORM, new Properties());
    public static final DragonScalePickaxeItem STORM_DRAGON_SCALE_PICKAXE = createDragonScalePickaxe("storm_dragon_pickaxe", DragonScaleTier.STORM, new Properties());
    public static final DragonScaleHoeItem STORM_DRAGON_SCALE_HOE = createDragonScaleHoe("storm_dragon_hoe", DragonScaleTier.STORM, new Properties());
    //Dragon Scale Tools - Terra
    public static final DragonScaleShovelItem TERRA_DRAGON_SCALE_SHOVEL = createDragonScaleShovel("terra_dragon_shovel", DragonScaleTier.TERRA, new Properties());
    public static final DragonScalePickaxeItem TERRA_DRAGON_SCALE_PICKAXE = createDragonScalePickaxe("terra_dragon_pickaxe", DragonScaleTier.TERRA, new Properties());
    public static final DragonScaleHoeItem TERRA_DRAGON_SCALE_HOE = createDragonScaleHoe("terra_dragon_hoe", DragonScaleTier.TERRA, new Properties());
    //Dragon Scale Tools - Zombie
    public static final DragonScaleShovelItem ZOMBIE_DRAGON_SCALE_SHOVEL = createDragonScaleShovel("zombie_dragon_shovel", DragonScaleTier.ZOMBIE, new Properties());
    public static final DragonScalePickaxeItem ZOMBIE_DRAGON_SCALE_PICKAXE = createDragonScalePickaxe("zombie_dragon_pickaxe", DragonScaleTier.ZOMBIE, new Properties());
    public static final DragonScaleHoeItem ZOMBIE_DRAGON_SCALE_HOE = createDragonScaleHoe("zombie_dragon_hoe", DragonScaleTier.ZOMBIE, new Properties());
    //Dragon Scale Tools - Sculk
    public static final DragonScaleShovelItem SCULK_DRAGON_SCALE_SHOVEL = createDragonScaleShovel("sculk_dragon_shovel", DragonScaleTier.SCULK, new Properties().fireResistant());
    public static final DragonScalePickaxeItem SCULK_DRAGON_SCALE_PICKAXE = createDragonScalePickaxe("sculk_dragon_pickaxe", DragonScaleTier.SCULK, new Properties().fireResistant());
    public static final DragonScaleHoeItem SCULK_DRAGON_SCALE_HOE = createDragonScaleHoe("sculk_dragon_hoe", DragonScaleTier.SCULK, new Properties().fireResistant());
    //Dragon Scale Armors
    public static final DragonScaleArmorSuit AETHER_DRAGON_SCALE_ARMORS = createDragonScaleArmors(
            "aether_dragon_scale_helmet",
            "aether_dragon_scale_chestplate",
            "aether_dragon_scale_leggings",
            "aether_dragon_scale_boots",
            DragonTypes.AETHER,
            DMArmorEffects.AETHER,
            new Properties()
    );
    public static final DragonScaleArmorSuit WATER_DRAGON_SCALE_ARMORS = createDragonScaleArmors(
            "water_dragon_scale_helmet",
            "water_dragon_scale_chestplate",
            "water_dragon_scale_leggings",
            "water_dragon_scale_boots",
            DragonTypes.WATER,
            DMArmorEffects.WATER,
            new Properties()
    );
    public static final DragonScaleArmorSuit ICE_DRAGON_SCALE_ARMORS = createDragonScaleArmors(
            "ice_dragon_scale_helmet",
            "ice_dragon_scale_chestplate",
            "ice_dragon_scale_leggings",
            "ice_dragon_scale_boots",
            DragonTypes.ICE,
            DMArmorEffects.ICE,
            new Properties()
    );
    public static final DragonScaleArmorSuit FIRE_DRAGON_SCALE_ARMORS = createDragonScaleArmors(
            "fire_dragon_scale_helmet",
            "fire_dragon_scale_chestplate",
            "fire_dragon_scale_leggings",
            "fire_dragon_scale_boots",
            DragonTypes.FIRE,
            DMArmorEffects.FIRE,
            new Properties()
    );
    public static final DragonScaleArmorSuit FOREST_DRAGON_SCALE_ARMORS = createDragonScaleArmors(
            "forest_dragon_scale_helmet",
            "forest_dragon_scale_chestplate",
            "forest_dragon_scale_leggings",
            "forest_dragon_scale_boots",
            DragonTypes.FOREST,
            DMArmorEffects.FOREST,
            new Properties()
    );
    public static final DragonScaleArmorSuit NETHER_DRAGON_SCALE_ARMORS = createDragonScaleArmors(
            "nether_dragon_scale_helmet",
            "nether_dragon_scale_chestplate",
            "nether_dragon_scale_leggings",
            "nether_dragon_scale_boots",
            DragonTypes.NETHER,
            DMArmorEffects.NETHER,
            new Properties()
    );
    public static final DragonScaleArmorSuit ENDER_DRAGON_SCALE_ARMORS = createDragonScaleArmors(
            "ender_dragon_scale_helmet",
            "ender_dragon_scale_chestplate",
            "ender_dragon_scale_leggings",
            "ender_dragon_scale_boots",
            DragonTypes.ENDER,
            DMArmorEffects.ENDER,
            new Properties()
    );
    public static final DragonScaleArmorSuit ENCHANT_DRAGON_SCALE_ARMORS = createDragonScaleArmors(
            "enchant_dragon_scale_helmet",
            "enchant_dragon_scale_chestplate",
            "enchant_dragon_scale_leggings",
            "enchant_dragon_scale_boots",
            DragonTypes.ENCHANT,
            DMArmorEffects.ENCHANT,
            new Properties()
    );
    public static final DragonScaleArmorSuit SUNLIGHT_DRAGON_SCALE_ARMORS = createDragonScaleArmors(
            "sunlight_dragon_scale_helmet",
            "sunlight_dragon_scale_chestplate",
            "sunlight_dragon_scale_leggings",
            "sunlight_dragon_scale_boots",
            DragonTypes.SUNLIGHT,
            DMArmorEffects.SUNLIGHT,
            new Properties()
    );
    public static final DragonScaleArmorSuit MOONLIGHT_DRAGON_SCALE_ARMORS = createDragonScaleArmors(
            "moonlight_dragon_scale_helmet",
            "moonlight_dragon_scale_chestplate",
            "moonlight_dragon_scale_leggings",
            "moonlight_dragon_scale_boots",
            DragonTypes.MOONLIGHT,
            DMArmorEffects.MOONLIGHT,
            new Properties()
    );
    public static final DragonScaleArmorSuit STORM_DRAGON_SCALE_ARMORS = createDragonScaleArmors(
            "storm_dragon_scale_helmet",
            "storm_dragon_scale_chestplate",
            "storm_dragon_scale_leggings",
            "storm_dragon_scale_boots",
            DragonTypes.STORM,
            DMArmorEffects.STORM,
            new Properties()
    );
    public static final DragonScaleArmorSuit TERRA_DRAGON_SCALE_ARMORS = createDragonScaleArmors(
            "terra_dragon_scale_helmet",
            "terra_dragon_scale_chestplate",
            "terra_dragon_scale_leggings",
            "terra_dragon_scale_boots",
            DragonTypes.TERRA,
            DMArmorEffects.TERRA,
            new Properties()
    );
    public static final DragonScaleArmorSuit ZOMBIE_DRAGON_SCALE_ARMORS = createDragonScaleArmors(
            "zombie_dragon_scale_helmet",
            "zombie_dragon_scale_chestplate",
            "zombie_dragon_scale_leggings",
            "zombie_dragon_scale_boots",
            DragonTypes.ZOMBIE,
            DMArmorEffects.ZOMBIE,
            new Properties()
    );
    public static final DragonScaleArmorSuit SCULK_DRAGON_SCALE_ARMORS = createDragonScaleArmors(
            "sculk_dragon_scale_helmet",
            "sculk_dragon_scale_chestplate",
            "sculk_dragon_scale_leggings",
            "sculk_dragon_scale_boots",
            DragonTypes.SCULK,
            null,
            new Properties().fireResistant()
    );
    //Dragon Spawn Eggs
    public static final DragonSpawnEggItem AETHER_DRAGON_SPAWN_EGG = createDragonSpawnEgg("aether_dragon_spawn_egg", DragonTypes.AETHER, 0x05C3D2, 0x281EE8, new Properties());
    public static final DragonSpawnEggItem ENCHANT_DRAGON_SPAWN_EGG = createDragonSpawnEgg("enchant_dragon_spawn_egg", DragonTypes.ENCHANT, 0xCC0DE0, 0xFFFFFF, new Properties());
    public static final DragonSpawnEggItem ENDER_DRAGON_SPAWN_EGG = createDragonSpawnEgg("ender_dragon_spawn_egg", DragonTypes.ENDER, 0x08080C, 0x79087E, new Properties());
    public static final DragonSpawnEggItem FIRE_DRAGON_SPAWN_EGG = createDragonSpawnEgg("fire_dragon_spawn_egg", DragonTypes.FIRE, 0x620508, 0xF7A502, new Properties());
    public static final DragonSpawnEggItem FOREST_DRAGON_SPAWN_EGG = createDragonSpawnEgg("forest_dragon_spawn_egg", DragonTypes.FOREST, 0x0C9613, 0x036408, new Properties());
    public static final DragonSpawnEggItem ICE_DRAGON_SPAWN_EGG = createDragonSpawnEgg("ice_dragon_spawn_egg", DragonTypes.ICE, 0xFFFFFF, 0x02D0EE, new Properties());
    public static final DragonSpawnEggItem MOONLIGHT_DRAGON_SPAWN_EGG = createDragonSpawnEgg("moonlight_dragon_spawn_egg", DragonTypes.MOONLIGHT, 0x00164E, 0xFEFEFE, new Properties());
    public static final DragonSpawnEggItem NETHER_DRAGON_SPAWN_EGG = createDragonSpawnEgg("nether_dragon_spawn_egg", DragonTypes.NETHER, 0x632F1B, 0xE7A621, new Properties());
    public static final DragonSpawnEggItem SCULK_DRAGON_SPAWN_EGG = createDragonSpawnEgg("sculk_dragon_spawn_egg", DragonTypes.SCULK, 0x0F4649, 0x39D6E0, new Properties());
    public static final DragonSpawnEggItem SKELETON_DRAGON_SPAWN_EGG = createDragonSpawnEgg("skeleton_dragon_spawn_egg", DragonTypes.SKELETON, 0xFFFFFF, 0x474F51, new Properties());
    public static final DragonSpawnEggItem STORM_DRAGON_SPAWN_EGG = createDragonSpawnEgg("storm_dragon_spawn_egg", DragonTypes.STORM, 0x010B0F, 0x0FA8CE, new Properties());
    public static final DragonSpawnEggItem SUNLIGHT_DRAGON_SPAWN_EGG = createDragonSpawnEgg("sunlight_dragon_spawn_egg", DragonTypes.SUNLIGHT, 0xF4950D, 0xF4E10D, new Properties());
    public static final DragonSpawnEggItem TERRA_DRAGON_SPAWN_EGG = createDragonSpawnEgg("terra_dragon_spawn_egg", DragonTypes.TERRA, 0x674517, 0xE6B10D, new Properties());
    public static final DragonSpawnEggItem WATER_DRAGON_SPAWN_EGG = createDragonSpawnEgg("water_dragon_spawn_egg", DragonTypes.WATER, 0x546FAD, 0x2B427E, new Properties());
    public static final DragonSpawnEggItem WITHER_DRAGON_SPAWN_EGG = createDragonSpawnEgg("wither_dragon_spawn_egg", DragonTypes.WITHER, 0x8A9999, 0x474F51, new Properties());
    public static final DragonSpawnEggItem ZOMBIE_DRAGON_SPAWN_EGG = createDragonSpawnEgg("zombie_dragon_spawn_egg", DragonTypes.ZOMBIE, 0x66664B, 0xB6D035, new Properties());
    //?
    public static final SmithingTemplateItem DRAGON_ARMOR_NETHERITE_UPGRADE_SMITHING_TEMPLATE = registerItem("dragon_armor_netherite_upgrade_smithing_template", new SmithingTemplateItem(
            Component.translatable("upgrade.dragonmounts.dragon_armor_netherite_upgrade.applies_to").withStyle(ChatFormatting.BLUE),
            Component.translatable("block.minecraft.netherite_block").withStyle(ChatFormatting.BLUE),
            Component.translatable("upgrade.minecraft.netherite_upgrade").withStyle(ChatFormatting.GRAY),
            Component.translatable("upgrade.dragonmounts.dragon_armor_netherite_upgrade.base_slot_description"),
            Component.translatable("upgrade.dragonmounts.dragon_armor_netherite_upgrade.additions_slot_description"),
            List.of(),
            List.of()
    ), ITEMS);
    public static final FluteItem FLUTE = registerItem("flute", new FluteItem(new Properties()), ITEMS);
    public static final VariantSwitcherItem VARIANT_SWITCHER = registerItem("variant_switcher", new VariantSwitcherItem(new Properties()), ITEMS);
    //Shears
    public static final TieredShearsItem DIAMOND_SHEARS = createTieredShears("diamond_shears", Tiers.DIAMOND, new Properties());
    public static final TieredShearsItem NETHERITE_SHEARS = createTieredShears("netherite_shears", Tiers.NETHERITE, new Properties().fireResistant());
    //Dragon Amulets
    public static final AmuletItem<Entity> AMULET = registerItem("amulet", new AmuletItem<>(Entity.class, new Properties()), ITEMS);
    public static final DragonAmuletItem FOREST_DRAGON_AMULET = createDragonAmulet("forest_dragon_amulet", DragonTypes.FOREST, new Properties());
    public static final DragonAmuletItem FIRE_DRAGON_AMULET = createDragonAmulet("fire_dragon_amulet", DragonTypes.FIRE, new Properties());
    public static final DragonAmuletItem ICE_DRAGON_AMULET = createDragonAmulet("ice_dragon_amulet", DragonTypes.ICE, new Properties());
    public static final DragonAmuletItem WATER_DRAGON_AMULET = createDragonAmulet("water_dragon_amulet", DragonTypes.WATER, new Properties());
    public static final DragonAmuletItem AETHER_DRAGON_AMULET = createDragonAmulet("aether_dragon_amulet", DragonTypes.AETHER, new Properties());
    public static final DragonAmuletItem NETHER_DRAGON_AMULET = createDragonAmulet("nether_dragon_amulet", DragonTypes.NETHER, new Properties());
    public static final DragonAmuletItem ENDER_DRAGON_AMULET = createDragonAmulet("ender_dragon_amulet", DragonTypes.ENDER, new Properties());
    public static final DragonAmuletItem SUNLIGHT_DRAGON_AMULET = createDragonAmulet("sunlight_dragon_amulet", DragonTypes.SUNLIGHT, new Properties());
    public static final DragonAmuletItem ENCHANT_DRAGON_AMULET = createDragonAmulet("enchant_dragon_amulet", DragonTypes.ENCHANT, new Properties());
    public static final DragonAmuletItem STORM_DRAGON_AMULET = createDragonAmulet("storm_dragon_amulet", DragonTypes.STORM, new Properties());
    public static final DragonAmuletItem TERRA_DRAGON_AMULET = createDragonAmulet("terra_dragon_amulet", DragonTypes.TERRA, new Properties());
    public static final DragonAmuletItem ZOMBIE_DRAGON_AMULET = createDragonAmulet("zombie_dragon_amulet", DragonTypes.ZOMBIE, new Properties());
    public static final DragonAmuletItem MOONLIGHT_DRAGON_AMULET = createDragonAmulet("moonlight_dragon_amulet", DragonTypes.MOONLIGHT, new Properties());
    public static final DragonAmuletItem SCULK_DRAGON_AMULET = createDragonAmulet("sculk_dragon_amulet", DragonTypes.SCULK, new Properties().fireResistant());
    public static final DragonAmuletItem SKELETON_DRAGON_AMULET = createDragonAmulet("skeleton_dragon_amulet", DragonTypes.SKELETON, new Properties());
    public static final DragonAmuletItem WITHER_DRAGON_AMULET = createDragonAmulet("wither_dragon_amulet", DragonTypes.WITHER, new Properties());
    //Dragon Essences
    public static final DragonEssenceItem FOREST_DRAGON_ESSENCE = createDragonEssence("forest_dragon_essence", DragonTypes.FOREST, new Properties());
    public static final DragonEssenceItem FIRE_DRAGON_ESSENCE = createDragonEssence("fire_dragon_essence", DragonTypes.FIRE, new Properties());
    public static final DragonEssenceItem ICE_DRAGON_ESSENCE = createDragonEssence("ice_dragon_essence", DragonTypes.ICE, new Properties());
    public static final DragonEssenceItem WATER_DRAGON_ESSENCE = createDragonEssence("water_dragon_essence", DragonTypes.WATER, new Properties());
    public static final DragonEssenceItem AETHER_DRAGON_ESSENCE = createDragonEssence("aether_dragon_essence", DragonTypes.AETHER, new Properties());
    public static final DragonEssenceItem NETHER_DRAGON_ESSENCE = createDragonEssence("nether_dragon_essence", DragonTypes.NETHER, new Properties());
    public static final DragonEssenceItem ENDER_DRAGON_ESSENCE = createDragonEssence("ender_dragon_essence", DragonTypes.ENDER, new Properties());
    public static final DragonEssenceItem SUNLIGHT_DRAGON_ESSENCE = createDragonEssence("sunlight_dragon_essence", DragonTypes.SUNLIGHT, new Properties());
    public static final DragonEssenceItem ENCHANT_DRAGON_ESSENCE = createDragonEssence("enchant_dragon_essence", DragonTypes.ENCHANT, new Properties());
    public static final DragonEssenceItem STORM_DRAGON_ESSENCE = createDragonEssence("storm_dragon_essence", DragonTypes.STORM, new Properties());
    public static final DragonEssenceItem TERRA_DRAGON_ESSENCE = createDragonEssence("terra_dragon_essence", DragonTypes.TERRA, new Properties());
    public static final DragonEssenceItem ZOMBIE_DRAGON_ESSENCE = createDragonEssence("zombie_dragon_essence", DragonTypes.ZOMBIE, new Properties());
    public static final DragonEssenceItem MOONLIGHT_DRAGON_ESSENCE = createDragonEssence("moonlight_dragon_essence", DragonTypes.MOONLIGHT, new Properties());
    public static final DragonEssenceItem SCULK_DRAGON_ESSENCE = createDragonEssence("sculk_dragon_essence", DragonTypes.SCULK, new Properties().fireResistant());
    public static final DragonEssenceItem SKELETON_DRAGON_ESSENCE = createDragonEssence("skeleton_dragon_essence", DragonTypes.SKELETON, new Properties());
    public static final DragonEssenceItem WITHER_DRAGON_ESSENCE = createDragonEssence("wither_dragon_essence", DragonTypes.WITHER, new Properties());

    static <T extends Item> T registerItem(String name, T item, ObjectArrayList<? super T> tab) {
        tab.add(item);
        return Registry.register(BuiltInRegistries.ITEM, makeId(name), item);
    }

    static DragonAmuletItem createDragonAmulet(String name, DragonType type, Properties props) {
        DragonAmuletItem item = new DragonAmuletItem(type, props);
        type.bindInstance(DragonAmuletItem.class, item);
        return Registry.register(BuiltInRegistries.ITEM, makeId(name), item);
    }

    static DragonArmorItem createDragonArmor(String name, Holder<ArmorMaterial> material, String texture, Properties props) {
        return registerItem(name, new DragonArmorItem(material, makeId(texture), props), TOOLS);
    }

    static DragonEssenceItem createDragonEssence(String name, DragonType type, Properties props) {
        DragonEssenceItem item = new DragonEssenceItem(type, props);
        type.bindInstance(DragonEssenceItem.class, item);
        return Registry.register(BuiltInRegistries.ITEM, makeId(name), item);
    }

    static DragonScaleAxeItem createDragonScaleAxe(String name, DragonScaleTier tier, float attackDamageModifier, float attackSpeedModifier, Properties props) {
        DragonScaleAxeItem item = new DragonScaleAxeItem(tier, props.attributes(AxeItem.createAttributes(tier, attackDamageModifier, attackSpeedModifier)));
        tier.type.bindInstance(DragonScaleAxeItem.class, item);
        return registerItem(name, item, TOOLS);
    }

    static DragonScaleAxeItem createDragonScaleAxe(String name, DragonScaleTier tier, Properties props) {
        return createDragonScaleAxe(name, tier, 5.0F, -2.8F, props);
    }

    static DragonScaleBowItem createDragonScaleBow(String name, DragonScaleTier tier, Properties props) {
        DragonScaleBowItem item = new DragonScaleBowItem(tier, props);
        tier.type.bindInstance(DragonScaleBowItem.class, item);
        return registerItem(name, item, TOOLS);
    }

    static DragonScaleHoeItem createDragonScaleHoe(String name, DragonScaleTier tier, Properties props) {
        DragonScaleHoeItem item = new DragonScaleHoeItem(tier, props.attributes(HoeItem.createAttributes(tier, 0.0F, -3.0F)));
        tier.type.bindInstance(DragonScaleHoeItem.class, item);
        return registerItem(name, item, TOOLS);
    }

    static DragonScalePickaxeItem createDragonScalePickaxe(String name, DragonScaleTier tier, Properties props) {
        DragonScalePickaxeItem item = new DragonScalePickaxeItem(tier, props.attributes(PickaxeItem.createAttributes(tier, 1, -2.8F)));
        tier.type.bindInstance(DragonScalePickaxeItem.class, item);
        return registerItem(name, item, TOOLS);
    }

    static DragonScaleArmorSuit createDragonScaleArmors(
            String helmet,
            String chestplate,
            String leggings,
            String boots,
            DragonType type,
            IDragonScaleArmorEffect effect,
            Properties props
    ) {
        DragonScaleArmorSuit suit = new DragonScaleArmorSuit(type, effect, props);
        type.bindInstance(DragonScaleArmorSuit.class, suit);
        registerItem(helmet, suit.helmet, TOOLS);
        registerItem(chestplate, suit.chestplate, TOOLS);
        registerItem(leggings, suit.leggings, TOOLS);
        registerItem(boots, suit.boots, TOOLS);
        return suit;
    }

    static DragonScalesItem createDragonScales(String name, DragonType type, Properties props) {
        DragonScalesItem item = new DragonScalesItem(type, props);
        type.bindInstance(DragonScalesItem.class, item);
        return registerItem(name, item, ITEMS);
    }

    static DragonScaleShieldItem createDragonScaleShield(String name, DragonType type, Properties props) {
        DragonScaleShieldItem item = new DragonScaleShieldItem(type, props);
        type.bindInstance(DragonScaleShieldItem.class, item);
        return registerItem(name, item, TOOLS);
    }

    static DragonScaleShovelItem createDragonScaleShovel(String name, DragonScaleTier tier, Properties props) {
        DragonScaleShovelItem item = new DragonScaleShovelItem(tier, props.attributes(ShovelItem.createAttributes(tier, 1.5F, -3.0F)));
        tier.type.bindInstance(DragonScaleShovelItem.class, item);
        return registerItem(name, item, TOOLS);
    }

    static DragonScaleSwordItem createDragonScaleSword(String name, DragonScaleTier tier, Properties props) {
        DragonScaleSwordItem item = new DragonScaleSwordItem(tier, props.attributes(SwordItem.createAttributes(tier, 3, -2.0F)));
        tier.type.bindInstance(DragonScaleSwordItem.class, item);
        return registerItem(name, item, TOOLS);
    }

    static DragonSpawnEggItem createDragonSpawnEgg(String name, DragonType type, int background, int highlight, Properties props) {
        DragonSpawnEggItem item = new DragonSpawnEggItem(type, background, highlight, props);
        type.bindInstance(DragonSpawnEggItem.class, item);
        return registerItem(name, item, SPAWN_EGGS);
    }

    static TieredShearsItem createTieredShears(String name, Tier tier, Properties props) {
        TieredShearsItem item = new TieredShearsItem(tier, props);
        DispenserBlock.registerBehavior(item, TieredShearsItem.DISPENSE_ITEM_BEHAVIOR);
        return registerItem(name, item, ITEMS);
    }

    public static void attachItems(CreativeModeTab.ItemDisplayParameters context, CreativeModeTab.Output entries) {
        ITEMS.forEach(item -> entries.accept(new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
        attachSpawnEggs(entries);
    }

    public static void attachTools(CreativeModeTab.ItemDisplayParameters context, CreativeModeTab.Output entries) {
        TOOLS.forEach(item -> entries.accept(new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
    }

    public static void attachSpawnEggs(CreativeModeTab.Output entries) {
        SPAWN_EGGS.forEach(item -> entries.accept(new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
    }

    public static void forEachSpawnEgg(Consumer<SpawnEggItem> consumer) {
        SPAWN_EGGS.forEach(consumer);
    }

    public static void init() {}
}