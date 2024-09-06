package net.dragonmounts.entity.dragon;

import net.dragonmounts.api.ConditionalShearable;
import net.dragonmounts.api.DragonTypified;
import net.dragonmounts.client.ClientDragonEntity;
import net.dragonmounts.init.DMItems;
import net.dragonmounts.init.DMSounds;
import net.dragonmounts.init.DragonVariants;
import net.dragonmounts.inventory.DragonInventory;
import net.dragonmounts.item.DragonArmorItem;
import net.dragonmounts.item.DragonScalesItem;
import net.dragonmounts.item.DragonSpawnEggItem;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.registry.DragonVariant;
import net.dragonmounts.tag.DMItemTags;
import net.dragonmounts.util.DragonFood;
import net.dragonmounts.util.math.MathUtil;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.dragonmounts.DragonMounts.makeId;
import static net.dragonmounts.init.DMGameRules.*;

/**
 * @see Mule
 * @see Horse
 */
public abstract class TameableDragonEntity extends TamableAnimal implements ExtendedScreenHandlerFactory<TameableDragonEntity>, HasCustomInventoryScreen, ConditionalShearable, DragonTypified.Mutable {
    public static TameableDragonEntity construct(EntityType<? extends TameableDragonEntity> type, Level level) {
        return level.isClientSide ? new ClientDragonEntity(type, level) : new ServerDragonEntity(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, DEFAULT_DRAGON_BASE_HEALTH)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FLYING_SPEED, BASE_AIR_SPEED)
                .add(Attributes.MOVEMENT_SPEED, BASE_GROUND_SPEED)
                .add(Attributes.ATTACK_DAMAGE, DEFAULT_DRAGON_BASE_DAMAGE)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.FOLLOW_RANGE, BASE_FOLLOW_RANGE)
                .add(Attributes.ARMOR, DEFAULT_DRAGON_BASE_ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS, BASE_TOUGHNESS);
    }

    public static final ResourceLocation STAGE_MODIFIER_ID = makeId("dragon_stage_bonus");
    // base attributes
    public static final double BASE_GROUND_SPEED = 0.4;
    public static final double BASE_AIR_SPEED = 0.9;
    public static final double BASE_TOUGHNESS = 30.0D;
    public static final double BASE_FOLLOW_RANGE = 64;
    public static final double BASE_FOLLOW_RANGE_FLYING = BASE_FOLLOW_RANGE * 2;
    public static final int HOME_RADIUS = 64;
    public static final double LIFTOFF_THRESHOLD = 10;
    protected static final Logger LOGGER = LogManager.getLogger();
    // data value IDs
    private static final EntityDataAccessor<Boolean> DATA_FLYING = SynchedEntityData.defineId(TameableDragonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_AGE_LOCKED = SynchedEntityData.defineId(TameableDragonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CAN_HOVER = SynchedEntityData.defineId(TameableDragonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SHEARED = SynchedEntityData.defineId(TameableDragonEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<ItemStack> DATA_CHEST_ITEM = SynchedEntityData.defineId(TameableDragonEntity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> DATA_SADDLE_ITEM = SynchedEntityData.defineId(TameableDragonEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<DragonVariant> DATA_DRAGON_VARIANT = SynchedEntityData.defineId(TameableDragonEntity.class, DragonVariant.SERIALIZER);
    public static final String AGE_DATA_PARAMETER_KEY = "Age";
    public static final String AGE_LOCKED_DATA_PARAMETER_KEY = "AgeLocked";
    public static final String FLYING_DATA_PARAMETER_KEY = "Flying";
    public static final String SADDLE_DATA_PARAMETER_KEY = "Saddle";
    public static final String SHEARED_DATA_PARAMETER_KEY = "ShearCooldown";
    public EndCrystal nearestCrystal;
    protected DragonLifeStage stage = DragonLifeStage.ADULT;
    protected boolean hasChest = false;
    protected boolean isSaddled = false;
    protected int flightTicks = 0;
    protected int crystalTicks;
    protected int shearCooldown;
    public int roarTicks;
    protected int ticksSinceLastAttack;
    protected boolean altBreathing;
    protected boolean isGoingDown;
    public final DragonInventory inventory = new DragonInventory(
            this,
            DATA_CHEST_ITEM,
            hasChest -> this.hasChest = hasChest,
            DATA_SADDLE_ITEM,
            isSaddled -> this.isSaddled = isSaddled
    );

    public TameableDragonEntity(EntityType<? extends TameableDragonEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.resetAttributes();
    }

    @SuppressWarnings("DataFlowIssue")
    public void resetAttributes() {
        GameRules rules = this.level().getGameRules();
        AttributeMap map = this.getAttributes();
        map.getInstance(Attributes.MAX_HEALTH).setBaseValue(rules.getRule(DRAGON_BASE_HEALTH).get());
        map.getInstance(Attributes.ATTACK_DAMAGE).setBaseValue(rules.getRule(DRAGON_BASE_DAMAGE).get());
        map.getInstance(Attributes.ARMOR).setBaseValue(rules.getRule(DRAGON_BASE_ARMOR).get());
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return true;
    }

    @Override
    public boolean isBodyArmorItem(ItemStack stack) {
        return stack.getItem() instanceof DragonArmorItem;
    }

    public void inventoryChanged() {
    }

    public final DragonVariant getVariant() {
        return this.entityData.get(DATA_DRAGON_VARIANT);
    }

    public final void setVariant(DragonVariant variant) {
        this.entityData.set(DATA_DRAGON_VARIANT, variant);
    }

    /**
     * Returns the int-precision distance to solid ground.
     *
     * @param limit an inclusive limit to reduce iterations
     */
    public int getAltitude(int limit) {
        BlockPos.MutableBlockPos pos = this.blockPosition().mutable();
        Level level = this.level();
        if (level.getBlockState(pos).isSolid()) return 0;// your dragon might get stuck in block!
        final int min = 0;// world lowest build height
        int i = 0;
        int y = pos.getY();
        do {
            if (--y < min) return limit;// void
            pos.setY(y);
            if (level.getBlockState(pos).isSolid()) return i;// ground
        } while (++i < limit);
        return limit;
    }

    /**
     * Returns the distance to the ground while the entity is flying.
     */
    public int getAltitude() {
        return this.getAltitude(this.level().getMaxLightLevel());
    }

    public boolean isHighEnough() {
        return this.isHighEnough((int) (8.4F * this.getAgeScale()));
    }

    public boolean isHighEnough(int height) {
        return this.getAltitude(height) >= height;
    }

    public int getMaxDeathTime() {
        return 120;
    }

    public final boolean isFlying() {
        return this.entityData.get(DATA_FLYING);
    }

    public final boolean canHover() {
        return this.entityData.get(DATA_CAN_HOVER);
    }

    protected abstract void checkCrystals();

    protected EndCrystal findCrystal() {
        EndCrystal result = null;
        double min = Double.MAX_VALUE;
        for (var crystal : this.level().getEntitiesOfClass(EndCrystal.class, this.getBoundingBox().inflate(32.0))) {
            double distance = crystal.distanceToSqr(this);
            if (distance < min) {
                min = distance;
                result = crystal;
            }
        }
        return result;
    }

    //----------Entity----------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLYING, false);
        builder.define(DATA_SHEARED, false);
        builder.define(DATA_AGE_LOCKED, false);
        builder.define(DATA_CAN_HOVER, true);
        builder.define(DATA_SADDLE_ITEM, ItemStack.EMPTY);
        builder.define(DATA_CHEST_ITEM, ItemStack.EMPTY);
        builder.define(DATA_DRAGON_VARIANT, DragonVariants.ENDER_FEMALE);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_SADDLE_ITEM.equals(accessor)) {
            this.inventory.saddle.setLocal(this.entityData.get(DATA_SADDLE_ITEM), false);
        } else if (DATA_CHEST_ITEM.equals(accessor)) {
            this.inventory.chest.setLocal(this.entityData.get(DATA_CHEST_ITEM), false);
        } else {
            super.onSyncedDataUpdated(accessor);
        }
    }

    @Nullable
    @Override
    public TameableDragonEntity getBreedOffspring(ServerLevel level, AgeableMob parent) {
        return null;
    }

    public boolean hasChest() {
        return this.hasChest;
    }

    public final boolean isSaddled() {
        return this.isSaddled;
    }

    @Override
    public void refreshDimensions() {
        Vec3 pos = this.position();
        super.refreshDimensions();
        this.setPos(pos.x, pos.y, pos.z);
    }

    @Override
    public float getAgeScale() {
        return DragonLifeStage.getSize(this.stage, this.age);
    }

    @Override
    public float getScale() {
        return DragonLifeStage.getSizeAverage(this.stage);
    }

    public @NotNull EntityDimensions getDefaultDimensions(Pose pose) {
        return this.getType().getDimensions();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return !stack.isEmpty() && DragonFood.test(stack.getItem());
    }

    /*@Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }*/

    @Override
    protected @NotNull ResourceKey<LootTable> getDefaultLootTable() {
        return this.getDragonType().getLootTable();
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        this.inventory.dropContents(false, 0);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        Entity entity = source.getEntity();
        return (entity != null && (entity == this || this.hasPassenger(entity))) || super.isInvulnerableTo(source) || this.getDragonType().isInvulnerableTo(source);
    }

    @Override
    protected int calculateFallDamage(float distance, float damageMultiplier) {return 0;}

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {return false;}

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    public abstract void setLifeStage(DragonLifeStage stage, boolean reset, boolean sync);

    public final DragonLifeStage getLifeStage() {
        return this.stage;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(this.getDragonType().getInstance(DragonSpawnEggItem.class, DMItems.ENDER_DRAGON_SPAWN_EGG));
    }

    @Override
    public Player getControllingPassenger() {
        return !this.isNoAi() && this.getFirstPassenger() instanceof Player player ? player : null;
    }

    @Override
    protected @NotNull Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float partialTick) {
        int index = this.getPassengers().indexOf(entity);
        return this.getDragonType().passengerLocator.locate(index == -1 ? 0 : index, this.isInSittingPose()).scale(this.getScale()).yRot(MathUtil.TO_RAD_FACTOR * -this.yBodyRot);
    }

    @Override
    protected @NotNull Component getTypeName() {
        return this.getDragonType().getFormattedName("entity.dragonmounts.dragon.name");
    }

    //----------MobEntity----------

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.is(MobEffects.WEAKNESS) && super.canBeAffected(effect);
    }

    /*@Override
    public EntityNavigation getNavigation() {
        if (this.hasVehicle()) {//?
            Entity vehicle = this.getVehicle();
            if (vehicle instanceof MobEntity) {
                return ((MobEntity) vehicle).getNavigation();
            }
        }
        return this.isFlying() ? this.flyingNavigation : this.groundNavigation;
    }*/

    /*@Override
    protected DragonBodyController createBodyControl() {
        return new DragonBodyController(this);
    }*/

    /*@Override
    public int getMaxHeadYRot() {
        return 90;
    }*/

    @Override
    public boolean canControlVehicle() {
        return super.canControlVehicle();
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
    }

    public @NotNull SlotAccess getSlot(int slot) {
        return switch (slot) {
            case 400 -> this.inventory.saddle;
            case 499 -> this.inventory.chest;
            default -> {
                var access = this.inventory.access(slot);
                yield access == SlotAccess.NULL ? super.getSlot(slot) : access;
            }
        };
    }

    //----------AgeableEntity----------

    protected void refreshAge() {
        switch (this.stage.ordinal()) {
            case 0:// NEWBORN
            case 1:// INFANT
                this.age = -this.stage.duration;
                return;
            case 2:// JUVENILE
            case 3:// PREJUVENILE
                this.age = this.stage.duration;
                return;
            default:
                this.age = 0;
        }
    }

    public void setAgeLocked(boolean locked) {
        this.entityData.set(DATA_AGE_LOCKED, locked);
    }

    public final boolean isAgeLocked() {
        return this.entityData.get(DATA_AGE_LOCKED);
    }

    @Override
    protected void ageBoundaryReached() {
        this.setLifeStage(DragonLifeStage.byId(this.stage.ordinal() + 1), true, false);
    }

    @Override
    public void ageUp(int amount, boolean forced) {
        int old = this.age;
        //Notice:                           ↓↓                      ↓↓              ↓↓           ↓↓
        if (!this.isAgeLocked() && (old < 0 && (this.age += amount) >= 0 || old > 0 && (this.age -= amount) <= 0)) {
            this.ageBoundaryReached();
            if (forced) {
                this.forcedAge += old < 0 ? -old : old;
            }
        }
    }

    @Override
    public final int getAge() {
        return this.age;
    }

    @Override
    public final void setBaby(boolean value) {
        this.setLifeStage(value ? DragonLifeStage.NEWBORN : DragonLifeStage.ADULT, true, true);
    }

    @Override
    protected int getBaseExperienceReward() {
        return 0;
    }

    //----------IDragonTypified.Mutable----------

    public final void setDragonType(DragonType type, boolean reset) {
        var manager = this.getAttributes();
        var previous = this.getVariant();
        manager.removeAttributeModifiers(previous.type.attributes);
        if (previous.type != type || reset) {
            this.setVariant(type.variants.draw(this.random, previous, true));
        }
        manager.addTransientAttributeModifiers(type.attributes);
        if (reset) {
            this.setHealth((float) manager.getValue(Attributes.MAX_HEALTH));
        }
    }

    @Override
    public final void setDragonType(DragonType type) {
        this.setDragonType(type, false);
    }

    @Override
    public final DragonType getDragonType() {
        return this.getVariant().type;
    }

    //----------IForgeShearable----------

    public final boolean isSheared() {
        return this.entityData.get(DATA_SHEARED);
    }

    public final void setSheared(int cooldown) {
        this.shearCooldown = cooldown;
        this.entityData.set(DATA_SHEARED, cooldown > 0);
    }

    @Override
    public boolean readyForShearing(Level level, ItemStack stack) {
        return this.isAlive() && this.stage.ordinal() >= 2 && !this.isSheared() && stack.is(DMItemTags.HARD_SHEARS);
    }

    @Override
    public boolean shear(Level level, @Nullable Player player, ItemStack stack, BlockPos pos, SoundSource source) {
        var scale = this.getDragonType().getInstance(DragonScalesItem.class, null);
        if (scale == null) return false;
        level.playSound(player, this, DMSounds.DRAGON_GROWL, source, 1.0F, 1.0F);
        var random = this.random;
        var item = this.spawnAtLocation(new ItemStack(scale), 1.0F);
        if (item != null) {
            item.setDeltaMovement(item.getDeltaMovement().add(
                    (random.nextFloat() - random.nextFloat()) * 0.1F,
                    random.nextFloat() * 0.05F,
                    (random.nextFloat() - random.nextFloat()) * 0.1F
            ));
        }
        this.setSheared(2500 + random.nextInt(1000));
        return true;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data) {
        if (data instanceof DragonSpawnData $data) {
            this.setLifeStage($data.stage, true, false);
        } else {
            data = new DragonSpawnData(DragonLifeStage.ADULT);
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }
}