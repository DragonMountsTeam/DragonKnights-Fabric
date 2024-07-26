package net.dragonmounts.api;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;

public class ArmorSuit<T extends ArmorItem, M extends ArmorMaterial> {
    public final M material;
    public final T helmet;
    public final T chestplate;
    public final T leggings;
    public final T boots;

    public ArmorSuit(M material, T helmet, T chestplate, T leggings, T boots) {
        this.material = material;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
    }

    public final T bySlot(EquipmentSlot slot) {
        switch (slot.getArmorStandSlotId()) {
            case 4: return this.helmet;
            case 3: return this.chestplate;
            case 2: return this.leggings;
            case 1: return this.boots;
            default: return null;
        }
    }
}
