package net.dragonmounts.data;

import net.dragonmounts.init.DMEntities;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.EntityTypeTags;

import java.util.concurrent.CompletableFuture;

public class DMEntityTagProvider extends FabricTagProvider.EntityTypeTagProvider {
    public DMEntityTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.getOrCreateTagBuilder(EntityTypeTags.CAN_BREATHE_UNDER_WATER)
                .add(DMEntities.TAMEABLE_DRAGON)
                .add(DMEntities.HATCHABLE_DRAGON_EGG);
        this.getOrCreateTagBuilder(EntityTypeTags.FALL_DAMAGE_IMMUNE)
                .add(DMEntities.TAMEABLE_DRAGON)
                .add(DMEntities.HATCHABLE_DRAGON_EGG);
    }
}
