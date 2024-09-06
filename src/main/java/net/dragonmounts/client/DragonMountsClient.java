package net.dragonmounts.client;

import net.dragonmounts.client.gui.DragonCoreScreen;
import net.dragonmounts.client.gui.DragonInventoryScreen;
import net.dragonmounts.client.model.dragon.DragonModelProvider;
import net.dragonmounts.client.renderer.DragonEggRenderer;
import net.dragonmounts.client.renderer.block.DragonCoreRenderer;
import net.dragonmounts.client.renderer.block.DragonHeadRenderer;
import net.dragonmounts.client.renderer.dragon.TameableDragonRenderer;
import net.dragonmounts.client.variant.VariantAppearance;
import net.dragonmounts.client.variant.VariantAppearances;
import net.dragonmounts.init.*;
import net.dragonmounts.item.DragonScaleBowItem;
import net.dragonmounts.item.DragonScaleShieldItem;
import net.dragonmounts.network.ClientHandler;
import net.dragonmounts.registry.DragonType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class DragonMountsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VariantAppearances.bindAppearance();
        var pull = ResourceLocation.withDefaultNamespace("pull");
        var pulling = ResourceLocation.withDefaultNamespace("pulling");
        var blocking = ResourceLocation.withDefaultNamespace("blocking");
        ClampedItemPropertyFunction duration = (stack, $, entity, i) -> entity == null ? 0.0F : entity.getUseItem() != stack ? 0.0F : (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F;
        ClampedItemPropertyFunction isUsingItem = (stack, $, entity, i) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
        for (var type : DragonType.REGISTRY) {
            var bow = type.getInstance(DragonScaleBowItem.class, null);
            if (bow != null) {
                ItemProperties.register(bow, pull, duration);
                ItemProperties.register(bow, pulling, isUsingItem);
            }
            var shield = type.getInstance(DragonScaleShieldItem.class, null);
            if (shield != null) {
                ItemProperties.register(shield, blocking, isUsingItem);
            }
        }
        DMItems.forEachSpawnEgg(egg -> ColorProviderRegistry.ITEM.register(($, index) -> egg.getColor(index), egg));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(DMItems::attachSpawnEggs);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries ->
                DragonVariants.BUILTIN_VALUES.forEach(variant -> entries.accept(variant.headItem))
        );
        MenuScreens.register(DMScreenHandlers.DRAGON_CORE, DragonCoreScreen::new);
        MenuScreens.register(DMScreenHandlers.DRAGON_INVENTORY, DragonInventoryScreen::new);
        EntityModelLayerRegistry.registerModelLayer(VariantAppearance.MODEL, DragonModelProvider.INSTANCE);
        BlockEntityRenderers.register(DMBlockEntities.DRAGON_CORE, DragonCoreRenderer::new);
        BlockEntityRenderers.register(DMBlockEntities.DRAGON_HEAD, DragonHeadRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(DMBlocks.DRAGON_CORE, DragonCoreRenderer.ITEM_RENDERER);
        DragonVariants.BUILTIN_VALUES.forEach(DragonHeadRenderer.INSTANCE);
        EntityRendererRegistry.register(DMEntities.HATCHABLE_DRAGON_EGG, DragonEggRenderer::new);
        registerEntityRenderer(DMEntities.TAMEABLE_DRAGON, TameableDragonRenderer::new);
        ClientHandler.init();
        DMKeyMappings.register();
    }

    @SuppressWarnings({"rawtypes", "unchecked", "SameParameterValue"})
    static void registerEntityRenderer(EntityType type, EntityRendererProvider provider) {
        EntityRendererRegistry.register(type, provider);
    }

    public static Level getLevel() {
        return Minecraft.getInstance().level;
    }
}
