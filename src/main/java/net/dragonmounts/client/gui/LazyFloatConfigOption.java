package net.dragonmounts.client.gui;

import com.mojang.serialization.Codec;
import net.dragonmounts.config.FloatEntry;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LazyFloatConfigOption extends AbstractLazyConfigOption<Float, FloatEntry> {
    public static final OptionInstance.CaptionBasedToString<Float> X_2F_STRINGIFIER = (component, config) -> Options.genericValueLabel(component, Component.literal(String.format("%.2f", config)));
    public final Range range;

    public LazyFloatConfigOption(String caption, FloatEntry config, Component tooltip, OptionInstance.CaptionBasedToString<Float> stringifier) {
        this(caption, config, new Range(config.min, config.max, 0.1F), tooltip, stringifier);
    }

    public LazyFloatConfigOption(
            String caption,
            FloatEntry config,
            Range range,
            Component tooltip,
            OptionInstance.CaptionBasedToString<Float> stringifier
    ) {
        super(caption, config, tooltip, stringifier);
        this.range = range;
    }

    @Override
    public OptionInstance<Float> makeInstance() {
        return new OptionInstance<>(this.caption, this, this.stringifier, this.range, this.config.get(), this.config);
    }

    public static class Range implements OptionInstance.SliderableValueSet<Float> {
        public final float max;
        public final float min;
        public final float step;
        protected final float range;

        public Range(float min, float max, float step) {
            if (min >= max) throw new IllegalArgumentException();
            this.max = max;
            this.min = min;
            this.step = step;
            this.range = max - min;
        }

        @Override
        public double toSliderValue(Float value) {
            return value < this.max ? value > this.min ? (value - this.min) / this.range : 0.0 : 1.0;
        }

        @Override
        public @NotNull Float fromSliderValue(double delta) {
            return Math.round(Mth.lerp(delta, this.min, this.max) / this.step) * this.step;
        }

        @Override
        public @NotNull Optional<Float> validateValue(Float value) {
            return value.compareTo(this.min) < 0 || value.compareTo(this.max) > 0 ? Optional.empty() : Optional.of(value);
        }

        @Override
        public @NotNull Codec<Float> codec() {
            return Codec.floatRange(this.min, this.max);
        }
    }
}
