package net.dragonmounts.entity.dragon;

import net.dragonmounts.api.IDragonTypified;
import net.dragonmounts.client.ClientDragonEntity;
import net.dragonmounts.data.tag.ForgeTags;
import net.dragonmounts.entity.ai.DragonBodyController;
import net.dragonmounts.entity.ai.DragonMovementController;
import net.dragonmounts.entity.path.DragonFlyingNavigator;
import net.dragonmounts.init.DMItems;
import net.dragonmounts.init.DMSounds;
import net.dragonmounts.init.DragonVariants;
import net.dragonmounts.inventory.DragonInventory;
import net.dragonmounts.inventory.LimitedSlot;
import net.dragonmounts.item.DragonArmorItem;
import net.dragonmounts.item.DragonScalesItem;
import net.dragonmounts.item.DragonSpawnEggItem;
import net.dragonmounts.item.TieredShearsItem;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.registry.DragonVariant;
import net.dragonmounts.util.DragonFood;
import net.dragonmounts.util.math.MathUtil;
import net.fabricmc.fabric.api.entity.EntityPickInteractionAware;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SaddleItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.dragonmounts.init.DMGameRules.*;
import static net.dragonmounts.util.BlockUtil.isSolid;


/**
 * @see net.minecraft.entity.passive.MuleEntity
 * @see net.minecraft.entity.passive.HorseEntity
 */
