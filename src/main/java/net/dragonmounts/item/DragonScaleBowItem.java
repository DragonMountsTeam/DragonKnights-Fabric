package net.dragonmounts.item;

import net.dragonmounts.api.DragonScaleTier;
import net.dragonmounts.api.IDragonTypified;
import net.dragonmounts.registry.DragonType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;

public class DragonScaleBowItem extends BowItem implements IDragonTypified {
    private static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_bow";
    public final DragonScaleTier tier;

    public DragonScaleBowItem(DragonScaleTier tier, Settings settings) {
        super(settings.maxDamageIfAbsent(tier.getDurability() >> 1));
        this.tier = tier;
    }

    @Override
    public int getEnchantability() {
        return this.tier.getEnchantability();
    }

    @Override
    public boolean canRepair(ItemStack toRepair, ItemStack repair) {
        return this.tier.repairIngredient.test(repair);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> tooltips, TooltipContext flag) {
        tooltips.add(this.tier.type.getName());
    }

    @Override
    public String getTranslationKey() {
        return TRANSLATION_KEY;
    }

    @Override
    public DragonType getDragonType() {
        return this.tier.type;
    }
}
