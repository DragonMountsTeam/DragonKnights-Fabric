package net.dragonmounts.data;

import net.dragonmounts.api.DragonScaleArmorSuit;
import net.dragonmounts.init.DMBlocks;
import net.dragonmounts.init.DMItems;
import net.dragonmounts.item.*;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.tag.DMItemTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

import static net.dragonmounts.DragonMounts.makeId;
import static net.minecraft.data.recipes.ShapedRecipeBuilder.shaped;
import static net.minecraft.data.recipes.SimpleCookingRecipeBuilder.blasting;
import static net.minecraft.data.recipes.SimpleCookingRecipeBuilder.smelting;
import static net.minecraft.data.recipes.SmithingTransformRecipeBuilder.smithing;

public class DMRecipeProvider extends RecipeProvider {
    public DMRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    public void buildRecipes(RecipeOutput output) {
        smelting(Ingredient.of(DMItems.IRON_DRAGON_ARMOR), RecipeCategory.MISC, Items.IRON_INGOT, 1.0F, 200).unlockedBy("has_armor", has(DMItems.IRON_DRAGON_ARMOR)).save(output, makeId("iron_ingot_form_smelting"));
        smelting(Ingredient.of(DMItems.GOLDEN_DRAGON_ARMOR), RecipeCategory.MISC, Items.GOLD_INGOT, 1.0F, 200).unlockedBy("has_armor", has(DMItems.GOLDEN_DRAGON_ARMOR)).save(output, makeId("gold_ingot_form_smelting"));
        blasting(Ingredient.of(DMItems.IRON_DRAGON_ARMOR), RecipeCategory.MISC, Items.IRON_INGOT, 1.0F, 100).unlockedBy("has_armor", has(DMItems.IRON_DRAGON_ARMOR)).save(output, makeId("iron_ingot_form_blasting"));
        blasting(Ingredient.of(DMItems.GOLDEN_DRAGON_ARMOR), RecipeCategory.MISC, Items.GOLD_INGOT, 1.0F, 100).unlockedBy("has_armor", has(DMItems.GOLDEN_DRAGON_ARMOR)).save(output, makeId("gold_ingot_form_blasting"));
        dragonArmor(output, ConventionalItemTags.IRON_INGOTS, ConventionalItemTags.STORAGE_BLOCKS_IRON, DMItems.IRON_DRAGON_ARMOR);
        dragonArmor(output, ConventionalItemTags.GOLD_INGOTS, ConventionalItemTags.STORAGE_BLOCKS_GOLD, DMItems.GOLDEN_DRAGON_ARMOR);
        dragonArmor(output, ConventionalItemTags.EMERALD_GEMS, ConventionalItemTags.STORAGE_BLOCKS_EMERALD, DMItems.EMERALD_DRAGON_ARMOR);
        dragonArmor(output, ConventionalItemTags.DIAMOND_GEMS, ConventionalItemTags.STORAGE_BLOCKS_DIAMOND, DMItems.DIAMOND_DRAGON_ARMOR);
        netheriteSmithing(output, DMItems.DIAMOND_DRAGON_ARMOR, RecipeCategory.TOOLS, DMItems.NETHERITE_DRAGON_ARMOR);
        for (DragonType type : DragonType.REGISTRY) {
            var scales = type.getInstance(DragonScalesItem.class, null);
            if (scales == null) continue;
            dragonScaleAxe(output, scales, type.getInstance(DragonScaleAxeItem.class, null));
            dragonScaleArmors(output, scales, type.getInstance(DragonScaleArmorSuit.class, null));
            dragonScaleBow(output, scales, type.getInstance(DragonScaleBowItem.class, null));
            dragonScaleHoe(output, scales, type.getInstance(DragonScaleHoeItem.class, null));
            dragonScalePickaxe(output, scales, type.getInstance(DragonScalePickaxeItem.class, null));
            dragonScaleShovel(output, scales, type.getInstance(DragonScaleShovelItem.class, null));
            dragonScaleShield(output, scales, type.getInstance(DragonScaleShieldItem.class, null));
            dragonScaleSword(output, scales, type.getInstance(DragonScaleSwordItem.class, null));
        }
        smithing(
                Ingredient.of(DMItems.DRAGON_ARMOR_NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                Ingredient.of(DMItems.EMERALD_DRAGON_ARMOR),
                Ingredient.of(Items.NETHERITE_BLOCK),
                RecipeCategory.COMBAT,
                DMItems.NETHERITE_DRAGON_ARMOR
        ).unlocks("has_netherite_block", has(Items.NETHERITE_BLOCK)).save(output, "netherite_dragon_armor_smithing_form_emerald");
        smithing(
                Ingredient.of(DMItems.DRAGON_ARMOR_NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                Ingredient.of(DMItems.DIAMOND_DRAGON_ARMOR),
                Ingredient.of(Items.NETHERITE_BLOCK),
                RecipeCategory.COMBAT,
                DMItems.NETHERITE_DRAGON_ARMOR
        ).unlocks("has_netherite_block", has(Items.NETHERITE_BLOCK)).save(output, "netherite_dragon_armor_smithing_form_diamond");
        shaped(RecipeCategory.MISC, DMItems.DRAGON_ARMOR_NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                .define('#', DMItemTags.DRAGON_SCALES)
                .define('T', Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                .pattern("###")
                .pattern("#T#")
                .pattern("###")
                .unlockedBy("has_template", has(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE))
                .save(output);
        shaped(RecipeCategory.MISC, DMItems.DRAGON_ARMOR_NETHERITE_UPGRADE_SMITHING_TEMPLATE, 2)
                .define('#', ConventionalItemTags.DIAMOND_GEMS)
                .define('C', Items.NETHERRACK)
                .define('S', DMItems.DRAGON_ARMOR_NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                .pattern("#S#")
                .pattern("#C#")
                .pattern("###")
                .unlockedBy("has_template", has(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE))
                .save(output, "dragon_armor_netherite_upgrade_smithing_template_copy");
        shaped(RecipeCategory.TOOLS, DMItems.DIAMOND_SHEARS)
                .define('X', ConventionalItemTags.DIAMOND_GEMS)
                .pattern(" X")
                .pattern("X ")
                .unlockedBy("has_diamond", has(ConventionalItemTags.DIAMOND_GEMS))
                .save(output);
        shaped(RecipeCategory.REDSTONE, Items.DISPENSER)
                .define('R', ConventionalItemTags.REDSTONE_DUSTS)
                .define('#', ConventionalItemTags.COBBLESTONES)
                .define('X', DMItemTags.DRAGON_SCALE_BOWS)
                .pattern("###")
                .pattern("#X#")
                .pattern("#R#")
                .unlockedBy("has_bow", has(DMItemTags.DRAGON_SCALE_BOWS))
                .save(output, makeId(getItemName(Blocks.DISPENSER)));
        shaped(RecipeCategory.TOOLS, DMItems.AMULET)
                .define('#', ConventionalItemTags.STRINGS)
                .define('Y', ConventionalItemTags.COBBLESTONES)
                .define('X', ConventionalItemTags.ENDER_PEARLS)
                .pattern(" Y ")
                .pattern("#X#")
                .pattern(" # ")
                .unlockedBy("has_pearls", has(ConventionalItemTags.ENDER_PEARLS))
                .save(output);
        shaped(RecipeCategory.DECORATIONS, DMBlocks.DRAGON_NEST)
                .define('X', ConventionalItemTags.WOODEN_RODS)
                .pattern("XXX")
                .pattern("XXX")
                .pattern("XXX")
                .unlockedBy("has_sticks", has(ConventionalItemTags.WOODEN_RODS))
                .save(output);/*:
        shaped(RecipeCategory.TOOLS, DMItems.DRAGON_WHISTLE)
                .define('P', ConventionalItemTags.WOODEN_RODS)
                .define('#', ConventionalItemTags.ENDER_PEARLS)
                .define('X', ConventionalItemTags.STRINGS)
                .pattern("P#")
                .pattern("#X")
                .unlockedBy("has_pearls", has(ConventionalItemTags.ENDER_PEARLS))
                .save(output);*/
        shaped(RecipeCategory.TRANSPORTATION, Items.SADDLE)
                .define('X', ConventionalItemTags.LEATHERS)
                .define('#', ConventionalItemTags.IRON_INGOTS)
                .pattern("XXX")
                .pattern("X#X")
                .unlockedBy("easter_egg", has(Items.SADDLE))
                .save(output, makeId("easter_egg"));
    }

    static void dragonArmor(RecipeOutput output, TagKey<Item> ingot, TagKey<Item> block, Item result) {
        shaped(RecipeCategory.COMBAT, result).define('#', ingot).define('X', block).pattern("X #").pattern(" XX").pattern("## ").unlockedBy("has_ingot", has(ingot)).unlockedBy("has_block", has(block)).save(output);
    }

    private static void dragonScaleAxe(RecipeOutput consumer, Item scales, Item result) {
        if (result == null) return;
        shaped(RecipeCategory.TOOLS, result).define('#', ConventionalItemTags.WOODEN_RODS).define('X', scales).pattern("XX").pattern("X#").pattern(" #").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
    }

    private static void dragonScaleArmors(RecipeOutput consumer, Item scales, DragonScaleArmorSuit suit) {
        if (suit == null) return;
        shaped(RecipeCategory.COMBAT, suit.helmet).define('X', scales).pattern("XXX").pattern("X X").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
        shaped(RecipeCategory.COMBAT, suit.chestplate).define('X', scales).pattern("X X").pattern("XXX").pattern("XXX").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
        shaped(RecipeCategory.COMBAT, suit.leggings).define('X', scales).pattern("XXX").pattern("X X").pattern("X X").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
        shaped(RecipeCategory.COMBAT, suit.boots).define('X', scales).pattern("X X").pattern("X X").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
    }

    private static void dragonScaleBow(RecipeOutput consumer, Item scales, Item result) {
        if (result == null) return;
        shaped(RecipeCategory.COMBAT, result).define('#', scales).define('X', ConventionalItemTags.STRINGS).pattern(" #X").pattern("# X").pattern(" #X").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
    }

    private static void dragonScaleHoe(RecipeOutput consumer, Item scales, Item result) {
        if (result == null) return;
        shaped(RecipeCategory.TOOLS, result).define('#', ConventionalItemTags.WOODEN_RODS).define('X', scales).pattern("XX").pattern(" #").pattern(" #").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
    }

    private static void dragonScalePickaxe(RecipeOutput consumer, Item scales, Item result) {
        if (result == null) return;
        shaped(RecipeCategory.TOOLS, result).define('#', ConventionalItemTags.WOODEN_RODS).define('X', scales).pattern("XXX").pattern(" # ").pattern(" # ").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
    }

    private static void dragonScaleShield(RecipeOutput consumer, Item scales, Item result) {
        if (result == null) return;
        shaped(RecipeCategory.COMBAT, result).define('X', ConventionalItemTags.IRON_INGOTS).define('W', scales).pattern("WXW").pattern("WWW").pattern(" W ").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
    }

    private static void dragonScaleShovel(RecipeOutput consumer, Item scales, Item result) {
        if (result == null) return;
        shaped(RecipeCategory.TOOLS, result).define('#', ConventionalItemTags.WOODEN_RODS).define('X', scales).pattern("X").pattern("#").pattern("#").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
    }

    private static void dragonScaleSword(RecipeOutput consumer, Item scales, Item result) {
        if (result == null) return;
        shaped(RecipeCategory.COMBAT, result).define('#', ConventionalItemTags.WOODEN_RODS).define('X', scales).pattern("X").pattern("X").pattern("#").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
    }


}
