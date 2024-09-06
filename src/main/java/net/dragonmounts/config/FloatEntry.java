package net.dragonmounts.config;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import static net.minecraft.util.Mth.clamp;

public class FloatEntry extends ConfigEntry<Float> {
    private static final float MIN_FLOAT = -Float.MAX_VALUE;
    public final float defaultValue;
    public final float min;
    public final float max;
    protected float backup;
    protected float value;

    public FloatEntry(String key, float init) {
        this(key, init, MIN_FLOAT, Float.MAX_VALUE);
    }

    public FloatEntry(String key, String display, float init) {
        this(key, display, init, MIN_FLOAT, Float.MAX_VALUE);
    }

    public FloatEntry(String key, float init, float min, float max) {
        super(key);
        this.min = min;
        this.max = max;
        this.defaultValue = this.value = this.backup = clamp(init, min, max);
    }

    public FloatEntry(String key, String display, float init, float min, float max) {
        super(key, display);
        this.min = min;
        this.max = max;
        this.defaultValue = this.value = this.backup = clamp(init, min, max);
    }

    public void set(float value) {
        this.value = clamp(value, this.min, this.max);
    }

    public float get() {
        return this.value;
    }

    @Override
    public void read(CompoundTag tag) {
        if (tag.contains(this.key)) {
            this.backup = this.value = clamp(tag.getFloat(this.key), this.min, this.max);
        }
    }

    @Override
    public void save(CompoundTag tag) {
        if (this.backup != this.value) {
            if (this.defaultValue != this.value) tag.putFloat(this.key, this.backup = this.value);
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
        this.set(FloatArgumentType.getFloat(context, "value"));
        context.getSource().sendSuccess(() -> Component.translatable("commands.dragonmounts.config.set", this.display, this.get()), true);
        ServerConfig.INSTANCE.save();
        return 1;
    }

    public LiteralArgumentBuilder<CommandSourceStack> generateCommand(float min, float max) {
        return Commands.literal(this.display).executes(this::get).then(Commands.argument("value", FloatArgumentType.floatArg(min, max)).executes(this::set));
    }

    @Override
    public final LiteralArgumentBuilder<CommandSourceStack> generateCommand() {
        return this.generateCommand(this.min, this.max);
    }

    @Override
    public void accept(Float value) {
        this.value = clamp(value, this.min, this.max);
    }
}
