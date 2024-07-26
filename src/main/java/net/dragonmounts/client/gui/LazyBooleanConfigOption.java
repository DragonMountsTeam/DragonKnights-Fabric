package net.dragonmounts.client.gui;

import net.dragonmounts.config.BooleanEntry;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.OptionButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.function.Function;

public class LazyBooleanConfigOption extends AbstractLazyConfigOption<BooleanEntry> {
    public static final Function<LazyBooleanConfigOption, Text> DEFAULT_STRINGIFY =
            option -> ScreenTexts.composeToggleText(option.getDisplayPrefix(), option.get());
    public static final Function<LazyBooleanConfigOption, Text> TOGGLE_STRINGIFY =
            option -> option.getGenericLabel(new TranslatableText(option.get() ? "options.key.toggle" : "options.key.hold"));
    public final Function<LazyBooleanConfigOption, Text> stringify;
    protected boolean value;

    public LazyBooleanConfigOption(
            String caption,
            BooleanEntry config,
            Function<LazyBooleanConfigOption, Text> stringify,
            Text tooltip
    ) {
        super(caption, config, tooltip);
        this.stringify = stringify;
        this.value = config.get();
    }

    public void toggle() {
        this.set(!this.get());
    }

    public void set(boolean value) {
        this.value = value;
    }

    public boolean get() {
        return this.value;
    }

    @Override
    public void save() {
        this.config.set(this.value);
    }

    @Override
    public ClickableWidget createButton(GameOptions options, int pX, int pY, int pWidth) {
        return new OptionButtonWidget(pX, pY, pWidth, 20, this, this.stringify.apply(this), button -> {
            this.toggle();
            button.setMessage(this.stringify.apply(this));
        });
    }
}
