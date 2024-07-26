package net.dragonmounts.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
    public static final Predicate<ServerCommandSource> HAS_PERMISSION_LEVEL_3 = source -> source.hasPermissionLevel(3);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("dragonmounts")
                .then(ConfigCommand.register(dedicated))
                .then(CooldownCommand.register())
                .then(FreeCommand.register())
                .then(StageCommand.register())
                .then(TameCommand.register())
                .then(TypeCommand.register());
        dispatcher.register(builder);
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
