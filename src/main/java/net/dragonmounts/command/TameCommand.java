package net.dragonmounts.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;

import java.util.Collection;
import java.util.function.Predicate;

import static net.dragonmounts.command.DMCommand.createClassCastException;
import static net.dragonmounts.command.DMCommand.getSingleProfileOrException;

public class TameCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(Predicate<CommandSourceStack> permission) {
        return Commands.literal("tame").requires(permission).then(Commands.argument("targets", EntityArgument.entities())
                .executes(context -> tame(context, EntityArgument.getEntities(context, "targets")))
                .then(Commands.argument("owner", GameProfileArgument.gameProfile())
                        .executes(context -> tame(context, EntityArgument.getEntities(context, "targets"), getSingleProfileOrException(context, "owner")))
                        .then(Commands.argument("forced", BoolArgumentType.bool()).executes(
                                context -> tame(context, BoolArgumentType.getBool(context, "forced"))
                        ))
                )
        );
    }

    private static int tame(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets) throws CommandSyntaxException {
        return tame(context, targets, context.getSource().getPlayerOrException().getGameProfile(), targets.size() == 1);
    }

    private static int tame(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets, GameProfile owner) {
        return tame(context, targets, owner, targets.size() == 1);
    }

    private static int tame(CommandContext<CommandSourceStack> context, boolean forced) throws CommandSyntaxException {
        return tame(context, EntityArgument.getEntities(context, "targets"), getSingleProfileOrException(context, "owner"), forced);
    }

    public static int tame(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets, GameProfile owner, boolean forced) {
        var source = context.getSource();
        var level = source.getLevel();
        var uuid = owner.getId();
        var player = level.getPlayerByUUID(uuid);
        Entity cache = null;
        boolean flag = true;
        int count = 0;
        if (player == null) {
            for (var target : targets) {
                if (target instanceof TamableAnimal entity) {
                    if (forced || entity.getOwnerUUID() == null) {
                        entity.setTame(true, true);
                        entity.setOwnerUUID(uuid);
                        ++count;
                    }
                    flag = false;
                    cache = entity;
                }
            }
        } else {
            for (var target : targets) {
                if (target instanceof TamableAnimal entity) {
                    if (forced || entity.getOwnerUUID() == null) {
                        entity.tame(player);
                        ++count;
                    }
                    flag = false;
                    cache = entity;
                }
            }
        }
        if (flag) {
            if (targets.size() == 1) {
                source.sendFailure(createClassCastException(targets.iterator().next(), TamableAnimal.class));
            } else {
                source.sendFailure(Component.translatable("commands.dragonmounts.tame.multiple", count, owner.getName()));
            }
        } else if (count == 1) {
            final var temp = cache;
            source.sendSuccess(() -> Component.translatable("commands.dragonmounts.tame.single", temp.getDisplayName(), owner.getName()), true);
        } else {
            final var temp = count;
            source.sendSuccess(() -> Component.translatable("commands.dragonmounts.tame.multiple", temp, owner.getName()), true);
        }
        return count;
    }
}
