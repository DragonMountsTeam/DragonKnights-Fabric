package net.dragonmounts.registry;

import net.dragonmounts.entity.dragon.ServerDragonEntity;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.Util;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

import static net.dragonmounts.DragonMounts.FLUTE_COMMAND;

public abstract class FluteCommand implements Consumer<ServerDragonEntity> {
    public static final MappedRegistry<FluteCommand> REGISTRY = FabricRegistryBuilder.createSimple(FLUTE_COMMAND).buildAndRegister();

    public final int id;
    public final ResourceLocation identifier;
    public final TranslatableContents name;
    public final ResourceLocation icon;

    public FluteCommand(ResourceLocation identifier, ResourceLocation icon) {
        this.id = REGISTRY.getId(Registry.register(REGISTRY, this.identifier = identifier, this));
        this.name = new TranslatableContents(this.makeDescriptionId(), null, TranslatableContents.NO_ARGS);
        this.icon = icon;
    }

    protected String makeDescriptionId() {
        return Util.makeDescriptionId("flute_command", this.identifier);
    }
}
