package net.dragonmounts.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DMDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        var pack = generator.createPack();
        pack.addProvider(DMModelProvider::new);
        pack.addProvider(DMEntityTagProvider::new);
        var block = pack.addProvider(DMBlockTagProvider::new);
        pack.addProvider((output, future) -> new DMItemTagProvider(output, future, block));
        pack.addProvider(DMRecipeProvider::new);
    }
}