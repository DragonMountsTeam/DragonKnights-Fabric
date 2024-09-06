package net.dragonmounts.data;

import net.dragonmounts.block.HatchableDragonEggBlock;
import net.dragonmounts.init.DMBlocks;
import net.dragonmounts.init.DragonVariants;
import net.dragonmounts.registry.DragonVariant;
import net.dragonmounts.util.Values;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;

import java.util.Optional;

import static net.dragonmounts.DragonMounts.makeId;
import static net.minecraft.data.models.BlockModelGenerators.createSimpleBlock;
import static net.minecraft.data.models.model.ModelLocationUtils.getModelLocation;
import static net.minecraft.resources.ResourceLocation.withDefaultNamespace;

public class DMModelProvider extends FabricModelProvider {
    public static final ModelTemplate VANILLA_DRAGON_HEAD = new ModelTemplate(Optional.of(withDefaultNamespace("item/dragon_head")), Optional.empty());

    public DMModelProvider(FabricDataOutput output) {
        super(output);
    }

    public static void createDragonEgg(BlockModelGenerators generators, HatchableDragonEggBlock block) {
        generators.blockStateOutput.accept(createSimpleBlock(block, BuiltInRegistries.BLOCK.getKey(block).withPrefix("block/")));
    }

    public static void generateDragonHeads(BlockModelGenerators generators, Values<DragonVariant> variants) {
        var state = generators.blockStateOutput;
        var model = generators.modelOutput;
        var empty = new TextureMapping();
        variants.forEach(variant -> {
            VANILLA_DRAGON_HEAD.create(getModelLocation(variant.headItem), empty, model);
            state.accept(createSimpleBlock(variant.headBlock, withDefaultNamespace("block/skull")));
            state.accept(createSimpleBlock(variant.headWallBlock, withDefaultNamespace("block/skull")));
            generators.skipAutoItemBlock(variant.headWallBlock);
        });
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generators) {
        generateDragonHeads(generators, DragonVariants.BUILTIN_VALUES);
        var particle = TextureMapping.particle(makeId("block/dragon_core_break"));
        generators.blockStateOutput.accept(createSimpleBlock(DMBlocks.DRAGON_CORE,
                ModelTemplates.PARTICLE_ONLY.create(makeId("block/dragon_core"), particle, generators.modelOutput)
        ));
        ModelTemplates.SHULKER_BOX_INVENTORY.create(makeId("item/dragon_core"), particle, generators.modelOutput);
        generators.createTrivialCube(DMBlocks.DRAGON_NEST);
        DMBlocks.BUILTIN_EGGS.forEach(egg -> createDragonEgg(generators, egg));
    }

    @Override
    public void generateItemModels(ItemModelGenerators generators) {
        generators.output.accept(makeId("item/dragon_nest"), new DelegatedModel(makeId("block/dragon_nest")));
        generators.output.accept(
                makeId("item/dragon_armor_netherite_upgrade_smithing_template"),
                new DelegatedModel(withDefaultNamespace("item/netherite_upgrade_smithing_template"))
        );
    }
}
