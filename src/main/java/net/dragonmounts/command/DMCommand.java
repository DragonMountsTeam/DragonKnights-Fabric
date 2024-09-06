package net.dragonmounts.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.function.Predicate;

import static net.minecraft.network.chat.HoverEvent.Action.SHOW_ENTITY;


public class DMCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection ignored) {
        Predicate<CommandSourceStack> hasPermissionLevel2 = source -> source.hasPermission(2);
        dispatcher.register(Commands.literal("dragonmounts")
                .then(ConfigCommand.register(source -> source.hasPermission(3)))
                .then(CooldownCommand.register(hasPermissionLevel2))
                .then(FreeCommand.register(hasPermissionLevel2))
                .then(SaveCommand.register(context, hasPermissionLevel2))
                .then(StageCommand.register(hasPermissionLevel2))
                .then(TameCommand.register(hasPermissionLevel2))
                .then(TypeCommand.register(hasPermissionLevel2))
        );
    }

    public static Component createClassCastException(Class<?> from, Class<?> to) {
        return Component.literal("java.lang.ClassCastException: " + from.getName() + " cannot be cast to " + to.getName());
    }

    public static Component createClassCastException(Entity entity, Class<?> clazz) {
        return Component.literal("java.lang.ClassCastException: ").append(
                Component.literal(entity.getClass().getName()).setStyle(
                        Style.EMPTY.withInsertion(entity.getStringUUID())
                                .withHoverEvent(new HoverEvent(SHOW_ENTITY, new HoverEvent.EntityTooltipInfo(entity.getType(), entity.getUUID(), entity.getName())))
                )
        ).append(" cannot be cast to " + clazz.getName());
    }

    public static GameProfile getSingleProfileOrException(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, name);
        if (profiles.isEmpty()) throw EntityArgument.NO_PLAYERS_FOUND.create();
        if (profiles.size() > 1) throw EntityArgument.ERROR_NOT_SINGLE_PLAYER.create();
        return profiles.stream().findAny().get();
    }
}
