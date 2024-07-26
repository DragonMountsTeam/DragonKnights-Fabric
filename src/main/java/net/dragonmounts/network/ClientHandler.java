package net.dragonmounts.network;

import net.dragonmounts.capability.ArmorEffectManager;
import net.dragonmounts.capability.IArmorEffectManager.Provider;
import net.dragonmounts.client.ClientDragonEntity;
import net.dragonmounts.entity.dragon.DragonLifeStage;
import net.dragonmounts.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.init.DMSounds;
import net.dragonmounts.registry.CooldownCategory;
import net.dragonmounts.util.DragonFood;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class ClientHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    public static void handleArmorRiposte(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender) {
        int id = buffer.readVarInt(), flag = buffer.readVarInt();
        client.execute(() -> {
            ClientWorld level = client.world;
            if (level == null) return;
            Entity entity = level.getEntityById(id);
            if (entity == null) return;
            final double x = entity.getX();
            final double z = entity.getZ();
            if ((flag & 0b01) == 0b01) {
                final double y = entity.getY() + 0.1;
                for (int i = -30; i < 31; ++i) {
                    level.addParticle(ParticleTypes.CLOUD, false, x, y, z, Math.sin(i), 0, Math.cos(i));
                }
                level.playSound(entity.getBlockPos(), SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.PLAYERS, 0.46F, 1.0F, false);
            }
            if ((flag & 0b10) == 0b10) {
                final double y = entity.getY() + 1;
                for (int i = -27; i < 28; ++i) {
                    level.addParticle(ParticleTypes.FLAME, x, y, z, Math.sin(i) / 3, 0, Math.cos(i) / 3);
                }
                level.playSound(entity.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 0.46F, 1.0F, false);
            }
        });
    }

    public static void handleCooldownInit(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender) {
        int size = buffer.readVarInt();
        int[] data = new int[size];
        for (int i = 0; i < size && buffer.isReadable(); ++i) {
            data[i] = buffer.readVarInt();
        }
        client.execute(() -> ArmorEffectManager.init(data));
    }

    public static void handleCooldownSync(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender) {
        int id = buffer.readVarInt(), cd = buffer.readVarInt();
        client.execute(() -> {
            CooldownCategory category = CooldownCategory.REGISTRY.get(id);
            if (category == null) return;
            //noinspection DataFlowIssue
            ((Provider) client.player).dragonmounts$getManager().setCooldown(category, cd);
        });
    }

    public static void handleEggShake(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender) {
        int id = buffer.readVarInt();
        int amplitude = buffer.readVarInt();
        float axis = buffer.readFloat();
        boolean particle = buffer.readBoolean();
        client.execute(() -> {
            ClientWorld level = client.world;
            if (level == null) return;
            Entity entity = level.getEntityById(id);
            if (entity instanceof HatchableDragonEggEntity) {
                BlockState state = ((HatchableDragonEggEntity) entity).syncShake(amplitude, axis);
                if (particle) {
                    level.syncWorldEvent(2001, entity.getBlockPos(), Block.getRawIdFromState(state));
                }
                level.playSound(entity.getX(), entity.getY(), entity.getZ(), DMSounds.DRAGON_HATCHING, SoundCategory.NEUTRAL, 1.0F, 1.0F, false);
            }
        });
    }

    public static void handleDragonSync(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender) {
        int id = buffer.readVarInt(), age = buffer.readVarInt();
        DragonLifeStage stage = DragonLifeStage.byId(buffer.readVarInt());
        client.execute(() -> {
            ClientWorld level = client.world;
            if (level == null) return;
            Entity entity = level.getEntityById(id);
            if (entity instanceof ClientDragonEntity) {
                ((ClientDragonEntity) entity).handleAgeSync(age, stage);
            }
        });
    }

    public static void handleFeedDragon(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender) {
        int id = buffer.readVarInt(), age = buffer.readVarInt();
        DragonLifeStage stage = DragonLifeStage.byId(buffer.readVarInt());
        Item item = Item.byRawId(buffer.readVarInt());
        client.execute(() -> {
            ClientWorld level = client.world;
            if (level == null) return;
            Entity entity = level.getEntityById(id);
            if (entity instanceof ClientDragonEntity) {
                ClientDragonEntity dragon = (ClientDragonEntity) entity;
                ((ClientDragonEntity) entity).handleAgeSync(age, stage);
                DragonFood.get(item).act(dragon, item);
            }
        });
    }

    public static void handleEggSync(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender) {
        int id = buffer.readVarInt(), age = buffer.readVarInt();
        client.execute(() -> {
            ClientWorld level = client.world;
            if (level == null) return;
            Entity entity = level.getEntityById(id);
            if (entity instanceof HatchableDragonEggEntity) {
                ((HatchableDragonEggEntity) entity).setAge(age, false);
            }
        });
    }
}
