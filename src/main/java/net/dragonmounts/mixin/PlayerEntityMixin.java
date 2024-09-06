package net.dragonmounts.mixin;

import net.dragonmounts.capability.ArmorEffectManager;
import net.dragonmounts.capability.IArmorEffectManager.Provider;
import net.dragonmounts.init.DMArmorEffects;
import net.dragonmounts.item.DragonScaleShieldItem;
import net.dragonmounts.network.ArmorRipostePayload;
import net.dragonmounts.util.EntityUtil;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.dragonmounts.capability.ArmorEffectManager.DATA_PARAMETER_KEY;
import static net.dragonmounts.util.EntityUtil.addOrMergeEffect;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements Provider {
    @Shadow
    public abstract ItemCooldowns getCooldowns();

    @Shadow
    public abstract void awardStat(Stat<?> stat);

    @Shadow
    public abstract void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack);

    @Unique
    protected ArmorEffectManager manager = new ArmorEffectManager(Player.class.cast(this));

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickManager(CallbackInfo info) {
        this.manager.tick();
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void saveCooldown(CompoundTag tag, CallbackInfo info) {
        var data = this.manager.saveNBT();
        if (data.isEmpty()) return;
        var caps = tag.getCompound("ForgeCaps");
        caps.put(DATA_PARAMETER_KEY, data);
        tag.put("ForgeCaps", caps);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readCooldown(CompoundTag tag, CallbackInfo info) {
        this.manager.readNBT(tag.getCompound("ForgeCaps").getCompound(DATA_PARAMETER_KEY));
    }

    @Inject(method = "hurtCurrentlyUsedShield", at = @At("HEAD"))
    public void hurtDragonScaleShield(float amount, CallbackInfo info) {
        if (this.useItem.getItem() instanceof DragonScaleShieldItem shield) {
            if (!this.level().isClientSide) {
                this.awardStat(Stats.ITEM_USED.get(shield));
            }
            if (amount >= 3.0F) {
                var slot = EntityUtil.getSlotForHand(this.getUsedItemHand());
                this.useItem.hurtAndBreak(1 + Mth.floor(amount), this, slot);
                if (this.useItem.isEmpty()) {
                    this.setItemSlot(slot, ItemStack.EMPTY);
                    this.useItem = ItemStack.EMPTY;
                    this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.level().random.nextFloat() * 0.4F);
                }
            }
        }
    }

    @Inject(method = "disableShield", at = @At("HEAD"), cancellable = true)
    public void disableDragonScaleShield(CallbackInfo info) {
        if (this.useItem.getItem() instanceof DragonScaleShieldItem shield) {
            this.getCooldowns().addCooldown(shield, 100);
            this.stopUsingItem();
            this.level().broadcastEntityEvent(this, (byte) 30);
            info.cancel();
        }
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setHealth(F)V"))
    public void riposte(DamageSource a, float b, CallbackInfo info) {
        var level = this.level();
        if (level.isClientSide) return;//In fact, `level.isClientSide` -> false
        var ice = DMArmorEffects.ICE;
        var nether = DMArmorEffects.NETHER;
        var manager = this.manager;
        var iceFlag = manager.isActive(ice) && manager.getCooldown(ice) <= 0;
        var netherFlag = manager.isActive(nether) && manager.getCooldown(nether) <= 0;
        int flag = (iceFlag ? 0b01 : 0b00) | (netherFlag ? 0b10 : 0b00);
        if (flag == 0) return;
        var entities = level.getEntities(this, this.getBoundingBox().inflate(5.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
        if (entities.isEmpty()) return;
        var freeze = level.damageSources().freeze();
        for (var entity : entities) {
            if (entity instanceof LivingEntity target) {
                target.knockback(0.4F, 1, 1);
                if (iceFlag) {
                    addOrMergeEffect(target, MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, true, true);
                    entity.invulnerableTime = 0;
                    entity.hurt(freeze, 1F);
                }
            } else if (iceFlag) {
                entity.invulnerableTime = 0;
                entity.hurt(freeze, 1F);
            }
            if (netherFlag) {
                int current = entity.getRemainingFireTicks();
                entity.setRemainingFireTicks(current > 0 ? current + 200 : 200);
            }
        }
        if (iceFlag) {
            manager.setCooldown(ice, ice.cooldown);
        }
        if (netherFlag) {
            manager.setCooldown(nether, nether.cooldown);
        }
        var payload = new ArmorRipostePayload(this.getId(), flag);
        for (var player : PlayerLookup.tracking(this)) {
            ServerPlayNetworking.send(player, payload);
        }
        ServerPlayNetworking.send(ServerPlayer.class.cast(this), payload);
    }

    @Override
    public ArmorEffectManager dragonmounts$getManager() {
        return this.manager;
    }

    private PlayerEntityMixin(EntityType<? extends LivingEntity> a, Level b) {super(a, b);}
}
