package net.dragonmounts.client.gui;

import net.dragonmounts.config.ConfigEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Option;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public abstract class AbstractLazyConfigOption<T extends ConfigEntry> extends Option {
    public final T config;

    public AbstractLazyConfigOption(String caption, T config, Text tooltip) {
        super(caption);
        this.config = config;
        if (tooltip != null) {
            this.setTooltip(MinecraftClient.getInstance().textRenderer.wrapLines(tooltip, 200));
        }
    }

    public abstract void save();

    @Override
    public Text getPixelLabel(int pixel) {
        return new TranslatableText("options.pixel_value", this.getDisplayPrefix(), pixel);
    }

    @Override
    public Text getPercentLabel(double percentage) {
        return new TranslatableText("options.percent_value", this.getDisplayPrefix(), (int) (percentage * 100));
    }

    @Override
    public Text getPercentAdditionLabel(int value) {
        return new TranslatableText("options.percent_add_value", this.getDisplayPrefix(), value);
    }

    @Override
    public Text getGenericLabel(Text value) {
        return new TranslatableText("options.generic_value", this.getDisplayPrefix(), value);
    }

    @Override
    public Text getGenericLabel(int value) {
        return this.getGenericLabel(new LiteralText(Integer.toString(value)));
    }
}
