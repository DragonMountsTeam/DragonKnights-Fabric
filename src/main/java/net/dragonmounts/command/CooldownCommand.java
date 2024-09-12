package net.dragonmounts.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.dragonmounts.capability.IArmorEffectManager.Provider;
import net.dragonmounts.registry.CooldownCategory;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

import java.util.Collection;
import java.util.function.Predicate;

@SuppressWarnings("WrongTypeInTranslationArgs")
public class CooldownCommand {
    public static ArgumentBuilder<ServerCommandSource, ?> register(Predicate<ServerCommandSource> permission) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> single = CommandManager.argument("player", EntityArgumentType.player());
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> multiple = CommandManager.argument("players", EntityArgumentType.players());
        IntegerArgumentType intArg = IntegerArgumentType.integer(0);
        for (CooldownCategory category : CooldownCategory.REGISTRY) {
            final String identifier = category.identifier.toString();
            single.then(CommandManager.literal(identifier).executes(context -> get(context.getSource(), EntityArgumentType.getPlayer(context, "player"), category))
                    .then(CommandManager.argument("value", intArg).executes(context -> set(context.getSource(), EntityArgumentType.getPlayer(context, "player"), category, IntegerArgumentType.getInteger(context, "value"))))
            );
            multiple.then(CommandManager.literal(identifier).executes(context -> get(context.getSource(), EntityArgumentType.getPlayers(context, "players"), category))
                    .then(CommandManager.argument("value", intArg).executes(context -> set(context.getSource(), EntityArgumentType.getPlayers(context, "players"), category, IntegerArgumentType.getInteger(context, "value"))))
            );
        }
        return CommandManager.literal("cooldown").requires(permission).then(single).then(multiple);
    }

    public static int get(ServerCommandSource source, ServerPlayerEntity player, CooldownCategory category) {
        source.sendFeedback(new TranslatableText("commands.dragonmounts.cooldown.get.success",
                player.getDisplayName(),
                category.identifier,
                ((Provider) player).dragonmounts$getManager().getCooldown(category)
        ), true);
        return 1;
    }

    public static int get(ServerCommandSource source, Collection<ServerPlayerEntity> players, CooldownCategory category) {
        if (players.isEmpty()) {
            source.sendError(new TranslatableText("commands.dragonmounts.cooldown.get.failure"));
            return 0;
        }
        for (ServerPlayerEntity player : players) {
            source.sendFeedback(new TranslatableText("commands.dragonmounts.cooldown.get.success",
                    player.getDisplayName(),
                    category.identifier,
                    ((Provider) player).dragonmounts$getManager().getCooldown(category)
            ), true);
        }
        return players.size();
    }

    public static int set(ServerCommandSource source, ServerPlayerEntity player, CooldownCategory category, int value) {
        ((Provider) player).dragonmounts$getManager().setCooldown(category, value);
        source.sendFeedback(new TranslatableText("commands.dragonmounts.cooldown.set.single", player.getDisplayName(), category.identifier, value), true);
        return 1;
    }

    public static int set(ServerCommandSource source, Collection<ServerPlayerEntity> players, CooldownCategory category, int value) {
        if (players.isEmpty()) {
            source.sendError(new TranslatableText("commands.dragonmounts.cooldown.set.failure"));
            return 0;
        }
        for (ServerPlayerEntity player : players) {
            ((Provider) player).dragonmounts$getManager().setCooldown(category, value);
        }
        int size = players.size();
        source.sendFeedback(new TranslatableText("commands.dragonmounts.cooldown.set.multiple", size, category.identifier, value), true);
        return size;
    }
}
