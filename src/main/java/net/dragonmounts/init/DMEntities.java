package net.dragonmounts.init;

import net.dragonmounts.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static net.dragonmounts.DragonMounts.MOD_ID;

public class DMEntities {
    public static final EntityType<HatchableDragonEggEntity> HATCHABLE_DRAGON_EGG = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(MOD_ID, "dragon_egg"),
            FabricEntityTypeBuilder.<HatchableDragonEggEntity>createLiving()
                    .entityFactory(HatchableDragonEggEntity::new)
                    .defaultAttributes(HatchableDragonEggEntity::createAttributes)
                    .dimensions(EntityDimensions.fixed(0.875F, 1.0F))
                    .fireImmune()
                    .build()
    );
    public static final EntityType<TameableDragonEntity> TAMEABLE_DRAGON = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(MOD_ID, "dragon"),
            FabricEntityTypeBuilder.<TameableDragonEntity>createMob()
                    .entityFactory(TameableDragonEntity::construct)
                    .defaultAttributes(TameableDragonEntity::createAttributes)
                    .dimensions(EntityDimensions.fixed(4.8F, 4.2F))
                    .spawnGroup(SpawnGroup.CREATURE)
                    .fireImmune()
                    .build()
    );

    public static void init() {}
}
