package net.dragonmounts.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.function.Predicate;

import static net.minecraft.text.HoverEvent.Action.SHOW_ENTITY;

public class DMCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean ignored) {
        Predicate<ServerCommandSource> hasPermissionLevel2 = source -> source.hasPermissionLevel(2);
        dispatcher.register(CommandManager.literal("dragonmounts")
                .then(ConfigCommand.register(source -> source.hasPermissionLevel(3)))
                .then(CooldownCommand.register(hasPermissionLevel2))
                .then(FreeCommand.register(hasPermissionLevel2))
                .then(RideCommand.register(hasPermissionLevel2))
                .then(SaveCommand.register(hasPermissionLevel2))
                .then(StageCommand.register(hasPermissionLevel2))
                .then(TameCommand.register(hasPermissionLevel2))
                .then(TypeCommand.register(hasPermissionLevel2))
        );
    }

    public static Text createClassCastException(Class<?> from, Class<?> to) {
        return new LiteralText("java.lang.ClassCastException: " + from.getName() + " cannot be cast to " + to.getName());
    }

    public static Text createClassCastException(Entity entity, Class<?> clazz) {
        return new LiteralText("java.lang.ClassCastException: ").append(
                new LiteralText(entity.getClass().getName()).setStyle(
                        Style.EMPTY.withInsertion(entity.getUuidAsString())
                                .withHoverEvent(new HoverEvent(SHOW_ENTITY, new HoverEvent.EntityContent(entity.getType(), entity.getUuid(), entity.getName())))
                )
        ).append(" cannot be cast to " + clazz.getName());
    }

    public static GameProfile getSingleProfileOrException(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, name);
        if (profiles.isEmpty()) {
            throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
        } else if (profiles.size() > 1) {
            throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.create();
        }
        return profiles.stream().findAny().get();
    }
}
