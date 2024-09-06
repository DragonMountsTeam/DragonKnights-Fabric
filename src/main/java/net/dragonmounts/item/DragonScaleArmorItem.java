package net.dragonmounts.item;

import net.dragonmounts.api.DragonTypified;
import net.dragonmounts.api.IArmorEffectSource;
import net.dragonmounts.api.IDragonScaleArmorEffect;
import net.dragonmounts.capability.IArmorEffectManager;
import net.dragonmounts.init.DMDataComponents;
import net.dragonmounts.registry.DragonType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.dragonmounts.DragonMounts.ITEM_TRANSLATION_KEY_PREFIX;

public class DragonScaleArmorItem extends ArmorItem implements DragonTypified, IArmorEffectSource {
    private static final String[] TRANSLATION_KEYS = new String[]{
            ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_helmet",
            ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_chestplate",
            ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_leggings",
            ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_boots",
            ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_armor"
    };

    protected final DragonType dragonType;
    public final IDragonScaleArmorEffect effect;

    public DragonScaleArmorItem(DragonType type, Type slot, IDragonScaleArmorEffect effect, Properties props) {
        super(type.material, slot, props.component(DMDataComponents.DRAGON_TYPE, type));
        this.dragonType = type;
        this.effect = effect;
    }

    @Override
    public void affect(IArmorEffectManager manager, Player player, ItemStack stack) {
        if (this.effect != null) {
            manager.stackLevel(this.effect);
        }
    }

    @Override
    public @NotNull String getDescriptionId() {
        return TRANSLATION_KEYS[this.type.ordinal()];
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag flag) {
        if (this.effect == null) return;
        this.effect.appendHoverText(stack, context, tooltips, flag);
    }

    @Override
    public DragonType getDragonType() {
        return this.dragonType;
    }
}
