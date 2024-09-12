package net.dragonmounts.entity.dragon;

import net.dragonmounts.api.IDragonTypified;
import net.dragonmounts.block.HatchableDragonEggBlock;
import net.dragonmounts.config.ServerConfig;
import net.dragonmounts.init.*;
import net.dragonmounts.item.DragonScalesItem;
import net.dragonmounts.network.DMPackets;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.registry.DragonVariant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.entity.EntityPickInteractionAware;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

import static net.dragonmounts.entity.dragon.TameableDragonEntity.AGE_DATA_PARAMETER_KEY;
import static net.dragonmounts.init.DMGameRules.DEFAULT_DRAGON_BASE_HEALTH;
import static net.dragonmounts.init.DMGameRules.DRAGON_BASE_HEALTH;
import static net.dragonmounts.util.EntityUtil.addOrMergeEffect;
import static net.dragonmounts.util.math.MathUtil.TO_RAD_FACTOR;
import static net.dragonmounts.util.math.MathUtil.getColor;

public class HatchableDragonEggEntity extends LivingEntity implements EntityPickInteractionAware, IDragonTypified.Mutable {
    protected static final TrackedData<DragonType> DATA_DRAGON_TYPE = DataTracker.registerData(HatchableDragonEggEntity.class, DragonType.SERIALIZER);
    private static final float EGG_CRACK_PROCESS_THRESHOLD = 0.9F;
    private static final float EGG_SHAKE_PROCESS_THRESHOLD = 0.75F;
    private static final float EGG_SHAKE_BASE_CHANCE = 20F;
    public static final int MIN_HATCHING_TIME = 36000;
    public static final int EGG_CRACK_THRESHOLD = (int) (EGG_CRACK_PROCESS_THRESHOLD * MIN_HATCHING_TIME);
    public static final int EGG_SHAKE_THRESHOLD = (int) (EGG_SHAKE_PROCESS_THRESHOLD * MIN_HATCHING_TIME);
    public final Predicate<Entity> pushablePredicate = (entity) -> !entity.isSpectator() && entity.isPushable() && !entity.hasPassenger(this);
    protected String variant;
    protected UUID owner;
    protected float rotationAxis = 0;
    protected float amplitude = 0;
    protected float amplitudeO = 0;
    protected int shaking = 0;
    protected int age = 0;
    protected boolean hatched = false;

    public HatchableDragonEggEntity(EntityType<? extends HatchableDragonEggEntity> type, World level) {
        super(type, level);
        this.pushSpeedReduction = 0.625F;
        //noinspection DataFlowIssue
        this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(level.getGameRules().get(DRAGON_BASE_HEALTH).get());
    }

    public HatchableDragonEggEntity(World level) {
        this(DMEntities.HATCHABLE_DRAGON_EGG, level);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, DEFAULT_DRAGON_BASE_HEALTH)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(DATA_DRAGON_TYPE, DragonTypes.ENDER);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        tag.putString(DragonType.DATA_PARAMETER_KEY, DragonType.REGISTRY.getId(this.dataTracker.get(DATA_DRAGON_TYPE)).toString());
        tag.putInt(AGE_DATA_PARAMETER_KEY, this.age);
        if (this.owner != null) {
            tag.putUuid("Owner", this.owner);
        }
        if (this.variant != null) {
            tag.putString(DragonVariant.DATA_PARAMETER_KEY, this.variant);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        if (tag.contains(DragonType.DATA_PARAMETER_KEY)) {
            this.setDragonType(DragonType.REGISTRY.get(new Identifier(tag.getString(DragonType.DATA_PARAMETER_KEY))), false);
        }
        if (tag.contains(DragonVariant.DATA_PARAMETER_KEY)) {
            this.variant = tag.getString(DragonVariant.DATA_PARAMETER_KEY);
        }
        if (tag.contains(AGE_DATA_PARAMETER_KEY)) {
            this.setAge(tag.getInt(AGE_DATA_PARAMETER_KEY), true);
        }
        if (tag.containsUuid("Owner")) {
            this.owner = tag.getUuid("Owner");
        } else {
            MinecraftServer server = this.getServer();
            if (server != null) {
                this.owner = ServerConfigHandler.getPlayerUuidByName(server, tag.getString("Owner"));
            }
        }
    }

    protected void spawnScales(int amount) {
        if (amount > 0) {
            DragonScalesItem scales = this.getDragonType().getInstance(DragonScalesItem.class, null);
            if (scales != null && this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
                this.dropStack(new ItemStack(scales, amount), 1.25F);
            }
        }
    }

