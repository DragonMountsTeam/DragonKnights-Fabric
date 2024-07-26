package net.dragonmounts.api;

import net.dragonmounts.init.DragonTypes;
import net.dragonmounts.item.DragonScalesItem;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.util.DragonTypifiedItemSupplier;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.stream.Stream;

public class DragonScaleMaterial implements ArmorMaterial, IDragonTypified {
    public static final int SHIELD_DURABILITY = 50;
    public static final int SLOT_BASED_DURABILITY = 0x0D0F100B;
    public static final DragonScaleMaterial AETHER;
    public static final DragonScaleMaterial ENCHANT;
    public static final DragonScaleMaterial ENDER;
    public static final DragonScaleMaterial FIRE;
    public static final DragonScaleMaterial FOREST;
    public static final DragonScaleMaterial ICE;
    public static final DragonScaleMaterial MOONLIGHT;
    public static final DragonScaleMaterial NETHER;
    public static final DragonScaleMaterial SCULK;
    public static final DragonScaleMaterial STORM;
    public static final DragonScaleMaterial SUNLIGHT;
    public static final DragonScaleMaterial TERRA;
    public static final DragonScaleMaterial WATER;
    public static final DragonScaleMaterial ZOMBIE;

    static {
        Builder builder = new Builder(0x03080703, 50)
                .setEnchantmentValue(11)
                .setToughness(7.0F);
        MOONLIGHT = builder.build(DragonTypes.MOONLIGHT);
        STORM = builder.build(DragonTypes.STORM);
        TERRA = builder.build(DragonTypes.TERRA);
        ZOMBIE = builder.build(DragonTypes.ZOMBIE);
        builder = new Builder(0x04080704, 50)
                .setEnchantmentValue(11)
                .setToughness(7.0F);
        AETHER = builder.build(DragonTypes.AETHER);
        FIRE = builder.build(DragonTypes.FIRE);
        FOREST = builder.build(DragonTypes.FOREST);
        ICE = builder.build(DragonTypes.ICE);
        SUNLIGHT = builder.build(DragonTypes.SUNLIGHT);
        WATER = builder.build(DragonTypes.WATER);
        ENCHANT = builder.setEnchantmentValue(30).build(DragonTypes.ENCHANT);
        builder = new Builder(0x04090704, 70)
                .setEnchantmentValue(11)
                .setToughness(9.0F);
        ENDER = builder.build(DragonTypes.ENDER);
        SCULK = builder.build(DragonTypes.SCULK);
        NETHER = new Builder(0x04090704, 55)
                .setEnchantmentValue(11)
                .setToughness(8.0F)
                .build(DragonTypes.NETHER);
    }

    public final int protection;
    public final int durabilityFactor;
    public final int enchantmentValue;
    public final float toughness;
    public final float knockbackResistance;
    public final DragonType type;
    public final String name;
    public final SoundEvent sound;
    public final Ingredient repairIngredient;

    public DragonScaleMaterial(DragonType type, Builder builder) {
        this.type = type;
        this.name = type.identifier.getPath();
        this.sound = builder.sound;
        this.protection = builder.protection;
        this.durabilityFactor = builder.durabilityFactor;
        this.enchantmentValue = builder.enchantmentValue;
        this.toughness = builder.toughness;
        this.knockbackResistance = builder.knockbackResistance;
        this.repairIngredient = builder.repairIngredient == null
                ? Ingredient.ofEntries(Stream.of(new DragonTypifiedItemSupplier<>(type, DragonScalesItem.class)))
                : builder.repairIngredient;
    }

    public int getDurabilityForShield() {
        return SHIELD_DURABILITY * this.durabilityFactor;
    }

    @Override
    public int getDurability(EquipmentSlot slot) {
        return (SLOT_BASED_DURABILITY >> (slot.getEntitySlotId() << 3) & 0xFF) * this.durabilityFactor;
    }

    @Override
    public int getProtectionAmount(EquipmentSlot slot) {
        return this.protection >> (slot.getEntitySlotId() << 3) & 0xFF;
    }

    @Override
    public int getEnchantability() {
        return this.enchantmentValue;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public final SoundEvent getEquipSound() {
        return this.sound;
    }

    @Override
    public final Ingredient getRepairIngredient() {
        return this.repairIngredient;
    }

    @Override
    public final DragonType getDragonType() {
        return this.type;
    }

    public static class Builder {
        public final int protection;
        public final int durabilityFactor;
        public int enchantmentValue = 1;
        public SoundEvent sound = SoundEvents.ITEM_ARMOR_EQUIP_GOLD;
        public float toughness = 0;
        public float knockbackResistance = 0;
        public Ingredient repairIngredient = null;

        public Builder(int protection, int durabilityFactor) {
            this.protection = protection;
            this.durabilityFactor = durabilityFactor;
        }

        public Builder setEnchantmentValue(int enchantmentValue) {
            this.enchantmentValue = enchantmentValue;
            return this;
        }

        public Builder setSound(SoundEvent sound) {
            this.sound = sound;
            return this;
        }

        public Builder setToughness(float toughness) {
            this.toughness = toughness;
            return this;
        }

        public Builder setKnockbackResistance(float knockbackResistance) {
            this.knockbackResistance = knockbackResistance;
            return this;
        }

        public Builder setRepairIngredient(Ingredient ingredient) {
            this.repairIngredient = ingredient;
            return this;
        }

        public DragonScaleMaterial build(DragonType type) {
            return new DragonScaleMaterial(type, this);
        }
    }
}
