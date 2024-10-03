package net.dragonmounts.init;

import net.dragonmounts.api.IDragonScaleArmorEffect;
import net.dragonmounts.capability.ArmorEffectManager;
import net.dragonmounts.capability.IArmorEffectManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.dragonmounts.DragonMounts.makeId;
import static net.dragonmounts.util.EntityUtil.addOrMergeEffect;
import static net.dragonmounts.util.EntityUtil.addOrResetEffect;

public class DMArmorEffects {
    public static final TranslatableContents FISHING_LUCK = new TranslatableContents("tooltip.dragonmounts.armor_effect_fishing_luck", null, TranslatableContents.NO_ARGS);

    public static final IDragonScaleArmorEffect.Advanced AETHER = new IDragonScaleArmorEffect.Advanced(makeId("aether"), 300) {
        @Override
        public boolean activate(IArmorEffectManager manager, Player player, int level) {
            boolean flag = level > 3;
            Level world = player.level();
            if (flag && !world.isClientSide && manager.getCooldown(this) <= 0 && player.isSprinting() && addOrMergeEffect(player, MobEffects.MOVEMENT_SPEED, 100, 1, true, true, true)) {
                world.playSound(null, player, SoundEvents.GUARDIAN_HURT, SoundSource.NEUTRAL, 1.0F, 1.0F);
                manager.setCooldown(this, this.cooldown);
            }
            return flag;
        }
    };

    public static final IDragonScaleArmorEffect ENCHANT = new IDragonScaleArmorEffect() {
        private static final TranslatableContents TOOLTIP = new TranslatableContents("tooltip.armor_effect.dragonmounts.enchant", null, TranslatableContents.NO_ARGS);

        @Override
        public boolean activate(IArmorEffectManager manager, Player player, int level) {
            Level world = player.level();
            if (world.isClientSide) {
                RandomSource random = player.getRandom();
                Vec3 pos = player.position();
                double x = pos.x, y = pos.y + 1.5, z = pos.z;
                for (int i = -2; i <= 2; ++i) {
                    for (int j = -2; j <= 2; ++j) {
                        if (i > -2 && i < 2 && j == -1) j = 2;
                        if (random.nextInt(30) == 0) {
                            for (int k = 0; k <= 1; ++k) {
                                world.addParticle(
                                        ParticleTypes.ENCHANT,
                                        x,
                                        y + random.nextFloat(),
                                        z,
                                        i + random.nextFloat() - 0.5D,
                                        k - random.nextFloat() - 1.0F,
                                        j + random.nextFloat() - 0.5D
                                );
                            }
                        }
                    }
                }
            }
            return level > 3;
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltips, TooltipFlag flag) {
            tooltips.add(MutableComponent.create(TOOLTIP));
        }
    };

    public static final IDragonScaleArmorEffect.Advanced ENDER = new IDragonScaleArmorEffect.Advanced(makeId("ender"), 1200) {
        @Override
        public boolean activate(IArmorEffectManager manager, Player player, int level) {
            Level world = player.level();
            if (world.isClientSide) {
                RandomSource random = player.getRandom();
                Vec3 pos = player.position();
                world.addParticle(
                        ParticleTypes.PORTAL,
                        pos.x + random.nextFloat() - 0.3,
                        pos.y + random.nextFloat() - 0.3,
                        pos.z + random.nextFloat() - 0.3,
                        random.nextFloat() * 2 - 0.15,
                        random.nextFloat() * 2 - 0.15,
                        random.nextFloat() * 2 - 0.15
                );
                return level > 3;
            }
            // use `|` instead of `||` to avoid short-circuit evaluation when trying to add both of these two effects
            if (level > 3 && manager.getCooldown(this) <= 0 && player.getHealth() < 10 && (addOrMergeEffect(player, MobEffects.DAMAGE_RESISTANCE, 600, 2, true, true, true) | addOrMergeEffect(player, MobEffects.DAMAGE_BOOST, 300, 1, true, true, true))) {
                world.levelEvent(2003, player.blockPosition(), 0);
                world.playSound(null, player, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 0.05F, 1.0F);
                manager.setCooldown(this, this.cooldown);
                return true;
            }
            return false;
        }
    };

    public static final IDragonScaleArmorEffect.Advanced FIRE = new IDragonScaleArmorEffect.Advanced(makeId("fire"), 900) {
        @Override
        public boolean activate(IArmorEffectManager manager, Player player, int level) {
            boolean flag = level > 3;
            if (flag && !player.level().isClientSide && manager.getCooldown(this) <= 0 && player.isOnFire()) {
                if (addOrMergeEffect(player, MobEffects.FIRE_RESISTANCE, 600, 0, true, true, true)) {
                    manager.setCooldown(this, this.cooldown);
                }
                player.clearFire();
            }
            return flag;
        }
    };

    public static final IDragonScaleArmorEffect.Advanced FOREST = new IDragonScaleArmorEffect.Advanced(makeId("forest"), 1200) {
        @Override
        public boolean activate(IArmorEffectManager manager, Player player, int level) {
            boolean flag = level > 3;
            if (flag && !player.level().isClientSide) {
                if (player.fishing != null) {
                    addOrResetEffect(player, MobEffects.LUCK, 200, 0, true, true, true, 21);
                }
                if (player.getHealth() < 10 && manager.getCooldown(this) <= 0) {
                    if (addOrMergeEffect(player, MobEffects.REGENERATION, 200, 1, true, true, true)) {
                        manager.setCooldown(this, this.cooldown);
                    }
                }
            }
            return flag;
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltips, TooltipFlag flag) {
            tooltips.add(Component.empty());
            this.appendTriggerInfo(stack, context, tooltips);
            tooltips.add(MutableComponent.create(this.description));
            tooltips.add(MutableComponent.create(FISHING_LUCK));
            this.appendCooldownInfo(tooltips);
        }
    };

