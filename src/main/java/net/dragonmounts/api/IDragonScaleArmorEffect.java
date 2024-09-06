package net.dragonmounts.api;

import net.dragonmounts.capability.ArmorEffectManager;
import net.dragonmounts.capability.IArmorEffectManager;
import net.dragonmounts.registry.CooldownCategory;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.dragonmounts.util.TimeUtil.formatAsFloat;

public interface IDragonScaleArmorEffect extends IArmorEffect {
    default void appendTriggerInfo(ItemStack stack, Item.TooltipContext context, List<Component> tooltips) {
        tooltips.add(Component.translatable("tooltip.dragonmounts.armor_effect_piece_4"));
    }

    default void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext context, List<Component> tooltips, TooltipFlag flag) {}

    class Advanced extends CooldownCategory implements IDragonScaleArmorEffect {
        public final int cooldown;
        public final TranslatableContents description;

        public Advanced(ResourceLocation identifier, int cooldown) {
            super(identifier);
            this.cooldown = cooldown;
            this.description = new TranslatableContents(Util.makeDescriptionId("tooltip.armor_effect", identifier), null, TranslatableContents.NO_ARGS);
        }

        public final void appendCooldownInfo(List<Component> tooltips) {
            int value = ArmorEffectManager.getLocalCooldown(this);
            if (value > 0) {
                tooltips.add(Component.translatable("tooltip.dragonmounts.armor_effect_remaining_cooldown", formatAsFloat(value)));
            } else if (this.cooldown > 0) {
                tooltips.add(Component.translatable("tooltip.dragonmounts.armor_effect_cooldown", formatAsFloat(this.cooldown)));
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext context, List<Component> tooltips, TooltipFlag flag) {
            tooltips.add(Component.empty());
            this.appendTriggerInfo(stack, context, tooltips);
            tooltips.add(MutableComponent.create(this.description));
            this.appendCooldownInfo(tooltips);
        }

        @Override
        public boolean activate(IArmorEffectManager manager, Player player, int level) {
            return level > 3;
        }
    }
}
