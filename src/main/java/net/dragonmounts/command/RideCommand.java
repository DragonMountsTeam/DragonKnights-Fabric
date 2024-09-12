package net.dragonmounts.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

import java.util.function.Predicate;

public class RideCommand {

    public static ArgumentBuilder<ServerCommandSource, ?> register(Predicate<ServerCommandSource> permission) {
        return CommandManager.literal("ride").requires(permission).then(
                CommandManager.argument("target", EntityArgumentType.entity())
                        .then(CommandManager.literal("mount")
                                .then(CommandManager.argument("vehicle", EntityArgumentType.entity())
                                        .executes(context -> mount(context.getSource(), EntityArgumentType.getEntity(context, "target"), EntityArgumentType.getEntity(context, "vehicle")))))
                        .then(CommandManager.literal("dismount")
                                .executes(context -> dismount(context.getSource(), EntityArgumentType.getEntity(context, "target"))))
        );
    }

    public static int mount(ServerCommandSource source, Entity target, Entity vehicle) {
        Entity current = target.getVehicle();
        if (current != null) {
            source.sendError(new TranslatableText("commands.ride.already_riding", target.getDisplayName(), current.getDisplayName()));
            return 0;
        }
        if (vehicle.getType() == EntityType.PLAYER) {
            source.sendError(new TranslatableText("commands.ride.mount.failure.cant_ride_players"));
            return 0;
        }
        if (target.streamPassengersRecursively().anyMatch(vehicle::isPartOf)) {
            source.sendError(new TranslatableText("commands.ride.mount.failure.loop"));
            return 0;
        }
        if (target.world != vehicle.world) {
            source.sendError(new TranslatableText("commands.ride.mount.failure.wrong_dimension"));
            return 0;
        }
        if (target.startRiding(vehicle, true)) {
            source.sendFeedback(new TranslatableText("commands.ride.mount.success", target.getDisplayName(), vehicle.getDisplayName()), true);
            return 1;
        }
        source.sendError(new TranslatableText("commands.ride.mount.failure.generic"));
        return 0;
    }

    public static int dismount(ServerCommandSource source, Entity target) {
        Entity vehicle = target.getVehicle();
        if (vehicle == null) {
            source.sendError(new TranslatableText("commands.ride.not_riding"));
            return 0;
        }
        target.stopRiding();
        source.sendFeedback(new TranslatableText("commands.ride.dismount.success", target.getDisplayName(), vehicle.getDisplayName()), true);
        return 1;
    }
}
