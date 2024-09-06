package net.dragonmounts.item;

import net.dragonmounts.api.DragonScaleTier;
import net.dragonmounts.api.DragonTypified;
import net.dragonmounts.init.DMDataComponents;
import net.dragonmounts.registry.DragonType;
import net.minecraft.world.item.HoeItem;
import org.jetbrains.annotations.NotNull;

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;

public class DragonScaleHoeItem extends HoeItem implements DragonTypified {
    private static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_hoe";
    public final DragonType type;

    public DragonScaleHoeItem(DragonScaleTier tier, Properties props) {
        super(tier, props.component(DMDataComponents.DRAGON_TYPE, tier.type));
        this.type = tier.type;
    }

    @Override
    public @NotNull String getDescriptionId() {
        return TRANSLATION_KEY;
    }

    @Override
    public DragonType getDragonType() {
        return this.type;
    }
}