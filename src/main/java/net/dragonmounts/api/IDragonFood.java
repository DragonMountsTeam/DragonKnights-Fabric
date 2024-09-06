package net.dragonmounts.api;

import net.dragonmounts.client.ClientDragonEntity;
import net.dragonmounts.entity.dragon.DragonLifeStage;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.util.math.MathUtil;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface IDragonFood {
    IDragonFood UNKNOWN = new IDragonFood() {
        @Override
        public void feed(TameableDragonEntity dragon, Player player, ItemStack stack, InteractionHand hand) {}

        @Override
        public boolean canFeed(TameableDragonEntity dragon, Player player, ItemStack stack, InteractionHand hand) {return false;}
    };

    void feed(TameableDragonEntity dragon, Player player, ItemStack stack, InteractionHand hand);

    default boolean canFeed(TameableDragonEntity dragon, Player player, ItemStack stack, InteractionHand hand) {
        return true;
    }

    default void displayEatingEffects(ClientDragonEntity dragon, Item item) {
        if (item == Items.AIR) return;
        if (dragon.getLifeStage() != DragonLifeStage.ADULT) {
            dragon.refreshForcedAgeTimer();
        }
        Vec3 pos = dragon.context.getThroatPosition(0, 0, -4);
        if (pos == null) return;
        Level level = dragon.level();
        level.playLocalSound(dragon, item.getEatingSound(), SoundSource.NEUTRAL, 1F, 0.75F);
        if (item == Items.HONEY_BOTTLE) return;
        if (item instanceof BucketItem) {
            level.playLocalSound(dragon, item.getDrinkingSound(), SoundSource.NEUTRAL, 0.25F, 0.75F);
            if (item == Items.COD_BUCKET) {
                item = Items.COD;
            } else if (item == Items.SALMON_BUCKET) {
                item = Items.SALMON;
            } else {
                item = Items.TROPICAL_FISH;
            }
        }
        ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(item));
        RandomSource random = dragon.getRandom();
        float xRot = -dragon.getXRot() * MathUtil.TO_RAD_FACTOR;
        float yRot = -dragon.getYRot() * MathUtil.TO_RAD_FACTOR;
        double cosX = Mth.cos(xRot);
        double sinX = Mth.sin(xRot);
        double cosY = Mth.cos(yRot);
        double sinY = Mth.sin(yRot);
        for (int i = 0; i < 8; ++i) {
            double x = (random.nextFloat() - 0.5D) * 0.1D, y = random.nextFloat() * 0.1D + 0.1D;
            level.addParticle(particle, pos.x, pos.y, pos.z, x * cosY + x * sinX * sinY, y * cosX + 0.05D, x * sinX * cosY - x * sinY);
        }
    }
}
