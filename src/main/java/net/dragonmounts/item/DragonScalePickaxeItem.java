package net.dragonmounts.item;

import net.dragonmounts.api.DragonScaleTier;
import net.dragonmounts.api.DragonTypified;
import net.dragonmounts.init.DMDataComponents;
import net.dragonmounts.registry.DragonType;
import net.minecraft.world.item.PickaxeItem;

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;

public class DragonScalePickaxeItem extends PickaxeItem implements DragonTypified {
    private static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_pickaxe";
    public final DragonType type;

    public DragonScalePickaxeItem(DragonScaleTier tier, Properties props) {
        super(tier, props.component(DMDataComponents.DRAGON_TYPE, tier.type));
        this.type = tier.type;
    }

    @Override
    public String getDescriptionId() {
        return TRANSLATION_KEY;
    }

    @Override
    public DragonType getDragonType() {
        return this.type;
    }
}