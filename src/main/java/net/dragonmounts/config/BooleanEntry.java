package net.dragonmounts.config;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

public class BooleanEntry extends ConfigEntry {
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
    public void read(NbtCompound tag) {
        if (tag.contains(this.key)) {
            this.value = this.backup = tag.getBoolean(this.key);
        }
    }

    @Override
    public void save(NbtCompound tag) {
        if (this.backup ^ this.value) {
            if (this.defaultValue ^ this.value) tag.putBoolean(this.key, this.backup = this.value);
            else tag.remove(this.key);
        }
    }

    @Override
    protected int get(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(new TranslatableText("commands.dragonmounts.config.query", this.display, this.get()), true);
        return 1;
    }

    @Override
    protected int set(CommandContext<ServerCommandSource> context) {
        this.set(BoolArgumentType.getBool(context, "value"));
        context.getSource().sendFeedback(new TranslatableText("commands.dragonmounts.config.set", this.display, this.get()), true);
        ServerConfig.INSTANCE.save();
        return 1;
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> generateCommand() {
        return CommandManager.literal(this.display).executes(this::get).then(CommandManager.argument("value", BoolArgumentType.bool()).executes(this::set));
    }
}
