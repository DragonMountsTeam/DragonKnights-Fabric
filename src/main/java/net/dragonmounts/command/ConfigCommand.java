package net.dragonmounts.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.dragonmounts.config.ServerConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.dragonmounts.command.DMCommand.HAS_PERMISSION_LEVEL_3;


public class ConfigCommand {
    public static final String OPEN_CONFIG_SCREEN = "/dragonmounts config client";

    public static ArgumentBuilder<ServerCommandSource, ?> register(boolean dedicated) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("config")
                .then(CommandManager.literal("server")
                        .requires(HAS_PERMISSION_LEVEL_3)
                        .then(ServerConfig.INSTANCE.debug.generateCommand())
                );
        return dedicated ? builder : builder.then(CommandManager.literal("client"));
    }
}
