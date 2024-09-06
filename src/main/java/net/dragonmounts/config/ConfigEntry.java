package net.dragonmounts.config;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Consumer;

public abstract class ConfigEntry<T> implements Consumer<T> {
    public final String key;
    public final String display;

    public ConfigEntry(String key) {
        this.display = (this.key = key).toLowerCase();
    }

    public ConfigEntry(String key, String display) {
        this.key = key;
        this.display = display;
    }

    public abstract void read(CompoundTag tag);

    public abstract void save(CompoundTag tag);

    protected abstract int get(CommandContext<CommandSourceStack> context);

    protected abstract int set(CommandContext<CommandSourceStack> context);

    public abstract LiteralArgumentBuilder<CommandSourceStack> generateCommand();
}
