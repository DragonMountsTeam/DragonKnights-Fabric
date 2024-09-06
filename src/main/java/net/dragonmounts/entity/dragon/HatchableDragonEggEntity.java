package net.dragonmounts.entity.dragon;

import net.dragonmounts.api.DragonTypified;
import net.dragonmounts.api.ScoreboardAccessor;
import net.dragonmounts.block.HatchableDragonEggBlock;
import net.dragonmounts.config.ServerConfig;
import net.dragonmounts.init.*;
import net.dragonmounts.item.DragonScalesItem;
import net.dragonmounts.network.ShakeEggPayload;
import net.dragonmounts.network.SyncEggAgePayload;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.registry.DragonVariant;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Predicate;

import static net.dragonmounts.entity.dragon.TameableDragonEntity.AGE_DATA_PARAMETER_KEY;
import static net.dragonmounts.init.DMGameRules.DEFAULT_DRAGON_BASE_HEALTH;
import static net.dragonmounts.init.DMGameRules.DRAGON_BASE_HEALTH;
import static net.dragonmounts.util.EntityUtil.addOrMergeEffect;
import static net.dragonmounts.util.math.MathUtil.TO_RAD_FACTOR;
import static net.minecraft.resources.ResourceLocation.tryParse;

public class HatchableDragonEggEntity extends LivingEntity implements DragonTypified.Mutable {
    protected static final EntityDataAccessor<DragonType> DATA_DRAGON_TYPE = SynchedEntityData.defineId(HatchableDragonEggEntity.class, DragonType.SERIALIZER);
    private static final Logger LOGGER = LogManager.getLogger();
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

    public static HatchableDragonEggEntity construct(EntityType<? extends HatchableDragonEggEntity> type, Level level) {
        return new HatchableDragonEggEntity(type, level);
    }

    public HatchableDragonEggEntity(EntityType<? extends HatchableDragonEggEntity> type, Level level) {
        super(type, level);
        //noinspection DataFlowIssue
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(level.getGameRules().getRule(DRAGON_BASE_HEALTH).get());
    }

