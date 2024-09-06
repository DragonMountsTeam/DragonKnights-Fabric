package net.dragonmounts.config;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class BooleanEntry extends ConfigEntry<Boolean> {
    public final boolean defaultValue;
    protected boolean backup;
    protected boolean value;

    public BooleanEntry(String key, boolean init) {
        super(key);
        this.defaultValue = this.value = this.backup = init;
    }

    public BooleanEntry(String key, String display, boolean init) {
        super(key, display);
        this.defaultValue = this.value = this.backup = init;
    }

    public void set(boolean value) {
        this.value = value;
    }

    public boolean get() {
        return this.value;
    }

    @Override
    public void read(CompoundTag tag) {
        if (tag.contains(this.key)) {
            this.value = this.backup = tag.getBoolean(this.key);
        }
    }

    @Override
    public void save(CompoundTag tag) {
        if (this.backup ^ this.value) {
            if (this.defaultValue ^ this.value) tag.putBoolean(this.key, this.backup = this.value);
            else tag.remove(this.key);
        }
    }

    @Override
    protected int get(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.translatable("commands.dragonmounts.config.query", this.display, this.get()), true);
        return 1;
    }

    @Override
    protected int set(CommandContext<CommandSourceStack> context) {
        this.set(BoolArgumentType.getBool(context, "value"));
        context.getSource().sendSuccess(() -> Component.translatable("commands.dragonmounts.config.set", this.display, this.get()), true);
        ServerConfig.INSTANCE.save();
        return 1;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> generateCommand() {
        return Commands.literal(this.display).executes(this::get).then(Commands.argument("value", BoolArgumentType.bool()).executes(this::set));
    }

    @Override
    public void accept(Boolean value) {
        this.value = value;
    }
}
