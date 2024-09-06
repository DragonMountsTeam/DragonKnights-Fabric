package net.dragonmounts.entity.dragon;

import net.dragonmounts.api.IDragonFood;
import net.dragonmounts.block.DragonCoreBlock;
import net.dragonmounts.config.ServerConfig;
import net.dragonmounts.init.*;
import net.dragonmounts.inventory.DragonInventory;
import net.dragonmounts.inventory.DragonInventoryHandler;
import net.dragonmounts.item.DragonEssenceItem;
import net.dragonmounts.network.FeedDragonPayload;
import net.dragonmounts.network.SyncDragonAgePayload;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.registry.DragonVariant;
import net.dragonmounts.tag.DMItemTags;
import net.dragonmounts.util.DragonFood;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static net.dragonmounts.util.EntityUtil.addOrMergeEffect;
import static net.dragonmounts.util.EntityUtil.addOrResetEffect;
import static net.minecraft.resources.ResourceLocation.tryParse;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class ServerDragonEntity extends TameableDragonEntity {
    //public final PlayerControlledGoal playerControlledGoal;

    public ServerDragonEntity(EntityType<? extends TameableDragonEntity> type, Level level) {
        super(type, level);
        //this.goalSelector.addGoal(0, this.playerControlledGoal = new PlayerControlledGoal(this));
    }

    public ServerDragonEntity(Level level) {
        this(DMEntities.TAMEABLE_DRAGON, level);
    }

    public ServerDragonEntity(HatchableDragonEggEntity egg, DragonLifeStage stage) {
        this(DMEntities.TAMEABLE_DRAGON, egg.level());
        CompoundTag data = egg.saveWithoutId(new CompoundTag());
        data.remove(AGE_DATA_PARAMETER_KEY);
        data.remove(DragonLifeStage.DATA_PARAMETER_KEY);
        this.load(data);
        this.setDragonType(egg.getDragonType(), false);
        this.setLifeStage(stage, true, true);
        this.setHealth(this.getMaxHealth() + egg.getHealth() - egg.getMaxHealth());
    }

    @Override
    protected void registerGoals() {/*
        var goals = this.goalSelector;
        var targets = this.targetSelector;
        goals.addGoal(1, new SitGoal(this));
        goals.add(2, new MeleeAttackGoal(this, 1.0D, true));
        goals.add(3, new DragonFollowOwnerGoal(this));
        goals.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        goals.add(4, new LookAroundGoal(this));
        targets.add(1, new TrackOwnerAttackerGoal(this));
        targets.add(2, new AttackWithOwnerGoal(this));
        targets.add(3, (new RevengeGoal(this)).setGroupRevenge());
        targets.add(4, new FollowTargetGoal<>(this, MobEntity.class, 5, false, false, entity -> entity instanceof Monster));*/
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(DragonVariant.DATA_PARAMETER_KEY, this.getVariant().identifier.toString());
        tag.putString(DragonLifeStage.DATA_PARAMETER_KEY, this.stage.getSerializedName());
        tag.putBoolean(AGE_LOCKED_DATA_PARAMETER_KEY, this.isAgeLocked());
        tag.putInt(SHEARED_DATA_PARAMETER_KEY, this.isSheared() ? this.shearCooldown : 0);
        var items = this.inventory.saveItems(this.registryAccess());
        if (!items.isEmpty()) {
            tag.put(DragonInventory.DATA_PARAMETER_KEY, items);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        int age = this.age;
        DragonLifeStage stage = this.stage;
        if (tag.contains(DragonLifeStage.DATA_PARAMETER_KEY)) {
            this.setLifeStage(DragonLifeStage.byName(tag.getString(DragonLifeStage.DATA_PARAMETER_KEY)), false, false);
        }
        super.readAdditionalSaveData(tag);
        if (!this.firstTick && (this.age != age || stage != this.stage)) {
            SyncDragonAgePayload payload = new SyncDragonAgePayload(this.getId(), this.age, this.stage);
            for (var player : PlayerLookup.tracking(this)) {
                ServerPlayNetworking.send(player, payload);
            }
        }
        if (tag.contains(DragonVariant.DATA_PARAMETER_KEY)) {
            this.setVariant(DragonVariant.REGISTRY.get(tryParse(tag.getString(DragonVariant.DATA_PARAMETER_KEY))));
        } else if (tag.contains(DragonType.DATA_PARAMETER_KEY)) {
            this.setVariant(DragonType.REGISTRY.get(tryParse(tag.getString(DragonType.DATA_PARAMETER_KEY))).variants.draw(this.random, DragonVariants.ENDER_FEMALE, true));
        }
        if (tag.contains(SADDLE_DATA_PARAMETER_KEY)) {
            this.inventory.saddle.setLocal(ItemStack.parseOptional(this.registryAccess(), tag.getCompound(SADDLE_DATA_PARAMETER_KEY)), true);
        }
        if (tag.contains(SHEARED_DATA_PARAMETER_KEY)) {
            this.setSheared(tag.getInt(SHEARED_DATA_PARAMETER_KEY));
        }
        if (tag.contains(AGE_LOCKED_DATA_PARAMETER_KEY)) {
            this.setAgeLocked(tag.getBoolean(AGE_LOCKED_DATA_PARAMETER_KEY));
        }
        if (tag.contains(DragonInventory.DATA_PARAMETER_KEY)) {
            this.inventory.loadItems(tag.getList(DragonInventory.DATA_PARAMETER_KEY, 10), this.registryAccess());
        }
    }

    /**
     * Causes this entity to lift off if it can fly.
     */
    public void liftOff() {

    }

    public void spawnEssence(ItemStack stack) {
        var pos = this.blockPosition();
        var level = this.level();
        var state = DMBlocks.DRAGON_CORE.defaultBlockState().setValue(HORIZONTAL_FACING, this.getDirection());
        if (!DragonCoreBlock.tryPlaceAt(level, pos, state, stack)) {
            int y = pos.getY(), max = Math.min(y + 5, level.getMaxBuildHeight());
            var mutable = pos.mutable();
            while (++y < max) {
                if (DragonCoreBlock.tryPlaceAt(level, mutable.setY(y), state, stack)) {
                    return;
                }
            }
        } else return;
        level.addFreshEntity(new ItemEntity(level, this.getX(), this.getY(), this.getZ(), stack));
    }

    @Override
    protected void checkCrystals() {
        if (this.nearestCrystal != null && this.nearestCrystal.isAlive()) {
            if (++this.crystalTicks > 0 && this.getHealth() < this.getMaxHealth()) {
                this.crystalTicks = -10;
                this.setHealth(this.getHealth() + 1.0F);
                addOrResetEffect(this, MobEffects.DAMAGE_BOOST, 300, 0, false, true, true, 101);//15s
            }
            if (this.random.nextInt(20) == 0) {
                this.nearestCrystal = this.findCrystal();
            }
        } else {
            this.nearestCrystal = this.random.nextInt(10) == 0 ? this.findCrystal() : null;
        }
    }

    @Override
    public void aiStep() {
        if (this.isDeadOrDying()) {
            this.nearestCrystal = null;
        } else {
            this.checkCrystals();
        }
        if (this.shearCooldown > 0) {
            this.setSheared(this.shearCooldown - 1);
        }
        if (this.isAgeLocked()) {
            int age = this.age;
            this.age = 0;
            super.aiStep();
            this.age = age;
        } else {
            super.aiStep();
        }
        /*if (this.isOnGround()) {
            this.flightTicks = 0;
            this.setFlying(false);
        } else {
            this.setFlying(++this.flightTicks > LIFTOFF_THRESHOLD && !this.isBaby() && this.getControllingPassenger() != null);
                if (this.isFlying() != flag) {
                    getEntityAttribute(FOLLOW_RANGE).setBaseValue(getDragonSpeed());
                }
        }*/
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var item = stack.getItem();
        var food = DragonFood.get(item);
        if (food != IDragonFood.UNKNOWN) {
            if (!food.canFeed(this, player, stack, hand)) return InteractionResult.FAIL;
            food.feed(this, player, stack, hand);
            var payload = new FeedDragonPayload(this.getId(), this.age, this.stage, item);
            for (var target : PlayerLookup.tracking(this)) {
                ServerPlayNetworking.send(target, payload);
            }
            return InteractionResult.SUCCESS;
        }
        if (!this.isOwnedBy(player)) return InteractionResult.PASS;
        if (this.inventory.onInteract(stack)) return InteractionResult.SUCCESS;
        if (stack.is(DMItemTags.BATONS)) {
            this.setOrderedToSit(!this.isOrderedToSit());
            return InteractionResult.SUCCESS;
        }
        var result = item.interactLivingEntity(stack, player, this, hand);
        if (result.consumesAction()) return result;
        if (!ServerConfig.INSTANCE.debug.get() || player.isSecondaryUseActive()) {
            this.openCustomInventoryScreen(player);
        } else if (this.isBaby()) {
            this.setTarget(null);
            this.getNavigation().stop();
            this.setInSittingPose(false);
            var tag = new CompoundTag();
            if (this.save(tag) && player.setEntityOnShoulder(tag)) {
                this.discard();
            }
        } else if (this.isSaddled) {
            player.setYRot(this.getYRot());
            player.setXRot(this.getXRot());
            player.startRiding(this);
        } else {
            this.openCustomInventoryScreen(player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt bolt) {
        super.thunderHit(level, bolt);
        addOrMergeEffect(this, MobEffects.DAMAGE_BOOST, 700, 0, false, true, true);//35s
        DragonType current = this.getDragonType();
        if (current == DragonTypes.SKELETON) {
            this.setDragonType(DragonTypes.WITHER, false);
        } else if (current == DragonTypes.WATER) {
            this.setDragonType(DragonTypes.STORM, false);
        } else return;
        this.playSound(SoundEvents.END_PORTAL_SPAWN, 2, 1);
        this.playSound(SoundEvents.PORTAL_TRIGGER, 2, 1);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.isTame()) {
            this.spawnEssence(this.getDragonType().getInstance(DragonEssenceItem.class, DMItems.ENDER_DRAGON_ESSENCE)
                    .saveEntity(this, DataComponentPatch.EMPTY)
            );
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        ServerPlayNetworking.send(player, new SyncDragonAgePayload(this.getId(), this.age, this.stage));
    }

    @Override
    public void setLifeStage(DragonLifeStage stage, boolean reset, boolean sync) {
        var modifier = stage.modifier;
        var health = this.getAttribute(Attributes.MAX_HEALTH);
        var damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        var armor = this.getAttribute(Attributes.ARMOR);
        assert health != null && damage != null && armor != null;
        double temp = health.getValue();
        health.addOrUpdateTransientModifier(modifier);
        temp = health.getValue() - temp;
        this.setHealth(temp > 0 ? this.getHealth() + (float) temp : this.getHealth());
        damage.addOrUpdateTransientModifier(modifier);
        armor.addOrUpdateTransientModifier(modifier);
        if (this.stage == stage) return;
        this.stage = stage;
        if (reset) {
            this.refreshAge();
        }
        this.reapplyPosition();
        this.refreshDimensions();
        if (sync) {
            var payload = new SyncDragonAgePayload(this.getId(), this.age, stage);
            for (var player : PlayerLookup.tracking(this)) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    @Override
    public void travel(Vec3 vector) {
        // disable method while flying, the movement is done entirely by
        // moveEntity() and this one just makes the dragon to fall slowly when
        if (!this.isFlying()) {
            super.travel(vector);
        }
    }

    @Override
    public void setAge(int age) {
        if (this.age == age) return;
        if (this.age < 0 && age >= 0 || this.age > 0 && age <= 0) {
            this.ageBoundaryReached();
        } else {
            this.age = age;
        }
        var payload = new SyncDragonAgePayload(this.getId(), age, this.stage);
        for (var player : PlayerLookup.tracking(this)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    protected void tickDeath() {
        if (++this.deathTime >= this.getMaxDeathTime()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void checkDespawn() {
        this.noActionTime = 0;
    }

    @Override
    public boolean dismountsUnderwater() {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;// double insurance
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        player.openMenu(this);
    }

    @Override
    public TameableDragonEntity getScreenOpeningData(ServerPlayer player) {
        return this;
    }

    @Override
    public DragonInventoryHandler createMenu(int id, Inventory inventory, Player player) {
        return new DragonInventoryHandler(id, inventory, this);
    }
}
