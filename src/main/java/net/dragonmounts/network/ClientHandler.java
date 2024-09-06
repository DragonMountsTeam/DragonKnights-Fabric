package net.dragonmounts.network;

import net.dragonmounts.capability.ArmorEffectManager;
import net.dragonmounts.capability.IArmorEffectManager.Provider;
import net.dragonmounts.client.ClientDragonEntity;
import net.dragonmounts.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.registry.CooldownCategory;
import net.dragonmounts.util.DragonFood;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import static net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver;

public class ClientHandler {
    public static void handleArmorRiposte(ArmorRipostePayload payload, ClientPlayNetworking.Context context) {
        var level = context.player().clientLevel;
        var entity = level.getEntity(payload.id());
        if (entity == null) return;
        int flag = payload.flag();
        double x = entity.getX();
        double z = entity.getZ();
        if ((flag & 0b01) == 0b01) {
            double y = entity.getY() + 0.1;
            for (int i = -30; i < 31; ++i) {
                level.addParticle(ParticleTypes.CLOUD, false, x, y, z, Math.sin(i), 0, Math.cos(i));
            }
            level.playLocalSound(entity, SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 0.46F, 1.0F);
        }
        if ((flag & 0b10) == 0b10) {
            double y = entity.getY() + 1;
            for (int i = -27; i < 28; ++i) {
                level.addParticle(ParticleTypes.FLAME, x, y, z, Math.sin(i) / 3, 0, Math.cos(i) / 3);
            }
            level.playLocalSound(entity, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.46F, 1.0F);
        }
    }

    public static void handleCooldownInit(InitCooldownPayload payload, @SuppressWarnings("unused") ClientPlayNetworking.Context context) {
        ArmorEffectManager.init(payload.data());
    }

    public static void handleCooldownSync(SyncCooldownPayload payload, ClientPlayNetworking.Context context) {
        var category = CooldownCategory.REGISTRY.byId(payload.id());
        if (category == null) return;
        ((Provider) context.player()).dragonmounts$getManager().setCooldown(category, payload.cd());
    }

    public static void handleEggShake(ShakeEggPayload payload, ClientPlayNetworking.Context context) {
        var level = context.player().clientLevel;
        if (level.getEntity(payload.id()) instanceof HatchableDragonEggEntity egg) {
            int flag = payload.flag();
            egg.syncShake(
                    payload.amplitude(),
                    (flag & 0b10) == 0b10 ? -payload.axis() : payload.axis(),
                    (flag & 0b01) == 0b01
            );
        }
    }

    public static void handleDragonSync(SyncDragonAgePayload payload, ClientPlayNetworking.Context context) {
        if (context.player().clientLevel.getEntity(payload.id()) instanceof ClientDragonEntity dragon) {
            dragon.setAge(payload.age());
            dragon.setLifeStage(payload.stage(), false, false);
        }
    }

    public static void handleFeedDragon(FeedDragonPayload payload, ClientPlayNetworking.Context context) {
        if (context.player().clientLevel.getEntity(payload.id()) instanceof ClientDragonEntity dragon) {
            dragon.setAge(payload.age());
            dragon.setLifeStage(payload.stage(), false, false);
            // 懒得缓存`payload.food()`了
            DragonFood.get(payload.food()).displayEatingEffects(dragon, payload.food());
        }
    }

    public static void handleEggSync(SyncEggAgePayload payload, ClientPlayNetworking.Context context) {
        if (context.player().clientLevel.getEntity(payload.id()) instanceof HatchableDragonEggEntity egg) {
            egg.setAge(payload.age(), false);
        }
    }

    public static void init() {
        registerGlobalReceiver(SyncCooldownPayload.TYPE, ClientHandler::handleCooldownSync);
        registerGlobalReceiver(ArmorRipostePayload.TYPE, ClientHandler::handleArmorRiposte);
        registerGlobalReceiver(InitCooldownPayload.TYPE, ClientHandler::handleCooldownInit);
        registerGlobalReceiver(ShakeEggPayload.TYPE, ClientHandler::handleEggShake);
        registerGlobalReceiver(SyncDragonAgePayload.TYPE, ClientHandler::handleDragonSync);
        registerGlobalReceiver(FeedDragonPayload.TYPE, ClientHandler::handleFeedDragon);
        registerGlobalReceiver(SyncEggAgePayload.TYPE, ClientHandler::handleEggSync);
    }
}