public abstract class TameableDragonEntity extends TameableEntity implements EntityPickInteractionAware, IDragonTypified.Mutable, Flutterer {
    public static TameableDragonEntity construct(EntityType<? extends TameableDragonEntity> type, World level) {
        return level.isClient ? new ClientDragonEntity(type, level) : new ServerDragonEntity(type, level);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, DEFAULT_DRAGON_BASE_HEALTH)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, BASE_AIR_SPEED)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, BASE_GROUND_SPEED)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, DEFAULT_DRAGON_BASE_DAMAGE)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, BASE_FOLLOW_RANGE)
                .add(EntityAttributes.GENERIC_ARMOR, DEFAULT_DRAGON_BASE_ARMOR)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, BASE_TOUGHNESS);
    }

    public static final UUID AGE_MODIFIER_UUID = UUID.fromString("2d147cda-121b-540e-bb24-435680aa374a");
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
    private static final TrackedData<Boolean> DATA_FLYING = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> DATA_AGE_LOCKED = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> DATA_CAN_HOVER = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> DATA_BREATHING = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> DATA_ALT_BREATHING = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> GOING_DOWN = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> ALLOW_OTHER_PLAYERS = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> HOVER_CANCELLED = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> ALT_TEXTURE = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Optional<UUID>> DATA_BREEDER = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private static final TrackedData<Integer> DATA_REPO_COUNT = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> DATA_SHEARED = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> HAS_ADJUDICATOR_STONE = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> HAS_ELDER_STONE = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    //private static final TrackedData<Boolean> SLEEP = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<String> DATA_BREATH_WEAPON_TARGET = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Integer> DATA_BREATH_WEAPON_MODE = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<ItemStack> DATA_SADDLE_ITEM = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    protected static final TrackedData<ItemStack> DATA_ARMOR_ITEM = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    protected static final TrackedData<ItemStack> DATA_CHEST_ITEM = DataTracker.registerData(TameableDragonEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<DragonVariant> DATA_DRAGON_VARIANT = DataTracker.registerData(TameableDragonEntity.class, DragonVariant.SERIALIZER);
    public static final String AGE_DATA_PARAMETER_KEY = "Age";
    public static final String AGE_LOCKED_DATA_PARAMETER_KEY = "AgeLocked";
    public static final String FLYING_DATA_PARAMETER_KEY = "Flying";
    public static final String SADDLE_DATA_PARAMETER_KEY = "Saddle";
    public static final String SHEARED_DATA_PARAMETER_KEY = "ShearCooldown";
    protected final MobNavigation groundNavigation;
    protected final DragonFlyingNavigator flyingNavigation;
    public EndCrystalEntity nearestCrystal;
    public final DragonInventory inventory = new DragonInventory(this);
    protected DragonLifeStage stage = DragonLifeStage.ADULT;
    protected ItemStack saddle = ItemStack.EMPTY;
    protected ItemStack armor = ItemStack.EMPTY;
    protected ItemStack chest = ItemStack.EMPTY;
    protected boolean hasChest = false;
    protected boolean isSaddled = false;
    protected int flightTicks = 0;
    protected int crystalTicks;
    protected int shearCooldown;
    public int roarTicks;
    protected int ticksSinceLastAttack;
    protected boolean altBreathing;
    protected boolean isGoingDown;

    public TameableDragonEntity(EntityType<? extends TameableDragonEntity> type, World level) {
        super(type, level);
        this.resetAttributes();
        this.stepHeight = 1.0F;
        this.inanimate = true;
        this.moveControl = new DragonMovementController(this);
        (this.flyingNavigation = new DragonFlyingNavigator(this, level)).setCanSwim(true);
        (this.navigation = this.groundNavigation = new MobNavigation(this, level)).setCanSwim(true);
    }

    public void resetAttributes() {
        GameRules rules = this.world.getGameRules();
        AttributeContainer manager = this.getAttributes();
        //noinspection DataFlowIssue
        manager.getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(rules.get(DRAGON_BASE_HEALTH).get());
        //noinspection DataFlowIssue
        manager.getCustomInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(rules.get(DRAGON_BASE_DAMAGE).get());
        //noinspection DataFlowIssue
        manager.getCustomInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(rules.get(DRAGON_BASE_ARMOR).get());
    }

    public void setSaddle(ItemStack stack, boolean sync) {
        boolean prior = this.isSaddled;
        this.isSaddled = stack.getItem() instanceof SaddleItem;
        if (!stack.isEmpty()) {
            stack.setCount(1);
        }
        this.saddle = stack;
        if (this.world.isClient) return;
        if (!prior && this.isSaddled) {
            this.playSound(SoundEvents.ENTITY_HORSE_SADDLE, 0.5F, 1.0F);
        }
        if (sync) {
            this.dataTracker.set(DATA_SADDLE_ITEM, stack.copy());
        }
    }

    public void setArmor(ItemStack stack, boolean sync) {
        if (!stack.isEmpty()) {
            stack.setCount(1);
        }
        this.armor = stack;
        if (this.world.isClient) return;
        EntityAttributeInstance attribute = this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
        if (attribute != null) {
            attribute.removeModifier(DragonArmorItem.MODIFIER_UUID);
            Item item = stack.getItem();
            if (item instanceof DragonArmorItem) {
                attribute.addTemporaryModifier(new EntityAttributeModifier(
                        DragonArmorItem.MODIFIER_UUID,
                        "Dragon armor bonus",
                        ((DragonArmorItem) item).getProtection(),
                        EntityAttributeModifier.Operation.ADDITION
                ));
            }
        }
        if (sync) {
            this.dataTracker.set(DATA_ARMOR_ITEM, stack.copy());
        }
    }

    public void setChest(ItemStack stack, boolean sync) {
        boolean prior = this.hasChest;
        this.hasChest = ForgeTags.Item.CHESTS_WOODEN.contains(stack.getItem());
        if (prior && !this.hasChest) {
            this.inventory.dropContents(true, 0);
        }
        if (!stack.isEmpty()) {
            stack.setCount(1);
        }
        this.chest = stack;
        if (sync && !this.world.isClient) {
            this.dataTracker.set(DATA_CHEST_ITEM, stack.copy());
        }
    }

    public ItemStack getSaddleStack() {
        return this.saddle;
    }

    public ItemStack getArmorStack() {
        return this.armor;
    }

    public ItemStack getChestStack() {
        return this.chest;
    }

    public void inventoryChanged() {}

    public final DragonVariant getVariant() {
        return this.dataTracker.get(DATA_DRAGON_VARIANT);
    }

    public final void setVariant(DragonVariant variant) {
        this.dataTracker.set(DATA_DRAGON_VARIANT, variant);
    }

    /**
     * Returns the int-precision distance to solid ground.
     *
     * @param limit an inclusive limit to reduce iterations
     */
    public int getAltitude(int limit) {
        BlockPos.Mutable pos = this.getBlockPos().mutableCopy();
        if (isSolid(this.world, pos)) return 0;// your dragon might get stuck in block!
        final int min = 0;// world lowest build height
        int i = 0;
        int y = pos.getY();
        do {
            if (--y < min) return limit;// void
            pos.setY(y);
            if (isSolid(this.world, pos)) return i;// ground
        } while (++i < limit);
        return limit;
    }

    /**
     * Returns the distance to the ground while the entity is flying.
     */
    public int getAltitude() {
        return this.getAltitude(this.world.getMaxLightLevel());
    }

    public boolean isHighEnough() {
        return this.isHighEnough((int) (8.4F * this.getScaleFactor()));
    }

    public boolean isHighEnough(int height) {
        return this.getAltitude(height) >= height;
    }

    public float getMaxDeathTime() {
        return 120.0F;
    }

    public final boolean isFlying() {
        return this.dataTracker.get(DATA_FLYING);
    }

    public final boolean canHover() {
        return this.dataTracker.get(DATA_CAN_HOVER);
    }

    protected final void setFlying(boolean flying) {
        this.dataTracker.set(DATA_FLYING, flying);
        this.navigation = flying ? this.flyingNavigation : this.groundNavigation;
    }

    protected abstract void checkCrystals();

    protected EndCrystalEntity findCrystal() {
        EndCrystalEntity result = null;
        double min = Double.MAX_VALUE;
        for (EndCrystalEntity crystal : this.world.getNonSpectatingEntities(EndCrystalEntity.class, this.getBoundingBox().expand(32.0))) {
            double distance = crystal.squaredDistanceTo(this);
            if (distance < min) {
                min = distance;
                result = crystal;
            }
        }
        return result;
    }

    //----------Entity----------

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(DATA_FLYING, false);
        this.dataTracker.startTracking(DATA_SHEARED, false);
        this.dataTracker.startTracking(DATA_AGE_LOCKED, false);
        this.dataTracker.startTracking(DATA_CAN_HOVER, true);
        this.dataTracker.startTracking(DATA_SADDLE_ITEM, ItemStack.EMPTY);
        this.dataTracker.startTracking(DATA_ARMOR_ITEM, ItemStack.EMPTY);
        this.dataTracker.startTracking(DATA_CHEST_ITEM, ItemStack.EMPTY);
        this.dataTracker.startTracking(DATA_DRAGON_VARIANT, DragonVariants.ENDER_FEMALE);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> key) {
        if (DATA_SADDLE_ITEM.equals(key)) {
            this.saddle = this.dataTracker.get(DATA_SADDLE_ITEM);
            this.isSaddled = this.saddle.getItem() instanceof SaddleItem;
        } else if (DATA_ARMOR_ITEM.equals(key)) {
            this.armor = this.dataTracker.get(DATA_ARMOR_ITEM);
        } else if (DATA_CHEST_ITEM.equals(key)) {
            this.chest = this.dataTracker.get(DATA_CHEST_ITEM);
            this.hasChest = ForgeTags.Item.CHESTS_WOODEN.contains(this.chest.getItem());
        } else {
            super.onTrackedDataSet(key);
        }
    }

    @Override
    public TameableDragonEntity createChild(ServerWorld serverWorld, PassiveEntity entity) {
        return null;
    }

    public boolean hasChest() {
        return this.hasChest;
    }

    public final boolean isSaddled() {
        return this.isSaddled;
    }

    @Override
    public final void calculateDimensions() {
        Vec3d pos = this.getPos();
        super.calculateDimensions();
        this.setPosition(pos.x, pos.y, pos.z);
    }

    @Override
    public final float getScaleFactor() {
        return DragonLifeStage.getSize(this.stage, this.breedingAge);
    }

    @Override
    public final EntityDimensions getDimensions(EntityPose pose) {
        return this.getType().getDimensions().scaled(DragonLifeStage.getSizeAverage(this.stage));
    }

    @Override
    public final boolean isBreedingItem(ItemStack stack) {
        return !stack.isEmpty() && DragonFood.test(stack.getItem());
    }

    /*@Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }*/

    @Override
    protected Identifier getLootTableId() {
        return this.getDragonType().lootTable;
    }

    @Override
    protected void dropInventory() {
        super.dropInventory();
        this.inventory.dropContents(false, 0);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        Entity entity = source.getSource();
        return (entity != null && (entity == this || this.hasPassenger(entity))) || super.isInvulnerableTo(source) || this.getDragonType().isInvulnerableTo(source);
    }

    @Override
    public boolean handleFallDamage(float distance, float multiplier) {
        return false;
    }

    @Override
    protected void fall(double y, boolean onGround, BlockState state, BlockPos pos) {}

    public abstract void setLifeStage(DragonLifeStage stage, boolean reset, boolean sync);

    public final DragonLifeStage getLifeStage() {
        return this.stage;
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public ItemStack getPickedStack(@Nullable PlayerEntity player, @Nullable HitResult result) {
        return new ItemStack(
                this.getDragonType().getInstance(DragonSpawnEggItem.class, DMItems.ENDER_DRAGON_SPAWN_EGG)
        );
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return Collections.singletonList(this.getArmorStack());
    }

    @Override
    public Entity getPrimaryPassenger() {
        List<Entity> passengers = this.getPassengerList();
        return passengers.isEmpty() ? null : passengers.get(0);
    }

    public PlayerEntity getControllingPlayer() {
        List<Entity> passengers = this.getPassengerList();
        if (passengers.isEmpty()) return null;
        Entity entity = passengers.get(0);
        return entity instanceof PlayerEntity ? (PlayerEntity) entity : null;
    }

    @Override
    public void updatePassengerPosition(Entity passenger) {
        int index = this.getPassengerList().indexOf(passenger);
        if (index >= 0) {
            Vec3d pos = this.getDragonType().passengerLocator.locate(index, this.isInSittingPose())
                    .multiply(this.getScaleFactor())
                    .rotateY(MathUtil.TO_RAD_FACTOR * -this.bodyYaw)
                    .add(this.getPos());
            passenger.setPosition(pos.x, /*passenger instanceof PlayerEntity ? pos.y - this.getScale() * 0.2D :*/ pos.y, pos.z);
            if (index == 0) {
                this.onPassengerLookAround(passenger);
                passenger.prevPitch = passenger.pitch;
            }
        }
    }

    @Override
    public double getHeightOffset() {
        return this.getDragonType().passengerLocator.locate(0, this.isInSittingPose()).y * this.getScaleFactor();
    }

    @Override
    public float getEyeHeight(EntityPose pose) {
        float height = super.getEyeHeight(pose, this.getDimensions(pose));
        return this.isInSittingPose() ? height * 0.8F : height;
    }

    @Override
    protected Text getDefaultName() {
        return this.getDragonType().getFormattedName("entity.dragonmounts.dragon.name");
    }

    //----------LivingEntity----------

    @Override
    protected void updatePostDeath() {
        if (++this.deathTime >= this.getMaxDeathTime()) {
            this.remove();
            World level = this.world;
            Random random = this.random;
            for (int i = 0; i < 20; ++i) {
                level.addParticle(
                        ParticleTypes.POOF,
                        this.getParticleX(1D),
                        this.getRandomBodyY(),
                        this.getParticleZ(1D),
                        random.nextGaussian() * 0.02,
                        random.nextGaussian() * 0.02,
                        random.nextGaussian() * 0.02
                );
            }
        }
    }

    @Override
    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        return effect.getEffectType() != StatusEffects.WEAKNESS && super.canHaveStatusEffect(effect);
    }

    @Override
    public boolean canBreatheInWater() {
        return true;
    }

    @Override
    public boolean canBeRiddenInWater() {
        return true;
    }

    //----------MobEntity----------

    @Override
    public EntityNavigation getNavigation() {
        if (this.hasVehicle()) {//?
            Entity vehicle = this.getVehicle();
            if (vehicle instanceof MobEntity) {
                return ((MobEntity) vehicle).getNavigation();
            }
        }
        return this.isFlying() ? this.flyingNavigation : this.groundNavigation;
    }

    @Override
    protected DragonBodyController createBodyControl() {
        return new DragonBodyController(this);
    }

    @Override
    public int getBodyYawSpeed() {
        return 90;
    }

    @Override
    public boolean canBeControlledByRider() {
        return this.getPrimaryPassenger() instanceof LivingEntity;
    }

    @Override
    public boolean equip(int index, ItemStack stack) {
        if (index < DragonInventory.INVENTORY_SIZE) {
            return this.inventory.setItemAfterChecked(index, stack);
        }
        switch (index) {
            case 98:
                this.handItems.set(0, stack);
                return true;
            case 99:
                this.handItems.set(1, stack);
                return true;
            case 100:
            case 101:
            case 102:
            case 103:
                if (stack.isEmpty() || LimitedSlot.DragonArmor.canInsert(stack.getItem())) {
                    this.setArmor(stack, true);
                    return true;
                }
            default: return false;
        }
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            this.setArmor(stack, true);
        } else {
            this.handItems.set(slot.getEntitySlotId(), stack);
        }
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            return this.getArmorStack();
        }
        return this.handItems.get(slot.getEntitySlotId());
    }

    //----------AgeableEntity----------

    protected void refreshAge() {
        switch (this.stage.ordinal()) {
            case 0:// NEWBORN
            case 1:// INFANT
                this.breedingAge = -this.stage.duration;
                return;
            case 2:// JUVENILE
            case 3:// PREJUVENILE
                this.breedingAge = this.stage.duration;
                return;
            default:
                this.breedingAge = 0;
        }
    }

    public void setAgeLocked(boolean locked) {
        this.dataTracker.set(DATA_AGE_LOCKED, locked);
    }

    public final boolean isAgeLocked() {
        return this.dataTracker.get(DATA_AGE_LOCKED);
    }

    @Override
    protected void onGrowUp() {
        this.setLifeStage(DragonLifeStage.byId(this.stage.ordinal() + 1), true, false);
    }

    @Override
    public void growUp(int delta, boolean forced) {
        int backup = this.breedingAge;
        //Notice:                                        ↓↓                   ↓↓                                     ↓↓                   ↓↓
        if (!this.isAgeLocked() && (this.breedingAge < 0 && (this.breedingAge += delta) >= 0 || this.breedingAge > 0 && (this.breedingAge -= delta) <= 0)) {
            this.onGrowUp();
            if (forced) {
                this.forcedAge += backup < 0 ? -backup : backup;
            }
        }
    }

    @Override
    public final int getBreedingAge() {
        return this.breedingAge;
    }

    @Override
    public final void setBaby(boolean value) {
        this.setLifeStage(value ? DragonLifeStage.NEWBORN : DragonLifeStage.ADULT, true, true);
    }

    @Override
    protected int getXpToDrop(PlayerEntity player) {
        return 0;
    }

    //----------IDragonTypified.Mutable----------

    public final void setDragonType(DragonType type, boolean reset) {
        AttributeContainer manager = this.getAttributes();
        DragonVariant previous = this.getVariant();
        manager.removeModifiers(previous.type.attributes);
        if (previous.type != type || reset) {
            this.setVariant(type.variants.draw(this.random, previous));
        }
        manager.addTemporaryModifiers(type.attributes);
        if (reset) {
            this.setHealth((float) manager.getValue(EntityAttributes.GENERIC_MAX_HEALTH));
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
        return this.dataTracker.get(DATA_SHEARED);
    }

    public final void setSheared(int cooldown) {
        this.shearCooldown = cooldown;
        this.dataTracker.set(DATA_SHEARED, cooldown > 0);
    }

    public final boolean isShearable(ItemStack stack, World level, BlockPos pos) {
        Item item = stack.getItem();
        return this.isAlive() && this.stage.ordinal() >= 2 && !this.isSheared() && item instanceof TieredShearsItem && ((TieredShearsItem) item).getTier().getMiningLevel() >= 3;
    }

    public List<ItemStack> onSheared(PlayerEntity player, ItemStack stack, World level, BlockPos pos, int fortune) {
        DragonScalesItem scale = this.getDragonType().getInstance(DragonScalesItem.class, null);
        if (scale != null) {
            this.setSheared(2500 + this.random.nextInt(1000));
            this.playSound(DMSounds.DRAGON_GROWL, 1.0F, 1.0F);
            return Collections.singletonList(new ItemStack(scale, 2 + this.random.nextInt(3)));
        }
        return Collections.emptyList();
    }

    @Override
    public EntityData initialize(ServerWorldAccess level, LocalDifficulty difficulty, SpawnReason reason, @Nullable EntityData data, @Nullable NbtCompound tag) {
        //noinspection DataFlowIssue
        this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).addPersistentModifier(new EntityAttributeModifier(
                "Random spawn bonus",
                this.random.nextGaussian() * 0.05D,
                EntityAttributeModifier.Operation.MULTIPLY_BASE
        ));
        this.setLeftHanded(this.random.nextFloat() < 0.05F);
        this.setLifeStage(tag != null && tag.contains(DragonLifeStage.DATA_PARAMETER_KEY)
                        ? DragonLifeStage.byName(tag.getString(DragonLifeStage.DATA_PARAMETER_KEY))
                        : DragonLifeStage.ADULT,
                true, false
        );
        return data;
    }

    public final PacketByteBuf writeId(PacketByteBuf buffer) {
        return buffer.writeVarInt(this.getEntityId());
    }
}