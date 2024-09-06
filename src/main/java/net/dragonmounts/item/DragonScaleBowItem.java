package net.dragonmounts.item;

import net.dragonmounts.api.DragonScaleTier;
import net.dragonmounts.api.DragonTypified;
import net.dragonmounts.init.DMDataComponents;
import net.dragonmounts.registry.DragonType;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;

public class DragonScaleBowItem extends BowItem implements DragonTypified {
    private static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_bow";
    public final DragonScaleTier tier;

    public DragonScaleBowItem(DragonScaleTier tier, Properties props) {
        super(props.durability(tier.getUses() >> 1).component(DMDataComponents.DRAGON_TYPE, tier.type));
        this.tier = tier;
    }

    @Override
    public int getEnchantmentValue() {
        return this.tier.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack tool, ItemStack candidate) {
        return this.tier.repairIngredient.get().test(candidate);
    }

    @Override
    public @NotNull String getDescriptionId() {
        return TRANSLATION_KEY;
    }

    @Override
    public DragonType getDragonType() {
        return this.tier.type;
    }
}
