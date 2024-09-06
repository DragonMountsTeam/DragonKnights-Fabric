package net.dragonmounts;

import net.dragonmounts.capability.ArmorEffectManager;
import net.dragonmounts.command.DMCommand;
import net.dragonmounts.config.ClientConfig;
import net.dragonmounts.config.ServerConfig;
import net.dragonmounts.init.*;
import net.dragonmounts.network.*;
import net.dragonmounts.registry.CarriageType;
import net.dragonmounts.registry.CooldownCategory;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.registry.DragonVariant;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import static net.minecraft.resources.ResourceKey.createRegistryKey;

public class DragonMounts implements ModInitializer {
    public static final String MOD_ID = "dragonmounts";
    private static final ResourceLocation ROOT = ResourceLocation.fromNamespaceAndPath(MOD_ID, "root");
    public static final String ITEM_TRANSLATION_KEY_PREFIX = "item." + MOD_ID + '.';
    public static final String BLOCK_TRANSLATION_KEY_PREFIX = "block." + MOD_ID + '.';
    public static final ResourceKey<Registry<CarriageType>> CARRIAGE_TYPE = createRegistryKey(makeId("carriage_type"));
    public static final ResourceKey<Registry<DragonType>> DRAGON_TYPE = createRegistryKey(makeId("dragon_type"));
    public static final ResourceKey<Registry<DragonVariant>> DRAGON_VARIANT = createRegistryKey(makeId("dragon_variant"));
    public static final ResourceKey<Registry<CooldownCategory>> COOLDOWN_CATEGORY = createRegistryKey(makeId("cooldown_category"));

    /**
     * to skip namespace checking
     */
    public static ResourceLocation makeId(String name) {
        return ROOT.withPath(name);
    }

    public void onInitialize() {
        DMGameRules.init();
        ClientConfig.init();
        ServerConfig.init();
        DMEntities.init();
        DMItems.init();
        DMBlocks.init();
        DMBlockEntities.init();
        DMScreenHandlers.init();
        DMItemGroups.init();
        DragonVariants.init();
        CarriageTypes.init();
        DMSounds.init();
        registerPayload(PayloadTypeRegistry.playS2C());
        registerPayload(PayloadTypeRegistry.playC2S());
        EntityDataSerializers.registerSerializer(CarriageType.SERIALIZER);
        EntityDataSerializers.registerSerializer(DragonType.SERIALIZER);
        EntityDataSerializers.registerSerializer(DragonVariant.SERIALIZER);
        CommandRegistrationCallback.EVENT.register(DMCommand::register);
        ServerPlayerEvents.COPY_FROM.register((player, priorPlayer, $) -> ArmorEffectManager.onPlayerClone(player, priorPlayer));
        AttackEntityCallback.EVENT.register(DMArmorEffects::meleeChanneling);
        ServerPlayNetworking.registerGlobalReceiver(RideDragonPayload.TYPE, RideDragonPayload::handle);
    }

    public static void registerPayload(PayloadTypeRegistry<RegistryFriendlyByteBuf> registry) {
        registry.register(ArmorRipostePayload.TYPE, ArmorRipostePayload.CODEC);
        registry.register(FeedDragonPayload.TYPE, FeedDragonPayload.CODEC);
        registry.register(InitCooldownPayload.TYPE, InitCooldownPayload.CODEC);
        registry.register(RideDragonPayload.TYPE, RideDragonPayload.CODEC);
        registry.register(ShakeEggPayload.TYPE, ShakeEggPayload.CODEC);
        registry.register(SyncCooldownPayload.TYPE, SyncCooldownPayload.CODEC);
        registry.register(SyncDragonAgePayload.TYPE, SyncDragonAgePayload.CODEC);
        registry.register(SyncEggAgePayload.TYPE, SyncEggAgePayload.CODEC);
    }
}
