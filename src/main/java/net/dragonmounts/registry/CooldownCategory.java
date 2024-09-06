package net.dragonmounts.registry;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import static net.dragonmounts.DragonMounts.COOLDOWN_CATEGORY;

public class CooldownCategory {
    public static final MappedRegistry<CooldownCategory> REGISTRY = FabricRegistryBuilder.createSimple(COOLDOWN_CATEGORY).buildAndRegister();

    public final ResourceLocation identifier;
    public final int id;

    public CooldownCategory(ResourceLocation identifier) {
        this.id = REGISTRY.getId(Registry.register(REGISTRY, this.identifier = identifier, this));
    }
}
