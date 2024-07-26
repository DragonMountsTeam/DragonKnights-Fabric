package net.dragonmounts.client;

import net.dragonmounts.config.ClientConfig;
import net.dragonmounts.data.tag.DMItemTags;
import net.dragonmounts.data.tag.ForgeTags;
import net.dragonmounts.entity.dragon.DragonLifeStage;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.init.DMKeyBindings;
import net.dragonmounts.item.DragonArmorItem;
import net.dragonmounts.network.DMPackets;
import net.dragonmounts.util.DragonFood;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SaddleItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;


public class ClientDragonEntity extends TameableDragonEntity {
    public final DragonAnimationContext context = new DragonAnimationContext(this);
    public boolean renderCrystalBeams = true;
    private int rideFlag = -1;

    public ClientDragonEntity(EntityType<? extends TameableDragonEntity> type, World world) {
        super(type, world);
    }

    public void onWingsDown(float speed) {
        if (!this.isTouchingWater()) {
            Vec3d pos = this.getPos();
            // play wing sounds
            this.world.playSound(
                    pos.x,
                    pos.y,
                    pos.z,
                    SoundEvents.ENTITY_ENDER_DRAGON_FLAP,
                    SoundCategory.VOICE,
                    (1 - speed) * this.getSoundPitch(),
                    (0.5F - speed * 0.2F) * this.getSoundVolume(),
                    true
            );
        }
    }

    @Override
    public void tickMovement() {
        if (this.isDead()) {
            this.nearestCrystal = null;
        } else {
            this.checkCrystals();
        }
        super.tickMovement();
        this.context.tick(this.firstUpdate);
        if (!this.isAgeLocked()) {
            if (this.breedingAge < 0) {
                ++this.breedingAge;
            } else if (this.breedingAge > 0) {
                --this.breedingAge;
            }
        }
    }

    @Override
    protected void checkCrystals() {
        if (this.nearestCrystal != null && this.nearestCrystal.isAlive()) {
            if (this.random.nextInt(20) == 0) {
                this.nearestCrystal = this.findCrystal();
            }
        } else {
            this.nearestCrystal = this.random.nextInt(10) == 0 ? this.findCrystal() : null;
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        Item item = player.getStackInHand(hand).getItem();
        return DragonFood.test(item) || (
                this.isOwner(player) && (
                        item instanceof SaddleItem ||
                                item instanceof DragonArmorItem ||
                                DMItemTags.BATONS.contains(item) ||
                                ForgeTags.Item.CHESTS_WOODEN.contains(item)
                )
        ) ? ActionResult.CONSUME : ActionResult.PASS;
    }

    @Override
    public void setLifeStage(DragonLifeStage stage, boolean reset, boolean sync) {
        if (this.stage == stage) return;
        this.stage = stage;
        if (reset) {
            this.refreshAge();
        }
        this.refreshPosition();
        this.calculateDimensions();
    }

    @Override
    public void travel(Vec3d vector) {
        super.travel(vector);
        if (this.isLogicalSideForUpdatingMovement()) {
            int flag = (
                    MinecraftClient.getInstance().options.keyJump.isPressed() ? 0b0001 : 0
            ) | (
                    DMKeyBindings.DESCENT.isPressed() ? 0b0010 : 0
            ) | (
                    ClientConfig.INSTANCE.converge_pitch_angle.get() ? 0b0100 : 0
            ) | (
                    ClientConfig.INSTANCE.converge_yaw_angle.get() ? 0b1000 : 0
            );
            if (this.rideFlag != flag) {
                PacketByteBuf buffer = this.writeId(PacketByteBufs.create());
                buffer.writeByte(this.rideFlag = flag);
                ClientPlayNetworking.send(DMPackets.RIDE_DRAGON_PACKET_ID, buffer);
            }
        }
    }

    public void handleAgeSync(int age, DragonLifeStage stage) {
        this.breedingAge = age;
        this.setLifeStage(stage, false, false);
    }

    @Override
    public void setBreedingAge(int age) {
        this.breedingAge = age;
    }

    public void refreshForcedAgeTimer() {
        if (this.happyTicksRemaining <= 0) {
            this.happyTicksRemaining = 40;
        }
    }
}
