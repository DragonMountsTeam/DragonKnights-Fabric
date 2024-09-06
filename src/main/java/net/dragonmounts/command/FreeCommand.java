package net.dragonmounts.command;

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
import java.util.UUID;
import java.util.function.Predicate;

import static net.dragonmounts.command.DMCommand.createClassCastException;
import static net.dragonmounts.command.DMCommand.getSingleProfileOrException;

public class FreeCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(Predicate<CommandSourceStack> permission) {
        return Commands.literal("free").requires(permission).then(Commands.argument("targets", EntityArgument.entities())
                .executes(context -> free(context, EntityArgument.getEntities(context, "targets")))
                .then(Commands.argument("owner", GameProfileArgument.gameProfile())
                        .executes(context -> free(context, EntityArgument.getEntities(context, "targets"), getSingleProfileOrException(context, "owner").getId()))
                )
                .then(Commands.argument("forced", BoolArgumentType.bool())
                        .executes(context -> free(context, BoolArgumentType.getBool(context, "forced")))
                )
        );
    }

    private static int free(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets) throws CommandSyntaxException {
        return free(context, targets, context.getSource().getPlayerOrException().getUUID(), targets.size() == 1);
    }

    private static int free(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets, UUID owner) {
        return free(context, targets, owner, false);
    }

    private static int free(CommandContext<CommandSourceStack> context, boolean forced) throws CommandSyntaxException {
        return free(context, EntityArgument.getEntities(context, "targets"), null, forced);
    }

    public static int free(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets, UUID owner, boolean forced) {
        var source = context.getSource();
        Entity cache = null;
        boolean flag = true;
        int count = 0;
        for (var target : targets) {
            if (target instanceof TamableAnimal entity) {
                if (forced || (owner != null && owner.equals(entity.getOwnerUUID()))) {
                    entity.setTame(false, false);
                    entity.setOwnerUUID(null);
                    ++count;
                }
                flag = false;
                cache = entity;
            }
        }
        if (flag) {
            if (targets.size() == 1) {
                source.sendFailure(createClassCastException(targets.iterator().next(), TamableAnimal.class));
            } else {
                source.sendFailure(Component.translatable("commands.dragonmounts.free.multiple", count));
            }
        } else if (count == 1) {
            final var temp = cache;
            source.sendSuccess(() -> Component.translatable("commands.dragonmounts.free.single", temp.getDisplayName()), true);
        } else {
            final var temp = count;
            source.sendSuccess(() -> Component.translatable("commands.dragonmounts.free.multiple", temp), true);
        }
        return count;
    }
}
