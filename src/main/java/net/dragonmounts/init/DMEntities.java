package net.dragonmounts.init;

import net.dragonmounts.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import static net.dragonmounts.DragonMounts.makeId;

public class DMEntities {
    public static final EntityType<HatchableDragonEggEntity> HATCHABLE_DRAGON_EGG = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            makeId("dragon_egg"),
            EntityType.Builder.of(HatchableDragonEggEntity::construct, MobCategory.MISC)
                    .sized(0.875F, 1.0F)
                    .fireImmune()
                    .build()
    );
    public static final EntityType<TameableDragonEntity> TAMEABLE_DRAGON = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            makeId("dragon"),
            EntityType.Builder.of(TameableDragonEntity::construct, MobCategory.CREATURE)
                    .sized(4.8F, 4.2F)
                    .fireImmune()
                    .build()
    );

    @SuppressWarnings("DataFlowIssue")
    public static void init() {
        FabricDefaultAttributeRegistry.register(TAMEABLE_DRAGON, TameableDragonEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(HATCHABLE_DRAGON_EGG, HatchableDragonEggEntity.createAttributes());
    }
}
