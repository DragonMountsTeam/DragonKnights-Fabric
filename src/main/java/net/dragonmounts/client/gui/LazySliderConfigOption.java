package net.dragonmounts.client.gui;

import net.dragonmounts.config.DoubleEntry;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.Function;

public class LazySliderConfigOption extends AbstractLazyConfigOption<DoubleEntry> {
    public static final Function<LazySliderConfigOption, Text> STRINGIFY_X_2F = button -> button.getGenericLabel(new LiteralText(String.format("%.2f", button.value)));
    public final Function<LazySliderConfigOption, Text> stringify;
    public final double minValue;
    public final double maxValue;
    public final double distance;
    public final double steps;
    protected double value;

    public LazySliderConfigOption(
            String caption,
            DoubleEntry config,
            double minValue,
            double maxValue,
            double steps,
            Function<LazySliderConfigOption, Text> stringify,
            Text tooltip
    ) {
        super(caption, config, tooltip);
        this.value = config.get();
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.distance = maxValue - minValue;
        this.steps = steps;
        this.stringify = stringify;
    }

    @Override
    public ClickableWidget createButton(GameOptions options, int pX, int pY, int pWidth) {
        return new ConfigSliderWidget(pX, pY, pWidth, 20, this);
    }

    public double toPct(double value) {
        return MathHelper.clamp((value - this.minValue) / this.distance, 0.0D, 1.0D);
    }

    public double toValue(double value) {
        value = this.minValue + value * this.distance;
        if (this.steps > 0.0F) {
            return MathHelper.clamp(this.steps * Math.round(value / this.steps), this.minValue, this.maxValue);
        }
        return MathHelper.clamp(value, this.minValue, this.maxValue);
    }

    public void set(double value) {
        this.value = value;
    }

    public double get() {
        return this.value;
    }

    @Override
    public void save() {
        this.config.set(this.value);
    }

    public Text getMessage() {
        return this.stringify.apply(this);
    }
}
