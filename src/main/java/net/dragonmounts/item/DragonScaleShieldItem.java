package net.dragonmounts.item;

import net.dragonmounts.api.DragonScaleMaterial;
import net.dragonmounts.api.IDragonTypified;
import net.dragonmounts.registry.DragonType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;

public class DragonScaleShieldItem extends ShieldItem implements IDragonTypified {
    private static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_shield";
    public final DragonScaleMaterial material;

    public DragonScaleShieldItem(DragonScaleMaterial material, Settings settings) {
        super(settings.maxDamageIfAbsent(material.getDurabilityForShield()));
        this.material = material;
    }

    @Override
    public int getEnchantability() {
        return this.material.getEnchantability();
    }

    @Override
    public boolean canRepair(ItemStack toRepair, ItemStack repair) {
        return this.material.repairIngredient.test(repair);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> tooltips, TooltipContext flag) {
        tooltips.add(this.material.type.getName());
    }

    @Override
    public String getTranslationKey() {
        return TRANSLATION_KEY;
    }

    @Override
    public DragonType getDragonType() {
        return this.material.type;
    }
}
