package net.dragonmounts.client;

import net.dragonmounts.api.DragonScaleArmorSuit;
import net.dragonmounts.client.gui.DragonCoreScreen;
import net.dragonmounts.client.gui.DragonInventoryScreen;
import net.dragonmounts.client.render.DragonEggRenderer;
import net.dragonmounts.client.render.block.DragonCoreRenderer;
import net.dragonmounts.client.render.block.DragonHeadRenderer;
import net.dragonmounts.client.render.dragon.TameableDragonRenderer;
import net.dragonmounts.client.variant.VariantAppearances;
import net.dragonmounts.init.*;
import net.dragonmounts.item.DragonScaleBowItem;
import net.dragonmounts.item.DragonScaleShieldItem;
import net.dragonmounts.item.DragonSpawnEggItem;
import net.dragonmounts.network.ClientHandler;
import net.dragonmounts.registry.DragonType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.impl.client.rendering.ArmorProviderExtensions;
import net.fabricmc.fabric.impl.client.rendering.ColorProviderRegistryImpl;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.util.Identifier;

import static net.dragonmounts.network.DMPackets.*;
import static net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver;

@Environment(EnvType.CLIENT)
public class DragonMountsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VariantAppearances.bindAppearance();
        Identifier pull = new Identifier("pull");
        Identifier pulling = new Identifier("pulling");
        Identifier blocking = new Identifier("blocking");
        ModelPredicateProvider duration = (stack, $, entity) -> entity == null ? 0.0F : entity.getActiveItem() != stack ? 0.0F : (stack.getMaxUseTime() - entity.getItemUseTimeLeft()) / 20.0F;
        ModelPredicateProvider isUsingItem = (stack, $, entity) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F;
        for (DragonType type : DragonType.REGISTRY) {
            DragonScaleBowItem bow = type.getInstance(DragonScaleBowItem.class, null);
            if (bow != null) {
                FabricModelPredicateProviderRegistry.register(bow, pull, duration);
                FabricModelPredicateProviderRegistry.register(bow, pulling, isUsingItem);
            }
            DragonScaleShieldItem shield = type.getInstance(DragonScaleShieldItem.class, null);
            if (shield != null) {
                FabricModelPredicateProviderRegistry.register(shield, blocking, isUsingItem);
            }
            DragonScaleArmorSuit suit = type.getInstance(DragonScaleArmorSuit.class, null);
            if (suit != null) {
                ArmorRenderingRegistry.TextureProvider provider = suit::getArmorTexture;
                ((ArmorProviderExtensions) suit.helmet).fabric_setArmorTextureProvider(provider);
                ((ArmorProviderExtensions) suit.chestplate).fabric_setArmorTextureProvider(provider);
                ((ArmorProviderExtensions) suit.leggings).fabric_setArmorTextureProvider(provider);
                ((ArmorProviderExtensions) suit.boots).fabric_setArmorTextureProvider(provider);
            }
            DragonSpawnEggItem egg = type.getInstance(DragonSpawnEggItem.class, null);
            if (egg != null) {
                ColorProviderRegistryImpl.ITEM.register(($, index) -> egg.getColor(index), egg);
            }
        }
        ScreenRegistry.register(DMScreenHandlers.DRAGON_CORE, DragonCoreScreen::new);
        ScreenRegistry.register(DMScreenHandlers.DRAGON_INVENTORY, DragonInventoryScreen::new);
        BlockEntityRendererRegistry.INSTANCE.register(DMBlockEntities.DRAGON_CORE, DragonCoreRenderer::new);
        BlockEntityRendererRegistry.INSTANCE.register(DMBlockEntities.DRAGON_HEAD, DragonHeadRenderer::new);
        BuiltinItemRendererRegistry.INSTANCE.register(DMBlocks.DRAGON_CORE, DragonCoreRenderer.ITEM_RENDERER);
        DragonVariants.BUILTIN_VALUES.forEach(variant -> BuiltinItemRendererRegistry.INSTANCE.register(variant.headItem, DragonHeadRenderer.ITEM_RENDERER));
        EntityRendererRegistry.INSTANCE.register(DMEntities.HATCHABLE_DRAGON_EGG, (dispatcher, $) -> new DragonEggRenderer(dispatcher));
        EntityRendererRegistry.INSTANCE.register(DMEntities.TAMEABLE_DRAGON, (dispatcher, $) -> new TameableDragonRenderer(dispatcher));
        registerPacketHandler();
        DMKeyBindings.register();
    }

    private static void registerPacketHandler() {
        registerGlobalReceiver(ARMOR_RIPOSTE_PACKET_ID, ClientHandler::handleArmorRiposte);
        registerGlobalReceiver(INIT_COOLDOWN_PACKET_ID, ClientHandler::handleCooldownInit);
        registerGlobalReceiver(SYNC_COOLDOWN_PACKET_ID, ClientHandler::handleCooldownSync);
        registerGlobalReceiver(SHAKE_DRAGON_EGG_PACKET_ID, ClientHandler::handleEggShake);
        registerGlobalReceiver(SYNC_DRAGON_AGE_PACKET_ID, ClientHandler::handleDragonSync);
        registerGlobalReceiver(FEED_DRAGON_PACKET_ID, ClientHandler::handleFeedDragon);
        registerGlobalReceiver(SYNC_EGG_AGE_PACKET_ID, ClientHandler::handleEggSync);
    }
}
