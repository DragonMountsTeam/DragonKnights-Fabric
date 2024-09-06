package net.dragonmounts.item;

import net.dragonmounts.api.DragonTypified;
import net.dragonmounts.init.DMDataComponents;
import net.dragonmounts.registry.DragonType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.jetbrains.annotations.NotNull;

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;

public class DragonScaleShieldItem extends ShieldItem implements DragonTypified {
    public static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_shield";
    public static final int BASE_DURABILITY = 50;
    public final DragonType type;

    public DragonScaleShieldItem(DragonType type, Properties props) {
        super(props.durability(BASE_DURABILITY * type.durabilityFactor).component(DMDataComponents.DRAGON_TYPE, type));
        this.type = type;
    }

    @Override
    public int getEnchantmentValue() {
        return this.type.material.value().enchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack tool, ItemStack candidate) {
        return this.type.getRepairIngredient().test(candidate);
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
