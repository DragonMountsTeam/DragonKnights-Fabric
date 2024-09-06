package net.dragonmounts.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.dragonmounts.capability.IArmorEffectManager.Provider;
import net.dragonmounts.registry.CooldownCategory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.function.Predicate;

public class CooldownCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(Predicate<CommandSourceStack> permission) {
        var single = Commands.argument("player", EntityArgument.player());
        var multiple = Commands.argument("players", EntityArgument.players());
        for (var category : CooldownCategory.REGISTRY) {
            var identifier = category.identifier.toString();
            single.then(Commands.literal(identifier).executes(context -> get(context.getSource(), EntityArgument.getPlayer(context, "player"), category))
                    .then(Commands.argument("value", IntegerArgumentType.integer(0)).executes(context -> set(context.getSource(), EntityArgument.getPlayer(context, "player"), category, IntegerArgumentType.getInteger(context, "value"))))
            );
            multiple.then(Commands.literal(identifier).executes(context -> get(context.getSource(), EntityArgument.getPlayers(context, "players"), category))
                    .then(Commands.argument("value", IntegerArgumentType.integer(0)).executes(context -> set(context.getSource(), EntityArgument.getPlayers(context, "players"), category, IntegerArgumentType.getInteger(context, "value"))))
            );
        }
        return Commands.literal("cooldown").requires(permission).then(single).then(multiple);
    }

    public static int get(CommandSourceStack source, ServerPlayer player, CooldownCategory category) {
        source.sendSuccess(() -> Component.translatable("commands.dragonmounts.cooldown.get.success",
                player.getDisplayName(),
                category.identifier,
                ((Provider) player).dragonmounts$getManager().getCooldown(category)
        ), true);
        return 1;
    }

    public static int get(CommandSourceStack source, Collection<ServerPlayer> players, CooldownCategory category) {
        if (players.isEmpty()) {
            source.sendFailure(Component.translatable("commands.dragonmounts.cooldown.get.failure"));
            return 0;
        }
        for (var player : players) {
            source.sendSuccess(() -> Component.translatable("commands.dragonmounts.cooldown.get.success",
                    player.getDisplayName(),
                    category.identifier,
                    ((Provider) player).dragonmounts$getManager().getCooldown(category)
            ), true);
        }
        return players.size();
    }

    public static int set(CommandSourceStack source, ServerPlayer player, CooldownCategory category, int value) {
        ((Provider) player).dragonmounts$getManager().setCooldown(category, value);
        source.sendSuccess(() -> Component.translatable("commands.dragonmounts.cooldown.set.single", player.getDisplayName(), category.identifier, value), true);
        return 1;
    }

    public static int set(CommandSourceStack source, Collection<ServerPlayer> players, CooldownCategory category, int value) {
        if (players.isEmpty()) {
            source.sendFailure(Component.translatable("commands.dragonmounts.cooldown.set.failure"));
            return 0;
        }
        for (var player : players) {
            ((Provider) player).dragonmounts$getManager().setCooldown(category, value);
        }
        int size = players.size();
        source.sendSuccess(() -> Component.translatable("commands.dragonmounts.cooldown.set.multiple", size, category.identifier, value), true);
        return size;
    }
}
