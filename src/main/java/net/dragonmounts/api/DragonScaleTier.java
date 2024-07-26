package net.dragonmounts.api;

import net.dragonmounts.init.DragonTypes;
import net.dragonmounts.item.DragonScalesItem;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.util.DragonTypifiedItemSupplier;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

import java.util.stream.Stream;

public class DragonScaleTier implements ToolMaterial, IDragonTypified {
    public static final DragonScaleTier AETHER;
    public static final DragonScaleTier ENCHANT;
    public static final DragonScaleTier ENDER;
    public static final DragonScaleTier FIRE;
    public static final DragonScaleTier FOREST;
    public static final DragonScaleTier ICE;
    public static final DragonScaleTier MOONLIGHT;
    public static final DragonScaleTier NETHER;
    public static final DragonScaleTier SCULK;
    public static final DragonScaleTier STORM;
    public static final DragonScaleTier SUNLIGHT;
    public static final DragonScaleTier TERRA;
    public static final DragonScaleTier WATER;
    public static final DragonScaleTier ZOMBIE;

    static {
        Builder builder = new Builder(4, 2700, 8.0F, 5.0F).setEnchantmentValue(11);
        FIRE = builder.build(DragonTypes.FIRE);
        FOREST = builder.build(DragonTypes.FOREST);
        ICE = builder.build(DragonTypes.ICE);
        MOONLIGHT = builder.build(DragonTypes.MOONLIGHT);
        STORM = builder.build(DragonTypes.STORM);
        SUNLIGHT = builder.build(DragonTypes.SUNLIGHT);
        TERRA = builder.build(DragonTypes.TERRA);
        WATER = builder.build(DragonTypes.WATER);
        ZOMBIE = builder.build(DragonTypes.ZOMBIE);
        ENCHANT = builder.setEnchantmentValue(30).build(DragonTypes.ENCHANT);
        builder = new Builder(5, 3000, 8.0F, 6.0F).setEnchantmentValue(11);
        ENDER = builder.build(DragonTypes.ENDER);
        SCULK = builder.build(DragonTypes.SCULK);
        AETHER = new Builder(5, 2700, 8.0F, 5.0F).setEnchantmentValue(11).build(DragonTypes.AETHER);
        NETHER = new Builder(5, 2700, 8.0F, 6.0F).setEnchantmentValue(11).build(DragonTypes.NETHER);
    }

    public final DragonType type;
    public final int level;
    public final int uses;
    public final float speed;
    public final float damage;
    public final int enchantmentValue;
    public final Ingredient repairIngredient;

    public DragonScaleTier(DragonType type, Builder builder) {
        this.type = type;
        this.level = builder.level;
        this.uses = builder.uses;
        this.speed = builder.speed;
        this.damage = builder.damage;
        this.enchantmentValue = builder.enchantmentValue;
        this.repairIngredient = builder.repairIngredient == null
                ? Ingredient.ofEntries(Stream.of(new DragonTypifiedItemSupplier<>(type, DragonScalesItem.class)))
                : builder.repairIngredient;
    }

    @Override
    public int getDurability() {
        return this.uses;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return this.speed;
    }

    @Override
    public float getAttackDamage() {
        return this.damage;
    }

    @Override
    public int getMiningLevel() {
        return this.level;
    }

    @Override
    public int getEnchantability() {
        return this.enchantmentValue;
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
        public final int level;
        public final int uses;
        public final float speed;
        public final float damage;
        public int enchantmentValue = 1;
        public Ingredient repairIngredient = null;

        public Builder(int level, int uses, float speed, float damage) {
            this.level = level;
            this.uses = uses;
            this.speed = speed;
            this.damage = damage;
        }

        public Builder setEnchantmentValue(int enchantmentValue) {
            this.enchantmentValue = enchantmentValue;
            return this;
        }

        public Builder setRepairIngredient(Ingredient ingredient) {
            this.repairIngredient = ingredient;
            return this;
        }

        public DragonScaleTier build(DragonType type) {
            return new DragonScaleTier(type, this);
        }
    }
}
