package net.dragonmounts.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

import static net.dragonmounts.command.DMCommand.createClassCastException;
import static net.dragonmounts.command.DMCommand.getSingleProfileOrException;

public class FreeCommand {
    public static ArgumentBuilder<ServerCommandSource, ?> register(Predicate<ServerCommandSource> permission) {
        return CommandManager.literal("free")
                .requires(permission)
                .then(CommandManager.argument("targets", EntityArgumentType.entities())
                        .executes(context -> free(context, EntityArgumentType.getEntities(context, "targets")))
                        .then(CommandManager.argument("owner", GameProfileArgumentType.gameProfile())
                                .executes(context -> free(context, EntityArgumentType.getEntities(context, "targets"), getSingleProfileOrException(context, "owner").getId()))
                        )
                        .then(CommandManager.argument("forced", BoolArgumentType.bool())
                                .executes(context -> free(context, BoolArgumentType.getBool(context, "forced")))
                        )
                );
    }

    private static int free(CommandContext<ServerCommandSource> context, Collection<? extends Entity> targets) throws CommandSyntaxException {
        return free(context, targets, context.getSource().getPlayer().getUuid(), targets.size() == 1);
    }

    private static int free(CommandContext<ServerCommandSource> context, Collection<? extends Entity> targets, UUID owner) {
        return free(context, targets, owner, false);
    }

    private static int free(CommandContext<ServerCommandSource> context, boolean forced) throws CommandSyntaxException {
        return free(context, EntityArgumentType.getEntities(context, "targets"), null, forced);
    }

    public static int free(CommandContext<ServerCommandSource> context, Collection<? extends Entity> targets, UUID owner, boolean forced) {
        ServerCommandSource source = context.getSource();
        Entity cache = null;
        boolean flag = true;
        int count = 0;
        for (Entity target : targets) {
            if (target instanceof TameableEntity) {
                TameableEntity entity = (TameableEntity) target;
                if (forced || (owner != null && owner.equals(entity.getOwnerUuid()))) {
                    entity.setTamed(false);
                    entity.setOwnerUuid(null);
                    ++count;
                }
                flag = false;
                cache = entity;
            }
        }
        if (flag) {
            source.sendError(targets.size() == 1
                    ? createClassCastException(targets.iterator().next(), TameableEntity.class)
                    : new TranslatableText("commands.dragonmounts.free.multiple", count)
            );
        } else if (count == 1) {
            source.sendFeedback(new TranslatableText("commands.dragonmounts.free.single", cache.getDisplayName()), true);
        } else {
            source.sendFeedback(new TranslatableText("commands.dragonmounts.free.multiple", count), true);
        }
        return count;
    }
}
