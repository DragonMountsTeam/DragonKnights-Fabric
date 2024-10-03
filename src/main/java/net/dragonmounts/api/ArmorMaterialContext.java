package net.dragonmounts.api;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;

import java.util.EnumMap;

public class ArmorMaterialContext {
    public final EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
    public int durabilityFactor;
    public int enchantmentValue = 1;
    public Holder<SoundEvent> sound = SoundEvents.ARMOR_EQUIP_GOLD;
    public float toughness = 0;
    public float knockbackResistance = 0;

    public ArmorMaterialContext(int durabilityFactor) {
        this.setDurabilityFactor(durabilityFactor).setDefense(ArmorItem.Type.BODY, 11);
    }

    public ArmorMaterialContext setDurabilityFactor(int durabilityFactor) {
        this.durabilityFactor = durabilityFactor;
        return this;
    }

    public ArmorMaterialContext setDefense(ArmorItem.Type type, int defense) {
        this.defense.put(type, defense);
        return this;
    }

    public ArmorMaterialContext setEnchantmentValue(int enchantmentValue) {
        this.enchantmentValue = enchantmentValue;
        return this;
    }

    public ArmorMaterialContext setSound(Holder<SoundEvent> sound) {
        this.sound = sound;
        return this;
    }

    public ArmorMaterialContext setToughness(float toughness) {
        this.toughness = toughness;
        return this;
    }

    public ArmorMaterialContext setKnockbackResistance(float knockbackResistance) {
        this.knockbackResistance = knockbackResistance;
        return this;
    }
}
