package net.dragonmounts.client.gui;

import net.dragonmounts.config.ConfigEntry;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractLazyConfigOption<V, T extends ConfigEntry<V>> implements OptionInstance.TooltipSupplier<V> {
    public final OptionInstance.CaptionBasedToString<V> stringifier;
    public final Component tooltip;
    public final String caption;
    public final T config;

    public AbstractLazyConfigOption(String caption, T config, Component tooltip, OptionInstance.CaptionBasedToString<V> stringifier) {
        this.caption = caption;
        this.config = config;
        this.tooltip = tooltip;
        this.stringifier = stringifier;
    }

    @Nullable
    @Override
    public Tooltip apply(V object) {
        return this.tooltip == null ? null : Tooltip.create(this.tooltip);
    }

    public abstract OptionInstance<V> makeInstance();
}
