package net.dragonmounts.api;

import net.dragonmounts.capability.IArmorEffectManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IArmorEffectSource {
    void affect(IArmorEffectManager manager, Player player, ItemStack stack);
}
