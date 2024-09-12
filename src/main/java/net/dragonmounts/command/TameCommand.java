package net.dragonmounts.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

import static net.dragonmounts.command.DMCommand.createClassCastException;
import static net.dragonmounts.command.DMCommand.getSingleProfileOrException;

public class TameCommand {
    public static ArgumentBuilder<ServerCommandSource, ?> register(Predicate<ServerCommandSource> permission) {
        return CommandManager.literal("tame").requires(permission).then(CommandManager.argument("targets", EntityArgumentType.entities())
                .executes(context -> tame(context, EntityArgumentType.getEntities(context, "targets")))
                .then(CommandManager.argument("owner", GameProfileArgumentType.gameProfile())
                        .executes(context -> tame(context, EntityArgumentType.getEntities(context, "targets"), getSingleProfileOrException(context, "owner")))
                        .then(CommandManager.argument("forced", BoolArgumentType.bool()).executes(
                                context -> tame(context, BoolArgumentType.getBool(context, "forced"))
                        ))
                )
        );
    }

    private static int tame(CommandContext<ServerCommandSource> context, Collection<? extends Entity> targets) throws CommandSyntaxException {
        return tame(context, targets, context.getSource().getPlayer().getGameProfile(), targets.size() == 1);
    }

    private static int tame(CommandContext<ServerCommandSource> context, Collection<? extends Entity> targets, GameProfile owner) {
        return tame(context, targets, owner, targets.size() == 1);
    }

    private static int tame(CommandContext<ServerCommandSource> context, boolean forced) throws CommandSyntaxException {
        return tame(context, EntityArgumentType.getEntities(context, "targets"), getSingleProfileOrException(context, "owner"), forced);
    }

    public static int tame(CommandContext<ServerCommandSource> context, Collection<? extends Entity> targets, GameProfile owner, boolean forced) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        UUID uuid = owner.getId();
        PlayerEntity player = world.getPlayerByUuid(uuid);
        Entity cache = null;
        boolean flag = true;
        int count = 0;
        if (player == null) {
            for (Entity target : targets) {
                if (target instanceof TameableEntity) {
                    TameableEntity entity = ((TameableEntity) target);
                    if (forced || entity.getOwnerUuid() == null) {
                        entity.setTamed(true);
                        entity.setOwnerUuid(uuid);
                        ++count;
                    }
                    flag = false;
                    cache = entity;
                }
            }
        } else {
            for (Entity target : targets) {
                if (target instanceof TameableEntity) {
                    TameableEntity entity = ((TameableEntity) target);
                    if (forced || entity.getOwnerUuid() == null) {
                        entity.setOwner(player);
                        ++count;
                    }
                    flag = false;
                    cache = entity;
                }
            }
        }
        if (flag) {
            source.sendError(targets.size() == 1
                    ? createClassCastException(targets.iterator().next(), TameableEntity.class)
                    : new TranslatableText("commands.dragonmounts.tame.multiple", count, owner.getName())
            );
        } else if (count == 1) {
            source.sendFeedback(new TranslatableText("commands.dragonmounts.tame.single", cache.getDisplayName(), owner.getName()), true);
        } else {
            source.sendFeedback(new TranslatableText("commands.dragonmounts.tame.multiple", count, owner.getName()), true);
        }
        return count;
    }
}
