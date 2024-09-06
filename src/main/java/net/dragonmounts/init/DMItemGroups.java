package net.dragonmounts.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import static net.dragonmounts.DragonMounts.makeId;

public class DMItemGroups {
    public static final ResourceKey<CreativeModeTab> BLOCK_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, makeId("blocks"));
    public static final ResourceKey<CreativeModeTab> ITEM_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, makeId("items"));
    public static final ResourceKey<CreativeModeTab> TOOL_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, makeId("tools"));

    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, BLOCK_TAB, FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.dragonmounts.blocks"))
                .icon(() -> new ItemStack(DMBlocks.ENDER_DRAGON_EGG))
                .displayItems(DMBlocks::attachBlocks).build()
        );
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ITEM_TAB, FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.dragonmounts.items"))
                .icon(() -> new ItemStack(DMItems.ENDER_DRAGON_SCALES))
                .displayItems(DMItems::attachItems).build()
        );
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TOOL_TAB, FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.dragonmounts.tools"))
                .icon(() -> new ItemStack(DMItems.ENDER_DRAGON_SCALE_SWORD))
                .displayItems(DMItems::attachTools).build()
        );
    }
}