    public void hatch() {
        if (!this.world.isClient) {
            this.spawnScales(this.random.nextInt(4) + 4);
            this.hatched = true;
        }
        this.remove();
    }

    public boolean isHatched() {
        return this.hatched;
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return Collections.singleton(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {}

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (ServerConfig.INSTANCE.debug.get() && this.isAlive() && player.getStackInHand(hand).getItem() == Items.DEBUG_STICK) {
            if (player.isSneaking() || this.world.isClient) {
                LOGGER.info("amplitude: {};   axis: {}", this.shaking, this.rotationAxis);
            } else {
                Random random = this.random;
                PacketByteBuf buffer = PacketByteBufs.create().writeVarInt(this.getEntityId());
                buffer
                        .writeByte(this.shaking = random.nextInt(21) + 10)//[10, 30]
                        .writeByte(random.nextInt(180))
                        .writeByte(
                                (random.nextBoolean() ? 0b10 : 0) | (this.age > EGG_CRACK_THRESHOLD ? 0b01 : 0)
                        );
                for (ServerPlayerEntity observer : PlayerLookup.tracking(this)) {
                    ServerPlayNetworking.send(observer, DMPackets.SHAKE_DRAGON_EGG_PACKET_ID, buffer);
                }
            }
            return ActionResult.SUCCESS;
        }
        if (this.isAlive() && player.isSneaking()) {
            HatchableDragonEggBlock block = this.getDragonType().getInstance(HatchableDragonEggBlock.class, null);
            if (block == null) return ActionResult.FAIL;
            this.remove();
            this.world.setBlockState(this.getBlockPos(), block.getDefaultState());
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void tick() {
        World level = this.world;
        Random random = this.random;
        super.tick();
        if (level.isClient) {
            if (--this.shaking > 0) {
                this.amplitudeO = this.amplitude;
                this.amplitude = MathHelper.sin(level.getTime() * 0.5F) * Math.min(this.shaking, 15);
            }
            // spawn generic particles
            DragonType type = this.getDragonType();
            Vec3d pos = this.getPos();
            double px = pos.x + (random.nextDouble() - 0.5);
            double py = pos.y + (random.nextDouble() - 0.3);
            double pz = pos.z + (random.nextDouble() - 0.5);
            double ox = (random.nextDouble() - 0.5) * 2;
            double oy = (random.nextDouble() - 0.3) * 2;
            double oz = (random.nextDouble() - 0.5) * 2;
            level.addParticle(type.eggParticle, px, py, pz, ox, oy, oz);
            if ((++this.age & 1) == 0 && type != DragonTypes.ENDER) {
                int color = type.color;
                level.addParticle(new DustParticleEffect(getColor(color, 2), getColor(color, 1), getColor(color, 0), 1.0F), px, py + 0.8, pz, ox, oy, oz);
            }
            return;
        }
        --this.shaking;
        // play the egg shake animation based on the time the eggs take to hatch
        if (++this.age > EGG_SHAKE_THRESHOLD && this.amplitude == 0) {
            float progress = (float) this.age / MIN_HATCHING_TIME;
            // wait until the egg is nearly hatched
            float chance = (progress - EGG_SHAKE_PROCESS_THRESHOLD) / EGG_SHAKE_BASE_CHANCE * (1 - EGG_SHAKE_PROCESS_THRESHOLD);
            if (this.age >= MIN_HATCHING_TIME && random.nextFloat() * 2 < chance) {
                this.hatch();
                return;
            }
            if (random.nextFloat() < chance) {
                boolean crack = progress > EGG_CRACK_PROCESS_THRESHOLD;
                if (crack) {
                    this.spawnScales(1);
                }
                PacketByteBuf buffer = PacketByteBufs.create().writeVarInt(this.getEntityId());
                buffer
                        .writeByte(this.shaking = random.nextInt(21) + 10)//[10, 30]
                        .writeByte(random.nextInt(180))
                        .writeByte(
                                (random.nextBoolean() ? 0b10 : 0) | (crack ? 0b01 : 0)
                        );
                for (ServerPlayerEntity player : PlayerLookup.tracking(this)) {
                    ServerPlayNetworking.send(player, DMPackets.SHAKE_DRAGON_EGG_PACKET_ID, buffer);
                }
            }
        }
        if (level.getGameRules().getBoolean(DMGameRules.IS_EGG_PUSHABLE)) {
            List<Entity> list = level.getOtherEntities(this, this.getBoundingBox().expand(0.125F, -0.01F, 0.125F), this.pushablePredicate);
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    this.pushAwayFrom(entity);
                }
            }
        }
    }

    @Override
    public ItemStack getPickedStack(@Nullable PlayerEntity player, @Nullable HitResult result) {
        return new ItemStack(this.getDragonType().getInstance(HatchableDragonEggBlock.class, DMBlocks.ENDER_DRAGON_EGG));
    }

    @Override
    protected Text getDefaultName() {
        return this.getDragonType().getFormattedName("entity.dragonmounts.dragon_egg.name");
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return super.isInvulnerableTo(source) || this.getDragonType().isInvulnerableTo(source);
    }

    @Override
    protected boolean canClimb() {
        return false;
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return this.world.getGameRules().getBoolean(DMGameRules.IS_EGG_PUSHABLE) && super.isPushable();
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        if (this.world.getGameRules().getBoolean(DMGameRules.IS_EGG_PUSHABLE)) {
            super.pushAwayFrom(entity);
        }
    }

    @Override
    protected boolean isImmobile() {
        return false;
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    @Override
    public boolean canBreatheInWater() {
        return true;
    }

    @Override
    public void onStruckByLightning(ServerWorld level, LightningEntity lightning) {
        super.onStruckByLightning(level, lightning);
        addOrMergeEffect(this, StatusEffects.STRENGTH, 700, 0, false, true, true);//35s
        DragonType current = this.getDragonType();
        if (current == DragonTypes.SKELETON) {
            this.setDragonType(DragonTypes.WITHER, false);
        } else if (current == DragonTypes.WATER) {
            this.setDragonType(DragonTypes.STORM, false);
        } else return;
        this.playSound(SoundEvents.BLOCK_END_PORTAL_SPAWN, 2, 1);
        this.playSound(SoundEvents.BLOCK_PORTAL_TRIGGER, 2, 1);
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, DMPackets.SYNC_EGG_AGE_PACKET_ID, PacketByteBufs.create().writeVarInt(this.getEntityId()).writeVarInt(this.age));
    }

    public void setAge(int age, boolean lazySync) {
        if (lazySync && this.age != age) {
            PacketByteBuf buffer = PacketByteBufs.create().writeVarInt(this.getEntityId()).writeVarInt(age);
            for (ServerPlayerEntity player : PlayerLookup.tracking(this)) {
                ServerPlayNetworking.send(player, DMPackets.SYNC_EGG_AGE_PACKET_ID, buffer);
            }
        }
        this.age = age;
    }

    public int getAge() {
        return this.age;
    }

    public float getRotationAxis() {
        return this.rotationAxis;
    }

    public float getAmplitude(float partialTicks) {
        return this.shaking <= 0 ? 0 : MathHelper.lerp(partialTicks, this.amplitudeO, this.amplitude);
    }

    @Environment(EnvType.CLIENT)
    public void syncShake(int amplitude, int axis, boolean crack) {
        World level = this.world;
        this.shaking = amplitude;
        this.rotationAxis = axis * TO_RAD_FACTOR;
        // use game time to make amplitude consistent between clients
        float target = MathHelper.sin(level.getTime() * 0.5F) * Math.min(amplitude, 15);
        // multiply with a factor to make it smoother
        this.amplitudeO = target * 0.25F;
        this.amplitude = target * 0.75F;
        if (crack) {
            level.syncWorldEvent(2001, this.getBlockPos(), Block.getRawIdFromState(
                    this.getDragonType().getInstance(HatchableDragonEggBlock.class, DMBlocks.ENDER_DRAGON_EGG).getDefaultState())
            );
        }
        Vec3d pos = this.getPos();
        level.playSound(pos.x, pos.y, pos.z, DMSounds.DRAGON_HATCHING, SoundCategory.NEUTRAL, 1.0F, 1.0F, false);
    }

    public final void setDragonType(DragonType type, boolean reset) {
        AttributeContainer manager = this.getAttributes();
        manager.removeModifiers(this.getDragonType().attributes);
        this.dataTracker.set(DATA_DRAGON_TYPE, type);
        manager.addTemporaryModifiers(type.attributes);
        if (reset) {
            EntityAttributeInstance health = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (health != null) {
                this.setHealth((float) health.getValue());
            }
        }
    }

    @Override
    public final void setDragonType(DragonType type) {
        this.setDragonType(type, false);
    }

    @Override
    public final DragonType getDragonType() {
        return this.dataTracker.get(DATA_DRAGON_TYPE);
    }
}
