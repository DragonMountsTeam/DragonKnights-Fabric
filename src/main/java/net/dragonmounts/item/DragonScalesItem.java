package net.dragonmounts.item;

import net.dragonmounts.api.DragonTypified;
import net.dragonmounts.init.DMDataComponents;
import net.dragonmounts.registry.DragonType;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;

public class DragonScalesItem extends Item implements DragonTypified {
    private static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scales";
    public final DragonType type;

    public DragonScalesItem(DragonType type, Properties props) {
        super(props.component(DMDataComponents.DRAGON_TYPE, type));
        this.type = type;
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
