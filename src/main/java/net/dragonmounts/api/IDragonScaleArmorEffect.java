package net.dragonmounts.api;

import net.dragonmounts.capability.ArmorEffectManager;
import net.dragonmounts.capability.IArmorEffectManager;
import net.dragonmounts.registry.CooldownCategory;
import net.dragonmounts.util.TextBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.dragonmounts.util.TimeUtil.stringifyTick;
import static net.minecraft.util.Util.createTranslationKey;

public interface IDragonScaleArmorEffect extends IArmorEffect {
    default void appendTriggerInfo(ItemStack stack, World world, List<Text> tooltips) {
        tooltips.add(new TranslatableText("tooltip.dragonmounts.armor_effect_piece_4"));
    }

    default void appendHoverText(ItemStack stack, @Nullable World level, List<Text> tooltips) {}

    class Advanced extends CooldownCategory implements IDragonScaleArmorEffect {
        public final TextBlock majorTooltip;
        public final int cooldown;
        public final String description;

        public Advanced(Identifier identifier, int cooldown) {
            super(identifier);
            this.cooldown = cooldown;
            this.description = createTranslationKey("tooltip.armor_effect", identifier);
            this.majorTooltip = init();
        }

        protected TextBlock init() {
            return new TextBlock(new TranslatableText(this.description));
        }

        public static void appendCooldownInfo(List<Text> tooltips, Advanced effect) {
            int value = ArmorEffectManager.getLocalCooldown(effect);
            if (value > 0) {
                tooltips.add(new TranslatableText("tooltip.dragonmounts.armor_effect_remaining_cooldown", stringifyTick(value)));
            } else if (effect.cooldown > 0) {
                tooltips.add(new TranslatableText("tooltip.dragonmounts.armor_effect_cooldown", stringifyTick(effect.cooldown)));
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable World level, List<Text> tooltips) {
            tooltips.add(LiteralText.EMPTY);
            this.appendTriggerInfo(stack, level, tooltips);
            this.majorTooltip.appendHoverText(tooltips);
            appendCooldownInfo(tooltips, this);
        }

        @Override
        public boolean activate(IArmorEffectManager manager, PlayerEntity player, int level) {
            return level > 3;
        }
    }
}
