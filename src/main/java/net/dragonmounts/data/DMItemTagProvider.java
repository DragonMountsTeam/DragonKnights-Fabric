package net.dragonmounts.data;

import net.dragonmounts.api.DragonScaleArmorSuit;
import net.dragonmounts.init.DMBlocks;
import net.dragonmounts.init.DMItems;
import net.dragonmounts.item.*;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.tag.DMBlockTags;
import net.dragonmounts.tag.DMItemTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DMItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public DMItemTagProvider(
            FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> provider,
            FabricTagProvider.BlockTagProvider block
    ) {
        super(output, provider, block);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.getOrCreateTagBuilder(DMItemTags.HARD_SHEARS)
                .add(DMItems.DIAMOND_SHEARS)
                .add(DMItems.NETHERITE_SHEARS);
        this.getOrCreateTagBuilder(ItemTags.PIGLIN_LOVED)
                .add(DMItems.GOLDEN_DRAGON_ARMOR);
        this.getOrCreateTagBuilder(ItemTags.PIGLIN_REPELLENTS)
                .add(DMBlocks.DRAGON_CORE.asItem());
        this.getOrCreateTagBuilder(DMItemTags.BATONS)
                .addOptionalTag(ConventionalItemTags.RODS)
                .add(Items.DEBUG_STICK)
                .add(Items.BONE)
                .add(Items.BAMBOO);
        var head = this.getOrCreateTagBuilder(ItemTags.HEAD_ARMOR);
        var chest = this.getOrCreateTagBuilder(ItemTags.CHEST_ARMOR);
        var leg = this.getOrCreateTagBuilder(ItemTags.LEG_ARMOR);
        var foot = this.getOrCreateTagBuilder(ItemTags.FOOT_ARMOR);
        Consumer<DragonScaleArmorSuit> bindSuit = suit -> {
            head.add(suit.helmet);
            chest.add(suit.chestplate);
            leg.add(suit.leggings);
            foot.add(suit.boots);
        };
        Consumer<Item> addToScales = this.getOrCreateTagBuilder(DMItemTags.DRAGON_SCALES)::add;
        Consumer<Item> addToSwords = this.getOrCreateTagBuilder(ItemTags.SWORD_ENCHANTABLE)::add;
        Consumer<Item> addToBows = this.getOrCreateTagBuilder(DMItemTags.DRAGON_SCALE_BOWS)::add;
        Consumer<Item> addToAxes = this.getOrCreateTagBuilder(ItemTags.AXES)::add;
        Consumer<Item> addToHoes = this.getOrCreateTagBuilder(ItemTags.HOES)::add;
        Consumer<Item> addToPickaxes = this.getOrCreateTagBuilder(ItemTags.PICKAXES)::add;
        Consumer<Item> addToShovels = this.getOrCreateTagBuilder(ItemTags.SHOVELS)::add;
        Consumer<Item> addToShields = this.getOrCreateTagBuilder(ConventionalItemTags.SHIELD_TOOLS)::add;
        for (var type : DragonType.REGISTRY) {
            type.ifPresent(DragonScaleArmorSuit.class, bindSuit);
            type.ifPresent(DragonScalesItem.class, addToScales);
            type.ifPresent(DragonScaleSwordItem.class, addToSwords);
            type.ifPresent(DragonScaleBowItem.class, addToBows);
            type.ifPresent(DragonScaleAxeItem.class, addToAxes);
            type.ifPresent(DragonScaleHoeItem.class, addToHoes);
            type.ifPresent(DragonScalePickaxeItem.class, addToPickaxes);
            type.ifPresent(ShovelItem.class, addToShovels);
            type.ifPresent(ShovelItem.class, addToShields);
        }
        this.getOrCreateTagBuilder(DMItemTags.DRAGON_INEDIBLE)
                .addOptionalTag(ConventionalItemTags.FOOD_POISONING_FOODS)
                .add(Items.AXOLOTL_BUCKET)
                .add(Items.PUFFERFISH_BUCKET);
        this.getOrCreateTagBuilder(DMItemTags.COOKED_DRAGON_FOOD)
                .addOptionalTag(ConventionalItemTags.COOKED_MEAT_FOODS)
                .addOptionalTag(ConventionalItemTags.COOKED_FISH_FOODS);
        this.getOrCreateTagBuilder(DMItemTags.RAW_DRAGON_FOOD)
                .addOptionalTag(ConventionalItemTags.RAW_MEAT_FOODS)
                .addOptionalTag(ConventionalItemTags.RAW_FISH_FOODS)
                .add(Items.COD_BUCKET)
                .add(Items.SALMON_BUCKET)
                .add(Items.TROPICAL_FISH_BUCKET);
        this.tag(ConventionalItemTags.SHEAR_TOOLS).addTag(DMItemTags.HARD_SHEARS);
        this.tag(ItemTags.MINING_ENCHANTABLE).addTag(DMItemTags.HARD_SHEARS);
        this.tag(ItemTags.BOW_ENCHANTABLE).addTag(DMItemTags.DRAGON_SCALE_BOWS);
        this.tag(ConventionalItemTags.BOW_TOOLS).addTag(DMItemTags.DRAGON_SCALE_BOWS);
        this.copy(DMBlockTags.DRAGON_EGGS, DMItemTags.DRAGON_EGGS);
    }
}
