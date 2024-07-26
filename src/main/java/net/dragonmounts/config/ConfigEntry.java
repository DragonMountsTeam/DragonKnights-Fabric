package net.dragonmounts.config;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;

public abstract class ConfigEntry {
    public final String key;
    public final String display;

    public ConfigEntry(String key) {
        this.display = (this.key = key).toLowerCase();
    }

    public ConfigEntry(String key, String display) {
        this.key = key;
        this.display = display;
    }

    public abstract void read(NbtCompound tag);

    public abstract void save(NbtCompound tag);

    protected abstract int get(CommandContext<ServerCommandSource> context);

    protected abstract int set(CommandContext<ServerCommandSource> context);

    public abstract LiteralArgumentBuilder<ServerCommandSource> generateCommand();
}
