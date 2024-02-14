package net.dragonmounts.util;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Map;


public class EntityUtil extends EntityType<Entity> {//to access protected methods

    private EntityUtil(EntityFactory<Entity> a, SpawnGroup b, boolean c, boolean d, boolean e, boolean f, ImmutableSet<Block> g, EntityDimensions h, int i, int j) {
        super(a, b, c, d, e, f, g, h, i, j);
    }

    public static void finalizeSpawn(ServerWorld world, Entity entity, BlockPos pos, SpawnReason reason, EntityData data, NbtCompound tag, boolean yOffset, boolean extraOffset) {
        double offset;
        if (yOffset) {
            entity.setPos(pos.getX() + 0.5D, (pos.getY() + 1), pos.getZ() + 0.5D);
            offset = getOriginY(world, pos, extraOffset, entity.getBoundingBox());
        } else {
            offset = 0.0D;
        }
        entity.refreshPositionAndAngles(pos.getX() + 0.5D, pos.getY() + offset, pos.getZ() + 0.5D, MathHelper.wrapDegrees(world.random.nextFloat() * 360.0F), 0.0F);
        if (entity instanceof MobEntity) {
            MobEntity mobentity = (MobEntity) entity;
            mobentity.headYaw = mobentity.yaw;
            mobentity.bodyYaw = mobentity.yaw;
            mobentity.initialize(world, world.getLocalDifficulty(mobentity.getBlockPos()), reason, data, tag);
            mobentity.playAmbientSound();
        }
    }

    public static boolean addOrMergeEffect(LivingEntity entity, StatusEffect effect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon) {
        StatusEffectInstance instance = entity.getStatusEffect(effect);
        return entity.addStatusEffect(new StatusEffectInstance(effect, instance != null && instance.getAmplifier() == amplifier ? duration + instance.getDuration() : duration, amplifier, ambient, visible, showIcon, null));
    }

    public static boolean addOrResetEffect(LivingEntity entity, StatusEffect effect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon, int threshold) {
        StatusEffectInstance instance = entity.getStatusEffect(effect);
        if (instance != null && instance.getAmplifier() == amplifier && instance.getDuration() > threshold)
            return false;
        return entity.addStatusEffect(new StatusEffectInstance(effect, duration, amplifier, ambient, visible, showIcon, null));
    }

    public static ItemStack consume(PlayerEntity player, Hand hand, ItemStack stack, ItemStack result) {
        stack.decrement(1);
        if (result != null) {
            if (stack.isEmpty()) {
                player.setStackInHand(hand, result);
                return result;
            } else if (!player.inventory.insertStack(result)) {//PlayerInventory.getFreeSlot() won't check the offhand slot
                player.dropItem(result, false);
            }
        }
        return stack;
    }

    public static NbtCompound saveScoreboard(Entity entity, NbtCompound compound) {
        Scoreboard scoreboard = entity.world.getScoreboard();
        String scoreboardName = entity.getEntityName();
        Team team = scoreboard.getPlayerTeam(scoreboardName);
        Map<ScoreboardObjective, ScoreboardPlayerScore> scores = scoreboard.getPlayerObjectives(scoreboardName);
        if (!scores.isEmpty()) {
            NbtCompound scoresTag = new NbtCompound();
            NbtCompound lockedScoresTag = new NbtCompound();
            ScoreboardPlayerScore cache;
            for (Map.Entry<ScoreboardObjective, ScoreboardPlayerScore> entry : scores.entrySet()) {
                cache = entry.getValue();
                if (cache.isLocked()) {
                    lockedScoresTag.putInt(entry.getKey().getName(), cache.getScore());
                } else {
                    scoresTag.putInt(entry.getKey().getName(), cache.getScore());
                }
            }
            if (!scoresTag.isEmpty()) {
                compound.put("Scores", scoresTag);
            }
            if (!lockedScoresTag.isEmpty()) {
                compound.put("LockedScores", lockedScoresTag);
            }
            if (team != null) {
                compound.putString("Team", team.getName());
            }
        }
        return compound;
    }

    public static <T extends Entity> T loadScores(T entity, NbtCompound compound) {
        World world = entity.world;
        Scoreboard scoreboard = world.getScoreboard();
        String scoreboardName = entity.getEntityName();
        Map<ScoreboardObjective, ScoreboardPlayerScore> existingScores = world.getScoreboard().getPlayerObjectives(scoreboardName);
        NbtCompound scores;
        ScoreboardObjective objective;
        ScoreboardPlayerScore score;
        if (compound.contains("Scores")) {
            scores = compound.getCompound("Scores");
            for (String name : scores.getKeys()) {
                objective = scoreboard.getObjective(name);
                if (!existingScores.containsKey(objective)) {
                    score = scoreboard.getPlayerScore(scoreboardName, objective);
                    score.setScore(scores.getInt(name));
                    score.setLocked(false);
                }
            }
        }
        if (compound.contains("LockedScores")) {
            scores = compound.getCompound("LockedScores");
            for (String name : scores.getKeys()) {
                objective = scoreboard.getObjective(name);
                if (!existingScores.containsKey(objective)) {
                    score = scoreboard.getPlayerScore(scoreboardName, objective);
                    score.setScore(scores.getInt(name));
                }
            }
        }
        return entity;
    }

    public static <T extends Entity> T loadScoreboard(T entity, NbtCompound compound) {
        World world = entity.world;
        Scoreboard scoreboard = world.getScoreboard();
        String scoreboardName = entity.getEntityName();
        Map<ScoreboardObjective, ScoreboardPlayerScore> existingScores = world.getScoreboard().getPlayerObjectives(scoreboardName);
        NbtCompound scores;
        ScoreboardObjective objective;
        ScoreboardPlayerScore score;
        // net.minecraft.entity.LivingEntity.readAdditionalSaveData
        if (compound.contains("Team", 8)) {
            scoreboard.addPlayerToTeam(scoreboardName, world.getScoreboard().getPlayerTeam(compound.getString("Team")));
        }
        if (compound.contains("Scores")) {
            scores = compound.getCompound("Scores");
            for (String name : scores.getKeys()) {
                objective = scoreboard.getObjective(name);
                if (!existingScores.containsKey(objective)) {
                    score = scoreboard.getPlayerScore(scoreboardName, objective);
                    score.setScore(scores.getInt(name));
                    score.setLocked(false);
                }
            }
        }
        if (compound.contains("LockedScores")) {
            scores = compound.getCompound("LockedScores");
            for (String name : scores.getKeys()) {
                objective = scoreboard.getObjective(name);
                if (!existingScores.containsKey(objective)) {
                    score = scoreboard.getPlayerScore(scoreboardName, objective);
                    score.setScore(scores.getInt(name));
                }
            }
        }
        return entity;
    }
}
