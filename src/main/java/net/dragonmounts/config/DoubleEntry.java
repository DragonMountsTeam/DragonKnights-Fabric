package net.dragonmounts.config;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import static net.minecraft.util.Mth.clamp;

public class DoubleEntry extends ConfigEntry<Double> {
    private static final double MIN_DOUBLE = -Double.MAX_VALUE;
    public final double defaultValue;
    public final double min;
    public final double max;
    protected double backup;
    protected double value;

    public DoubleEntry(String key, double init) {
        this(key, init, MIN_DOUBLE, Double.MAX_VALUE);
    }

    public DoubleEntry(String key, String display, double init) {
        this(key, display, init, MIN_DOUBLE, Double.MAX_VALUE);
    }

    public DoubleEntry(String key, double init, double min, double max) {
        super(key);
        this.min = min;
        this.max = max;
        this.defaultValue = this.value = this.backup = clamp(init, min, max);
    }

    public DoubleEntry(String key, String display, double init, double min, double max) {
        super(key, display);
        this.min = min;
        this.max = max;
        this.defaultValue = this.value = this.backup = clamp(init, min, max);
    }

    public void set(double value) {
        this.value = clamp(value, this.min, this.max);
    }

    public double get() {
        return this.value;
    }

    @Override
    public void read(CompoundTag tag) {
        if (tag.contains(this.key)) {
            this.backup = this.value = clamp(tag.getDouble(this.key), this.min, this.max);
        }
    }

    @Override
    public void save(CompoundTag tag) {
        if (this.backup != this.value) {
            if (this.defaultValue != this.value) tag.putDouble(this.key, this.backup = this.value);
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
        this.set(DoubleArgumentType.getDouble(context, "value"));
        context.getSource().sendSuccess(() -> Component.translatable("commands.dragonmounts.config.set", this.display, this.get()), true);
        ServerConfig.INSTANCE.save();
        return 1;
    }

    public LiteralArgumentBuilder<CommandSourceStack> generateCommand(double min, double max) {
        return Commands.literal(this.display).executes(this::get).then(Commands.argument("value", DoubleArgumentType.doubleArg(min, max)).executes(this::set));
    }

    @Override
    public final LiteralArgumentBuilder<CommandSourceStack> generateCommand() {
        return this.generateCommand(this.min, this.max);
    }

    @Override
    public void accept(Double value) {
        this.value = clamp(value, this.min, this.max);
    }
}
