package net.dragonmounts;

import net.dragonmounts.capability.ArmorEffectManager;
import net.dragonmounts.command.DMCommand;
import net.dragonmounts.config.ClientConfig;
import net.dragonmounts.config.ServerConfig;
import net.dragonmounts.entity.dragon.ServerDragonEntity;
import net.dragonmounts.init.*;
import net.dragonmounts.network.DMPackets;
import net.dragonmounts.registry.CarriageType;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.registry.DragonVariant;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Locale;

public class DragonMounts implements ModInitializer {
    public static final String MOD_ID = "dragonmounts";
    public static final String ITEM_TRANSLATION_KEY_PREFIX = "item." + MOD_ID + '.';
    public static final String BLOCK_TRANSLATION_KEY_PREFIX = "block." + MOD_ID + '.';

    public static Identifier makeId(String name) {
        return new Identifier(MOD_ID, name.toLowerCase(Locale.ROOT));
    }

    public void onInitialize() {
        DMGameRules.init();
        ClientConfig.init();
        ServerConfig.init();
        DMEntities.init();
        DMItems.init();
        DMBlocks.init();
        DragonVariants.init();
        TrackedDataHandlerRegistry.register(CarriageType.SERIALIZER);
        TrackedDataHandlerRegistry.register(DragonType.SERIALIZER);
        TrackedDataHandlerRegistry.register(DragonVariant.SERIALIZER);
        CommandRegistrationCallback.EVENT.register(DMCommand::register);
        ServerPlayerEvents.COPY_FROM.register((player, priorPlayer, $) -> ArmorEffectManager.onPlayerClone(player, priorPlayer));
        AttackEntityCallback.EVENT.register(DMArmorEffects::meleeChanneling);
        ServerPlayNetworking.registerGlobalReceiver(DMPackets.RIDE_DRAGON_PACKET_ID, DragonMounts::handleRideDragon);
    }

    public static void handleRideDragon(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender) {
        int id = buffer.readVarInt();
        byte flag = buffer.readByte();
        boolean climbing = (flag & 0b0001) == 0b0001;
        boolean descending = (flag & 0b0010) == 0b0010;
        boolean convergePitch = (flag & 0b0100) == 0b0100;
        boolean convergeYaw = (flag & 0b1000) == 0b1000;
        server.execute(() -> {
            Entity entity = player.world.getEntityById(id);
            if (entity instanceof ServerDragonEntity) {
                ((ServerDragonEntity) entity).playerControlledGoal.handlePacket(climbing, descending, convergePitch, convergeYaw);
            }
        });
    }
}
