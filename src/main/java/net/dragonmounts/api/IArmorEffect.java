package net.dragonmounts.api;

import net.dragonmounts.capability.IArmorEffectManager;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface IArmorEffect {
    boolean activate(IArmorEffectManager manager, Player player, int level);
}
