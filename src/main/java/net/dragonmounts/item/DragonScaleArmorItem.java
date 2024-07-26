package net.dragonmounts.item;

import net.dragonmounts.api.DragonScaleMaterial;
import net.dragonmounts.api.IArmorEffectSource;
import net.dragonmounts.api.IDragonScaleArmorEffect;
import net.dragonmounts.api.IDragonTypified;
import net.dragonmounts.capability.IArmorEffectManager;
import net.dragonmounts.registry.DragonType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;

public class DragonScaleArmorItem extends ArmorItem implements IDragonTypified, IArmorEffectSource {
    private static final String[] TRANSLATION_KEYS = new String[]{
            ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_boots",
            ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_leggings",
            ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_chestplate",
            ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_helmet"
    };

    protected DragonType type;
    public final IDragonScaleArmorEffect effect;

    public DragonScaleArmorItem(DragonScaleMaterial material, EquipmentSlot slot, IDragonScaleArmorEffect effect, Settings settings) {
        super(material, slot, settings);
        this.type = material.type;
        this.effect = effect;
    }

    @Override
    public void affect(IArmorEffectManager manager, PlayerEntity player, ItemStack stack) {
        if (this.effect != null) manager.stackLevel(this.effect);
    }

    @Override
    public String getTranslationKey() {
        return TRANSLATION_KEYS[this.slot.getEntitySlotId()];
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> tooltips, TooltipContext flag) {
        tooltips.add(this.type.getName());
        if (this.effect == null) return;
        this.effect.appendHoverText(stack, level, tooltips);
    }

    @Override
    public DragonType getDragonType() {
        return this.type;
    }
}
