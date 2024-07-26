package net.dragonmounts.api;

import net.dragonmounts.client.ClientDragonEntity;
import net.dragonmounts.entity.dragon.DragonLifeStage;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.util.math.MathUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

@FunctionalInterface
public interface IDragonFood {
    IDragonFood UNKNOWN = (dragon, player, stack, hand) -> {};
    void eat(TameableDragonEntity dragon, PlayerEntity player, ItemStack stack, Hand hand);

    default boolean isEatable(TameableDragonEntity dragon, PlayerEntity player, ItemStack stack, Hand hand) {
        return true;
    }

    default void act(ClientDragonEntity dragon, Item item) {
        if (item == Items.AIR) return;
        if (dragon.getLifeStage() != DragonLifeStage.ADULT) {
            dragon.refreshForcedAgeTimer();
        }
        Vec3d pos = dragon.context.getThroatPosition(0, 0, -4);
        if (pos == null) return;
        World level = dragon.world;
        level.playSound(pos.x, pos.y, pos.z, item.getEatSound(), SoundCategory.NEUTRAL, 1F, 0.75F, false);
        if (item == Items.HONEY_BOTTLE) return;
        if (item instanceof BucketItem) {
            level.playSound(pos.x, pos.y, pos.z, item.getDrinkSound(), SoundCategory.NEUTRAL, 0.25F, 0.75F, false);
            if (item == Items.COD_BUCKET) {
                item = Items.COD;
            } else if (item == Items.SALMON_BUCKET) {
                item = Items.SALMON;
            } else {
                item = Items.TROPICAL_FISH;
            }
        }
        ItemStack stack = new ItemStack(item);
        Random random = dragon.getRandom();
        for (int i = 0; i < 8; ++i) {
            Vec3d speed = new Vec3d((random.nextFloat() - 0.5D) * 0.1D, random.nextFloat() * 0.1D + 0.1D, 0.0D).rotateX(-dragon.pitch * MathUtil.PI / 180F).rotateY(-dragon.yaw * MathUtil.PI / 180F);
            level.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), pos.x, pos.y, pos.z, speed.x, speed.y + 0.05D, speed.z);
        }
    }
}
