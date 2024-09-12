package net.dragonmounts.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.dragonmounts.config.ServerConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Predicate;


public class ConfigCommand {
    public static final String OPEN_CONFIG_SCREEN = "/dragonmounts config client";

    public static ArgumentBuilder<ServerCommandSource, ?> register(Predicate<ServerCommandSource> permission) {
        return CommandManager.literal("config")
                .then(CommandManager.literal("client").requires(source -> source.getEntity() instanceof ServerPlayerEntity))
                .then(CommandManager.literal("server").requires(permission).then(ServerConfig.INSTANCE.debug.generateCommand()));
    }
}
