package net.dragonmounts.init;

import net.dragonmounts.api.IDragonScaleArmorEffect;
import net.dragonmounts.capability.ArmorEffectManager;
import net.dragonmounts.capability.IArmorEffectManager;
import net.dragonmounts.util.TextBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

import static net.dragonmounts.DragonMounts.MOD_ID;
import static net.dragonmounts.util.EntityUtil.addOrMergeEffect;
import static net.dragonmounts.util.EntityUtil.addOrResetEffect;

public class DMArmorEffects {
    public static final String FISHING_LUCK = "tooltip.dragonmounts.armor_effect_fishing_luck";

    public static final IDragonScaleArmorEffect.Advanced AETHER = new IDragonScaleArmorEffect.Advanced(new Identifier(MOD_ID, "aether"), 300) {
        @Override
        public boolean activate(IArmorEffectManager manager, PlayerEntity player, int level) {
            boolean flag = level > 3;
            if (flag && !player.world.isClient && manager.getCooldown(this) <= 0 && player.isSprinting() && addOrMergeEffect(player, StatusEffects.SPEED, 100, 1, true, true, true)) {
                player.world.playSoundFromEntity(null, player, SoundEvents.ENTITY_ELDER_GUARDIAN_HURT, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                manager.setCooldown(this, this.cooldown);
            }
            return flag;
        }
    };

    private static final TextBlock ENCHANT_TOOLTIP = new TextBlock(new TranslatableText("tooltip.armor_effect.dragonmounts.enchant"));
    public static final IDragonScaleArmorEffect ENCHANT = new IDragonScaleArmorEffect() {
        @Override
        public boolean activate(IArmorEffectManager manager, PlayerEntity player, int level) {
            if (player.world.isClient) {
                Random random = player.getRandom();
                for (int i = -2; i <= 2; ++i) {
                    for (int j = -2; j <= 2; ++j) {
                        if (i > -2 && i < 2 && j == -1) j = 2;
                        if (random.nextInt(30) == 0) {
                            for (int k = 0; k <= 1; ++k) {
                                player.world.addParticle(
                                        ParticleTypes.ENCHANT,
                                        player.getX(),
                                        player.getY() + random.nextFloat() + 1.5,
                                        player.getZ(),
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
        public void appendHoverText(ItemStack stack, @Nullable World level, List<Text> tooltips) {
            ENCHANT_TOOLTIP.appendHoverText(tooltips);
        }
    };

    public static final IDragonScaleArmorEffect.Advanced ENDER = new IDragonScaleArmorEffect.Advanced(new Identifier(MOD_ID, "ender"), 1200) {
        @Override
        public boolean activate(IArmorEffectManager manager, PlayerEntity player, int level) {
            if (player.world.isClient) {
                Random random = player.getRandom();
                player.world.addParticle(
                        ParticleTypes.PORTAL,
                        player.getX() + random.nextFloat() - 0.3,
                        player.getY() + random.nextFloat() - 0.3,
                        player.getZ() + random.nextFloat() - 0.3,
                        random.nextFloat() * 2 - 0.15,
                        random.nextFloat() * 2 - 0.15,
                        random.nextFloat() * 2 - 0.15
                );
                return level > 3;
            }
            // use `|` instead of `||` to avoid short-circuit evaluation when trying to add both of these two effects
            if (level > 3 && manager.getCooldown(this) <= 0 && player.getHealth() < 10 && (addOrMergeEffect(player, StatusEffects.RESISTANCE, 600, 2, true, true, true) | addOrMergeEffect(player, StatusEffects.STRENGTH, 300, 1, true, true, true))) {
                player.world.syncWorldEvent(2003, player.getBlockPos(), 0);
                player.world.playSoundFromEntity(null, player, SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.HOSTILE, 0.05F, 1.0F);
                manager.setCooldown(this, this.cooldown);
                return true;
            }
            return false;
        }
    };

    public static final IDragonScaleArmorEffect.Advanced FIRE = new IDragonScaleArmorEffect.Advanced(new Identifier(MOD_ID, "fire"), 900) {
        @Override
        public boolean activate(IArmorEffectManager manager, PlayerEntity player, int level) {
            boolean flag = level > 3;
            if (flag && !player.world.isClient && manager.getCooldown(this) <= 0 && player.isOnFire()) {
                if (addOrMergeEffect(player, StatusEffects.FIRE_RESISTANCE, 600, 0, true, true, true)) {
                    manager.setCooldown(this, this.cooldown);
                }
                player.extinguish();
            }
            return flag;
        }
    };

    public static final IDragonScaleArmorEffect.Advanced FOREST = new IDragonScaleArmorEffect.Advanced(new Identifier(MOD_ID, "forest"), 1200) {
        @Override
        public boolean activate(IArmorEffectManager manager, PlayerEntity player, int level) {
            boolean flag = level > 3;
            if (flag && !player.world.isClient) {
                if (player.fishHook != null) {
                    addOrResetEffect(player, StatusEffects.LUCK, 200, 0, true, true, true, 21);
                }
                if (player.getHealth() < 10 && manager.getCooldown(this) <= 0) {
                    if (addOrMergeEffect(player, StatusEffects.REGENERATION, 200, 1, true, true, true)) {
                        manager.setCooldown(this, this.cooldown);
                    }
                }
            }
            return flag;
        }

        @Override
        protected TextBlock init() {
            return new TextBlock(
                    new TranslatableText(this.description),
                    new TranslatableText(FISHING_LUCK)
            );
        }
    };

    public static final IDragonScaleArmorEffect.Advanced ICE = new IDragonScaleArmorEffect.Advanced(new Identifier(MOD_ID, "ice"), 1200);

    private static final TextBlock MOONLIGHT_TOOLTIP = new TextBlock(new TranslatableText("tooltip.armor_effect.dragonmounts.moonlight"));
    public static final IDragonScaleArmorEffect MOONLIGHT = new IDragonScaleArmorEffect() {
        @Override
        public boolean activate(IArmorEffectManager manager, PlayerEntity player, int level) {
            boolean flag = level > 3;
            if (flag && !player.world.isClient) {
                addOrResetEffect(player, StatusEffects.NIGHT_VISION, 600, 0, true, true, true, 201);
            }
            return flag;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable World level, List<Text> tooltips) {
            MOONLIGHT_TOOLTIP.appendHoverText(tooltips);
        }
    };

    public static final IDragonScaleArmorEffect.Advanced NETHER = new IDragonScaleArmorEffect.Advanced(new Identifier(MOD_ID, "nether"), 1200);

    public static final IDragonScaleArmorEffect.Advanced STORM = new IDragonScaleArmorEffect.Advanced(new Identifier(MOD_ID, "storm"), 160);

    public static final IDragonScaleArmorEffect.Advanced SUNLIGHT = new IDragonScaleArmorEffect.Advanced(new Identifier(MOD_ID, "sunlight"), 1200) {
        @Override
        public boolean activate(IArmorEffectManager manager, PlayerEntity player, int level) {
            boolean flag = level > 3;
            if (flag && !player.world.isClient) {
                if (player.fishHook != null) {
                    addOrResetEffect(player, StatusEffects.LUCK, 200, 0, true, true, true, 21);
                }
                if (manager.getCooldown(this) <= 0 && player.getHungerManager().getFoodLevel() < 6 && addOrMergeEffect(player, StatusEffects.SATURATION, 200, 0, true, true, true)) {
                    manager.setCooldown(this, this.cooldown);
                }
            }
            return flag;
        }

        @Override
        protected TextBlock init() {
            return new TextBlock(
                    new TranslatableText(this.description),
                    new TranslatableText(FISHING_LUCK)
            );
        }
    };
    private static final TextBlock TERRA_TOOLTIP = new TextBlock(new TranslatableText("tooltip.armor_effect.dragonmounts.terra"));
    public static final IDragonScaleArmorEffect TERRA = new IDragonScaleArmorEffect() {
        @Override
        public boolean activate(IArmorEffectManager manager, PlayerEntity player, int level) {
            boolean flag = level > 3;
            if (flag && !player.world.isClient) {
                addOrResetEffect(player, StatusEffects.HASTE, 600, 0, true, true, true, 201);
            }
            return flag;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable World level, List<Text> tooltips) {
            TERRA_TOOLTIP.appendHoverText(tooltips);
        }
    };
    private static final TextBlock WATER_TOOLTIP = new TextBlock(new TranslatableText("tooltip.armor_effect.dragonmounts.water"));
    public static final IDragonScaleArmorEffect WATER = new IDragonScaleArmorEffect() {
        @Override
        public boolean activate(IArmorEffectManager manager, PlayerEntity player, int level) {
            boolean flag = level > 3;
            if (flag && !player.world.isClient && player.isSubmergedIn(FluidTags.WATER)) {
                addOrResetEffect(player, StatusEffects.WATER_BREATHING, 600, 0, true, true, true, 201);
            }
            return flag;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable World level, List<Text> tooltips) {
            WATER_TOOLTIP.appendHoverText(tooltips);
        }
    };

    public static final IDragonScaleArmorEffect.Advanced ZOMBIE = new IDragonScaleArmorEffect.Advanced(new Identifier(MOD_ID, "zombie"), 400) {
        @Override
        public boolean activate(IArmorEffectManager manager, PlayerEntity player, int level) {
            boolean flag = level > 3;
            if (flag && !player.world.isClient && !player.world.isDay() && manager.getCooldown(this) <= 0 && addOrMergeEffect(player, StatusEffects.STRENGTH, 300, 0, true, true, true)) {
                manager.setCooldown(this, this.cooldown);
            }
            return flag;
        }
    };

    @SuppressWarnings("SameReturnValue")
    public static ActionResult meleeChanneling(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hit) {
        if (world.isClient || player.getRandom().nextBoolean()) return ActionResult.PASS;
        ArmorEffectManager manager = ((IArmorEffectManager.Provider) player).dragonmounts$getManager();
        if (manager.isActive(STORM) && manager.getCooldown(STORM) <= 0) {
            BlockPos pos = entity.getBlockPos();
            if (world.isSkyVisible(pos)) {
                LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
                if (bolt == null) return ActionResult.PASS;
                bolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos));
                bolt.setChanneler((ServerPlayerEntity) player);
                world.spawnEntity(bolt);
            }
            manager.setCooldown(STORM, STORM.cooldown);
        }
        return ActionResult.PASS;
    }
}