    public static final IDragonScaleArmorEffect.Advanced ICE = new IDragonScaleArmorEffect.Advanced(makeId("ice"), 1200);

    public static final IDragonScaleArmorEffect MOONLIGHT = new IDragonScaleArmorEffect() {
        private static final TranslatableContents TOOLTIP = new TranslatableContents("tooltip.armor_effect.dragonmounts.moonlight", null, TranslatableContents.NO_ARGS);

        @Override
        public boolean activate(IArmorEffectManager manager, Player player, int level) {
            boolean flag = level > 3;
            if (flag && !player.level().isClientSide) {
                addOrResetEffect(player, MobEffects.NIGHT_VISION, 600, 0, true, true, true, 201);
            }
            return flag;
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltips, TooltipFlag flag) {
            tooltips.add(MutableComponent.create(TOOLTIP));
        }
    };

    public static final IDragonScaleArmorEffect.Advanced NETHER = new IDragonScaleArmorEffect.Advanced(makeId("nether"), 1200);

    public static final IDragonScaleArmorEffect.Advanced STORM = new IDragonScaleArmorEffect.Advanced(makeId("storm"), 160);

    public static final IDragonScaleArmorEffect.Advanced SUNLIGHT = new IDragonScaleArmorEffect.Advanced(makeId("sunlight"), 1200) {
        @Override
        public boolean activate(IArmorEffectManager manager, Player player, int level) {
            boolean flag = level > 3;
            if (flag && !player.level().isClientSide) {
                if (player.fishing != null) {
                    addOrResetEffect(player, MobEffects.LUCK, 200, 0, true, true, true, 21);
                }
                if (manager.getCooldown(this) <= 0 && player.getFoodData().getFoodLevel() < 6 && addOrMergeEffect(player, MobEffects.SATURATION, 200, 0, true, true, true)) {
                    manager.setCooldown(this, this.cooldown);
                }
            }
            return flag;
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.@Nullable TooltipContext context, List<Component> tooltips, TooltipFlag flag) {
            tooltips.add(Component.empty());
            this.appendTriggerInfo(stack, context, tooltips);
            tooltips.add(MutableComponent.create(this.description));
            tooltips.add(MutableComponent.create(FISHING_LUCK));
            this.appendCooldownInfo(tooltips);
        }
    };
    public static final IDragonScaleArmorEffect TERRA = new IDragonScaleArmorEffect() {
        private static final TranslatableContents TOOLTIP = new TranslatableContents("tooltip.armor_effect.dragonmounts.terra", null, TranslatableContents.NO_ARGS);

        @Override
        public boolean activate(IArmorEffectManager manager, Player player, int level) {
            boolean flag = level > 3;
            if (flag && !player.level().isClientSide) {
                addOrResetEffect(player, MobEffects.DIG_SPEED, 600, 0, true, true, true, 201);
            }
            return flag;
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltips, TooltipFlag flag) {
            tooltips.add(MutableComponent.create(TOOLTIP));
        }
    };
    public static final IDragonScaleArmorEffect WATER = new IDragonScaleArmorEffect() {
        private static final TranslatableContents TOOLTIP = new TranslatableContents("tooltip.armor_effect.dragonmounts.water", null, TranslatableContents.NO_ARGS);

        @Override
        public boolean activate(IArmorEffectManager manager, Player player, int level) {
            boolean flag = level > 3;
            if (flag && !player.level().isClientSide && player.isEyeInFluid(FluidTags.WATER)) {
                addOrResetEffect(player, MobEffects.WATER_BREATHING, 600, 0, true, true, true, 201);
            }
            return flag;
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltips, TooltipFlag flag) {
            tooltips.add(MutableComponent.create(TOOLTIP));
        }
    };

    public static final IDragonScaleArmorEffect.Advanced ZOMBIE = new IDragonScaleArmorEffect.Advanced(makeId("zombie"), 400) {
        @Override
        public boolean activate(IArmorEffectManager manager, Player player, int level) {
            boolean flag = level > 3;
            if (flag && !player.level().isClientSide && !player.level().isDay() && manager.getCooldown(this) <= 0 && addOrMergeEffect(player, MobEffects.DAMAGE_BOOST, 300, 0, true, true, true)) {
                manager.setCooldown(this, this.cooldown);
            }
            return flag;
        }
    };

    @SuppressWarnings("SameReturnValue")
    public static InteractionResult meleeChanneling(Player player, Level level, InteractionHand hand, Entity entity, @Nullable EntityHitResult hit) {
        if (level.isClientSide || player.getRandom().nextBoolean()) return InteractionResult.PASS;
        ArmorEffectManager manager = ((IArmorEffectManager.Provider) player).dragonmounts$getManager();
        if (manager.isActive(STORM) && manager.getCooldown(STORM) <= 0) {
            BlockPos pos = entity.blockPosition();
            if (level.canSeeSky(pos)) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt == null) return InteractionResult.PASS;
                bolt.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                bolt.setCause((ServerPlayer) player);
                level.addFreshEntity(bolt);
            }
            manager.setCooldown(STORM, STORM.cooldown);
        }
        return InteractionResult.PASS;
    }
}
