package net.dragonmounts.client;

import net.dragonmounts.entity.dragon.DragonLifeStage;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.item.DragonArmorItem;
import net.dragonmounts.tag.DMItemTags;
import net.dragonmounts.util.DragonFood;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SaddleItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;


public class ClientDragonEntity extends TameableDragonEntity {
    public final DragonAnimationContext context = new DragonAnimationContext(this);
    public boolean renderCrystalBeams = true;

    public ClientDragonEntity(EntityType<? extends TameableDragonEntity> type, Level world) {
        super(type, world);
    }

   /* public void onWingsDown(float speed) {
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
    }*/

    @Override
    public void aiStep() {
        if (this.isDeadOrDying()) {
            this.nearestCrystal = null;
        } else {
            this.checkCrystals();
        }
        super.aiStep();
        this.context.tick(this.firstTick);
        if (!this.isAgeLocked()) {
            if (this.age < 0) {
                ++this.age;
            } else if (this.age > 0) {
                --this.age;
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

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        Item item = player.getItemInHand(hand).getItem();
        if (DragonFood.test(item)) return InteractionResult.CONSUME;
        if (this.isOwnedBy(player)) {
            if (item instanceof SaddleItem || item instanceof DragonArmorItem) {
                return InteractionResult.CONSUME;
            }
            Holder.Reference<Item> holder = item.builtInRegistryHolder();
            if (holder.is(DMItemTags.BATONS) || holder.is(ConventionalItemTags.WOODEN_CHESTS)) {
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void setLifeStage(DragonLifeStage stage, boolean reset, boolean sync) {
        if (this.stage == stage) return;
        this.stage = stage;
        if (reset) {
            this.refreshAge();
        }
        this.reapplyPosition();
        this.refreshDimensions();
    }


        /*if (this.isLogicalSideForUpdatingMovement()) {
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
        }*/

    @Override
    public void setAge(int age) {
        this.age = age;
    }

    public void refreshForcedAgeTimer() {
        if (this.forcedAgeTimer <= 0) {
            this.forcedAgeTimer = 40;
        }
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {}

    @Override
    public TameableDragonEntity getScreenOpeningData(ServerPlayer player) {return this;}

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {return null;}
}
