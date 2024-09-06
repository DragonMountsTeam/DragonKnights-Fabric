package net.dragonmounts.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.dragonmounts.config.ServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public class ConfigCommand {
    public static final String OPEN_CONFIG_SCREEN = "/dragonmounts config client";

    public static ArgumentBuilder<CommandSourceStack, ?> register(Predicate<CommandSourceStack> permission) {
        return Commands.literal("config")
                .then(Commands.literal("client").requires(source -> source.getEntity() instanceof ServerPlayer))
                .then(Commands.literal("server").requires(permission).then(ServerConfig.INSTANCE.debug.generateCommand()));
    }
}
