package net.dragonmounts.data;

import net.dragonmounts.block.HatchableDragonEggBlock;
import net.dragonmounts.init.DMBlocks;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.tag.DMBlockTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DMBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public DMBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.getOrCreateTagBuilder(BlockTags.PIGLIN_REPELLENTS).add(DMBlocks.DRAGON_CORE);
        this.getOrCreateTagBuilder(BlockTags.DRAGON_IMMUNE).add(DMBlocks.DRAGON_CORE);
        Consumer<Block> addToEggs = this.getOrCreateTagBuilder(DMBlockTags.DRAGON_EGGS).add(Blocks.DRAGON_EGG)::add;
        for (var type : DragonType.REGISTRY) {
            type.ifPresent(HatchableDragonEggBlock.class, addToEggs);
        }
    }
}
