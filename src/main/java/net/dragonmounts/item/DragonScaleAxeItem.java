package net.dragonmounts.item;

import net.dragonmounts.api.DragonScaleTier;
import net.dragonmounts.api.IDragonTypified;
import net.dragonmounts.registry.DragonType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;

public class DragonScaleAxeItem extends AxeItem implements IDragonTypified {
    private static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_axe";
    public final DragonType type;

    public DragonScaleAxeItem(
            DragonScaleTier tier,
            float attackDamageModifier,
            float attackSpeedModifier/*Minecraft: -3.0F*/,
            Settings settings
    ) {
        super(tier, attackDamageModifier, attackSpeedModifier, settings);
        this.type = tier.getDragonType();
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> tooltips, TooltipContext flag) {
        tooltips.add(this.type.getName());
    }

    @Override
    public String getTranslationKey() {
        return TRANSLATION_KEY;
    }

    @Override
    public DragonType getDragonType() {
        return this.type;
    }
}