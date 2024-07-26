package net.dragonmounts.init;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.impl.item.group.ItemGroupExtensions;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import static net.dragonmounts.DragonMounts.MOD_ID;

public class DMItemGroups {
    public static final ItemGroup BLOCK_TAB;
    public static final ItemGroup ITEM_TAB;
    public static final ItemGroup TOOL_TAB;

    public static FabricItemSettings none() {
        return new FabricItemSettings();
    }

    public static FabricItemSettings block() {
        return new FabricItemSettings().group(BLOCK_TAB);
    }

    public static FabricItemSettings item() {
        return new FabricItemSettings().group(ITEM_TAB);
    }

    public static FabricItemSettings tool() {
        return new FabricItemSettings().group(TOOL_TAB);
    }

    static {
        ItemGroupExtensions extensions = ((ItemGroupExtensions) ItemGroup.INVENTORY);
        extensions.fabric_expandArray();
        extensions.fabric_expandArray();
        extensions.fabric_expandArray();
        int index = ItemGroup.GROUPS.length;
        TOOL_TAB = new Impl(--index, "tools") {
            @Override
            public ItemStack createIcon() {
                return new ItemStack(DMItems.ENDER_DRAGON_SCALE_SWORD);
            }
        };
        ITEM_TAB = new Impl(--index, "items") {
            @Override
            public ItemStack createIcon() {
                return new ItemStack(DMItems.ENDER_DRAGON_SCALES);
            }
        };
        BLOCK_TAB = new Impl(--index, "blocks") {
            @Override
            public ItemStack createIcon() {
                return new ItemStack(DMBlocks.ENDER_DRAGON_EGG);
            }
        };
    }

    public static abstract class Impl extends ItemGroup {
        public Impl(int index, String name) {
            super(index, MOD_ID + '.' + name);
            super.setName(MOD_ID + '/' + name);
        }

        @Override
        public ItemGroup setName(String name) {
            return this;
        }
    }
}