    public HatchableDragonEggEntity(Level level) {
        this(DMEntities.HATCHABLE_DRAGON_EGG, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, DEFAULT_DRAGON_BASE_HEALTH)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_DRAGON_TYPE, DragonTypes.ENDER);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(DragonType.DATA_PARAMETER_KEY, this.getDragonType().identifier.toString());
        tag.putInt(AGE_DATA_PARAMETER_KEY, this.age);
        if (this.owner != null) {
            tag.putUUID("Owner", this.owner);
        }
        if (this.variant != null) {
            tag.putString(DragonVariant.DATA_PARAMETER_KEY, this.variant);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(DragonType.DATA_PARAMETER_KEY)) {
            this.setDragonType(DragonType.REGISTRY.get(tryParse(tag.getString(DragonType.DATA_PARAMETER_KEY))), false);
        }
        if (tag.contains(DragonVariant.DATA_PARAMETER_KEY)) {
            this.variant = tag.getString(DragonVariant.DATA_PARAMETER_KEY);
        }
        if (tag.contains(AGE_DATA_PARAMETER_KEY)) {
            this.setAge(tag.getInt(AGE_DATA_PARAMETER_KEY), !this.firstTick);
        }
        if (tag.hasUUID("Owner")) {
            this.owner = tag.getUUID("Owner");
        } else {
            this.owner = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), tag.getString("Owner"));
        }
    }

    protected void spawnScales(int amount) {
        if (amount > 0) {
            DragonScalesItem scales = this.getDragonType().getInstance(DragonScalesItem.class, null);
            if (scales != null && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                this.spawnAtLocation(new ItemStack(scales, amount), 1.25F);
            }
        }
    }

    @Override
    public void setLevelCallback(EntityInLevelCallback callback) {
        if (this.hatched && callback == EntityInLevelCallback.NULL) {
            Level level = this.level();
            level.addFreshEntity(new ServerDragonEntity(this, DragonLifeStage.NEWBORN));
        }
        super.setLevelCallback(callback);
    }

    public void hatch() {
        if (!this.level().isClientSide) {
            this.spawnScales(this.random.nextInt(4) + 4);
            this.hatched = true;
            ((ScoreboardAccessor) this.level().getScoreboard()).dragonmounts$preventRemoval(this);
        }
        this.discard();
    }

    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return Collections.singleton(ItemStack.EMPTY);
    }

    @Override
    public @NotNull ItemStack getItemBySlot(EquipmentSlot slot) {return ItemStack.EMPTY;}

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {}

    @Override
    public @NotNull HumanoidArm getMainArm() {return HumanoidArm.RIGHT;}

    @Override
    public @NotNull InteractionResult interact(Player player, InteractionHand hand) {
        if (ServerConfig.INSTANCE.debug.get() && this.isAlive() && player.getItemInHand(hand).getItem() == Items.DEBUG_STICK) {
            if (player.isShiftKeyDown() || this.level().isClientSide) {
                LOGGER.info("amplitude: {};   axis: {}", this.shaking, this.rotationAxis);
            } else {
                var random = this.random;
                int flag = this.age > EGG_CRACK_THRESHOLD ? 0b01 : 0b00;
                var payload = new ShakeEggPayload(
                        this.getId(),
                        this.shaking = random.nextIntBetweenInclusive(10, 30),
                        random.nextInt(180),
                        random.nextBoolean() ? 0b10 | flag : flag
                );
                for (var target : PlayerLookup.tracking(this)) {
                    ServerPlayNetworking.send(target, payload);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (this.isAlive() && player.isShiftKeyDown()) {
            var block = this.getDragonType().getInstance(HatchableDragonEggBlock.class, null);
            if (block == null) return InteractionResult.FAIL;
            this.discard();
            this.level().setBlock(this.blockPosition(), block.defaultBlockState(), 3);
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void tick() {
        var level = this.level();
        var random = this.random;
        super.tick();
        if (level.isClientSide) {
            if (--this.shaking > 0) {
                this.amplitudeO = this.amplitude;
                this.amplitude = Mth.sin(level.getGameTime() * 0.5F) * Math.min(this.shaking, 15);
            }
            // spawn generic particles
            var type = this.getDragonType();
            double px = getX() + (random.nextDouble() - 0.5);
            double py = getY() + (random.nextDouble() - 0.3);
            double pz = getZ() + (random.nextDouble() - 0.5);
            double ox = (random.nextDouble() - 0.5) * 2;
            double oy = (random.nextDouble() - 0.3) * 2;
            double oz = (random.nextDouble() - 0.5) * 2;
            level.addParticle(type.eggParticle, px, py, pz, ox, oy, oz);
            if ((++this.age & 1) == 0 && type != DragonTypes.ENDER) {
                level.addParticle(new DustParticleOptions(type.colorVector, 1.0F), px, py + 0.8, pz, ox, oy, oz);
            }
            return;
        }
        --this.shaking;
        // play the egg shake animation based on the time the eggs take to hatch
        if (++this.age > EGG_SHAKE_THRESHOLD && this.shaking < 0) {
            float progress = (float) this.age / MIN_HATCHING_TIME;
            // wait until the egg is nearly hatched
            float chance = (progress - EGG_SHAKE_PROCESS_THRESHOLD) / EGG_SHAKE_BASE_CHANCE * (1 - EGG_SHAKE_PROCESS_THRESHOLD);
            if (this.age >= MIN_HATCHING_TIME && random.nextFloat() * 2 < chance) {
                this.hatch();
                return;
            }
            if (random.nextFloat() < chance) {
                boolean crack = progress > EGG_CRACK_PROCESS_THRESHOLD;
                int flag = crack ? 0b01 : 0b00;
                var payload = new ShakeEggPayload(
                        this.getId(),
                        this.shaking = random.nextInt(21) + 10,//[10, 30]
                        random.nextInt(180),
                        random.nextBoolean() ? 0b10 | flag : flag
                );
                if (crack) {
                    this.spawnScales(1);
                }
                for (var player : PlayerLookup.tracking(this)) {
                    ServerPlayNetworking.send(player, payload);
                }
            }
        }
        if (level.getGameRules().getBoolean(DMGameRules.IS_EGG_PUSHABLE)) {
            var list = level.getEntities(this, this.getBoundingBox().inflate(0.125F, -0.01F, 0.125F), this.pushablePredicate);
            if (!list.isEmpty()) {
                for (var entity : list) {
                    this.push(entity);
                }
            }
        }
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(this.getDragonType().getInstance(HatchableDragonEggBlock.class, DMBlocks.ENDER_DRAGON_EGG));
    }

    @Override
    protected @NotNull Component getTypeName() {
        return this.getDragonType().getFormattedName("entity.dragonmounts.dragon_egg.name");
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return super.isInvulnerableTo(source) || this.getDragonType().isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        var weapon = source.getWeaponItem();
        if (weapon != null && (weapon.is(ItemTags.MACE_ENCHANTABLE))) {
            if (super.hurt(source, Math.max(20F, amount * 2F))) {
                this.spawnScales(1);
                return true;
            }
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected Entity.@NotNull MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return this.level().getGameRules().getBoolean(DMGameRules.IS_EGG_PUSHABLE) && super.isPushable();
    }

    @Override
    public void push(Entity entity) {
        if (this.level().getGameRules().getBoolean(DMGameRules.IS_EGG_PUSHABLE)) {
            super.push(entity);
        }
    }

    @Override
    protected boolean isImmobile() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt bolt) {
        super.thunderHit(level, bolt);
        addOrMergeEffect(this, MobEffects.DAMAGE_BOOST, 700, 0, false, true, true);//35s
        var current = this.getDragonType();
        if (current == DragonTypes.SKELETON) {
            this.setDragonType(DragonTypes.WITHER, false);
        } else if (current == DragonTypes.WATER) {
            this.setDragonType(DragonTypes.STORM, false);
        } else return;
        this.playSound(SoundEvents.END_PORTAL_SPAWN, 2, 1);
        this.playSound(SoundEvents.PORTAL_TRIGGER, 2, 1);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        ServerPlayNetworking.send(player, new SyncEggAgePayload(this.getId(), this.age));
    }

    public void setAge(int age, boolean lazySync) {
        if (lazySync && this.age != age) {
            var payload = new SyncEggAgePayload(this.getId(), age);
            for (var player : PlayerLookup.tracking(this)) {
                ServerPlayNetworking.send(player, payload);
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
        return this.shaking <= 0 ? 0 : Mth.lerp(partialTicks, this.amplitudeO, this.amplitude);
    }

    public void syncShake(int amplitude, int axis, boolean crack) {
        var level = this.level();
        this.shaking = amplitude;
        this.rotationAxis = axis * TO_RAD_FACTOR;
        // use game time to make amplitude consistent between clients
        float target = Mth.sin(level.getGameTime() * 0.5F) * Math.min(amplitude, 15);
        // multiply with a factor to make it smoother
        this.amplitudeO = target * 0.25F;
        this.amplitude = target * 0.75F;
        if (crack) {
            level.levelEvent(2001, this.blockPosition(), Block.getId(
                    this.getDragonType().getInstance(HatchableDragonEggBlock.class, DMBlocks.ENDER_DRAGON_EGG).defaultBlockState()
            ));
        }
        level.playLocalSound(this, DMSounds.DRAGON_HATCHING, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    public final void setDragonType(DragonType type, boolean reset) {
        var manager = this.getAttributes();
        manager.removeAttributeModifiers(this.getDragonType().attributes);
        this.entityData.set(DATA_DRAGON_TYPE, type);
        manager.addTransientAttributeModifiers(type.attributes);
        if (reset) {
            AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
            assert health != null;
            this.setHealth((float) health.getValue());
        }
    }

    @Override
    public final void setDragonType(DragonType type) {
        this.setDragonType(type, false);
    }

    @Override
    public final DragonType getDragonType() {
        return this.entityData.get(DATA_DRAGON_TYPE);
    }
}
