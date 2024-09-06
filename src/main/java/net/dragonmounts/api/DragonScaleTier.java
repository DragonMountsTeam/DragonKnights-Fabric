package net.dragonmounts.api;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.dragonmounts.init.DragonTypes;
import net.dragonmounts.registry.DragonType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class DragonScaleTier implements Tier, DragonTypified {
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
        Builder builder = new Builder(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 2700, 8.0F, 5.0F).setEnchantmentValue(11);
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
        TagKey<Block> netherite = BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        builder = new Builder(netherite, 3000, 8.0F, 6.0F).setEnchantmentValue(11);
        ENDER = builder.build(DragonTypes.ENDER);
        SCULK = builder.build(DragonTypes.SCULK);
        AETHER = new Builder(netherite, 2700, 8.0F, 5.0F).setEnchantmentValue(11).build(DragonTypes.AETHER);
        NETHER = new Builder(netherite, 2700, 8.0F, 6.0F).setEnchantmentValue(11).build(DragonTypes.NETHER);
    }

    public final DragonType type;
    public final TagKey<Block> incorrectBlocks;
    public final int uses;
    public final float speed;
    public final float damage;
    public final int enchantmentValue;
    public final Supplier<Ingredient> repairIngredient;

    public DragonScaleTier(DragonType type, Builder builder, Supplier<Ingredient> supplier) {
        this.type = type;
        this.incorrectBlocks = builder.incorrectBlocks;
        this.uses = builder.uses;
        this.speed = builder.speed;
        this.damage = builder.damage;
        this.enchantmentValue = builder.enchantmentValue;
        this.repairIngredient = Suppliers.memoize(supplier);
    }

    @Override
    public int getUses() {
        return this.uses;
    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.damage;
    }

    @Override
    public @NotNull TagKey<Block> getIncorrectBlocksForDrops() {
        return this.incorrectBlocks;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public final @NotNull Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public final DragonType getDragonType() {
        return this.type;
    }

    public static class Builder {
        public final TagKey<Block> incorrectBlocks;
        public final int uses;
        public final float speed;
        public final float damage;
        public int enchantmentValue = 1;
        public Supplier<Ingredient> repairIngredient = null;

        public Builder(TagKey<Block> incorrectBlocks, int uses, float speed, float damage) {
            this.incorrectBlocks = incorrectBlocks;
            this.uses = uses;
            this.speed = speed;
            this.damage = damage;
        }

        public Builder setEnchantmentValue(int enchantmentValue) {
            this.enchantmentValue = enchantmentValue;
            return this;
        }

        public Builder setRepairIngredient(Supplier<Ingredient> ingredient) {
            this.repairIngredient = ingredient;
            return this;
        }

        public DragonScaleTier build(DragonType type) {
            return new DragonScaleTier(type, this, this.repairIngredient == null
                    ? Suppliers.memoize(type::getRepairIngredient)
                    : this.repairIngredient
            );
        }
    }
}
