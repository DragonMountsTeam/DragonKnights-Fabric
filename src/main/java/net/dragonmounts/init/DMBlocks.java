package net.dragonmounts.init;

import net.dragonmounts.block.DragonCoreBlock;
import net.dragonmounts.block.HatchableDragonEggBlock;
import net.dragonmounts.block.entity.DragonCoreBlockEntity;
import net.dragonmounts.registry.DragonType;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import java.util.function.ToIntFunction;

import static net.dragonmounts.DragonMounts.MOD_ID;
import static net.dragonmounts.init.DMItemGroups.block;
import static net.dragonmounts.init.DMItemGroups.none;

public class DMBlocks {
    private static final ToIntFunction<BlockState> DRAGON_EGG_LUMINANCE = state -> 1;
    public static final HatchableDragonEggBlock AETHER_DRAGON_EGG = registerDragonEggBlock("aether_dragon_egg", DragonTypes.AETHER, MapColor.LIGHT_BLUE, block().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock ENCHANT_DRAGON_EGG = registerDragonEggBlock("enchant_dragon_egg", DragonTypes.ENCHANT, MapColor.PURPLE, block().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock ENDER_DRAGON_EGG = registerDragonEggBlock("ender_dragon_egg", DragonTypes.ENDER, MapColor.BLACK, block().rarity(Rarity.EPIC));
    public static final HatchableDragonEggBlock FIRE_DRAGON_EGG = registerDragonEggBlock("fire_dragon_egg", DragonTypes.FIRE, MapColor.BRIGHT_RED, block().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock FOREST_DRAGON_EGG = registerDragonEggBlock("forest_dragon_egg", DragonTypes.FOREST, MapColor.GREEN, block().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock ICE_DRAGON_EGG = registerDragonEggBlock("ice_dragon_egg", DragonTypes.ICE, MapColor.WHITE, block().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock MOONLIGHT_DRAGON_EGG = registerDragonEggBlock("moonlight_dragon_egg", DragonTypes.MOONLIGHT, MapColor.BLUE, block().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock NETHER_DRAGON_EGG = registerDragonEggBlock("nether_dragon_egg", DragonTypes.NETHER, MapColor.DARK_RED, block().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock SCULK_DRAGON_EGG = registerDragonEggBlock("sculk_dragon_egg", DragonTypes.SCULK, MapColor.BLACK, block().rarity(Rarity.RARE));
    public static final HatchableDragonEggBlock SKELETON_DRAGON_EGG = registerDragonEggBlock("skeleton_dragon_egg", DragonTypes.SKELETON, MapColor.OFF_WHITE, block().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock STORM_DRAGON_EGG = registerDragonEggBlock("storm_dragon_egg", DragonTypes.STORM, MapColor.WHITE_GRAY, block().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock SUNLIGHT_DRAGON_EGG = registerDragonEggBlock("sunlight_dragon_egg", DragonTypes.SUNLIGHT, MapColor.YELLOW, block().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock TERRA_DRAGON_EGG = registerDragonEggBlock("terra_dragon_egg", DragonTypes.TERRA, MapColor.DIRT_BROWN, block().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock WATER_DRAGON_EGG = registerDragonEggBlock("water_dragon_egg", DragonTypes.WATER, MapColor.WATER_BLUE, block().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock WITHER_DRAGON_EGG = registerDragonEggBlock("wither_dragon_egg", DragonTypes.WITHER, MapColor.GRAY, block().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock ZOMBIE_DRAGON_EGG = registerDragonEggBlock("zombie_dragon_egg", DragonTypes.ZOMBIE, MapColor.TERRACOTTA_GREEN, block().rarity(Rarity.UNCOMMON));
    public static final Block DRAGON_NEST;
    public static final DragonCoreBlock DRAGON_CORE;
    static {
        final Block block = new Block(AbstractBlock.Settings.of(Material.STONE, MapColor.OAK_TAN).strength(1.0F).sounds(BlockSoundGroup.WOOD));
        final Identifier identifier = new Identifier(MOD_ID, "dragon_nest");
        FlammableBlockRegistry.getDefaultInstance().add(block, 30, 77);
        Registry.register(Registry.ITEM, identifier, new BlockItem(block, block()));
        DRAGON_NEST = Registry.register(Registry.BLOCK, identifier, block);
    }

    static {
        final AbstractBlock.ContextPredicate predicate = ($, level, pos) -> {
            BlockEntity entity = level.getBlockEntity(pos);
            return entity instanceof DragonCoreBlockEntity && ((DragonCoreBlockEntity) entity).isClosed();
        };
        final DragonCoreBlock block = new DragonCoreBlock(AbstractBlock.Settings.of(Material.PORTAL, MapColor.BLACK)
                .strength(2000, 600)
                .sounds(new BlockSoundGroup(1.0F, 1.0F, SoundEvents.BLOCK_CHEST_LOCKED, SoundEvents.BLOCK_STONE_STEP, SoundEvents.ITEM_BOOK_PUT, SoundEvents.BLOCK_STONE_HIT, SoundEvents.BLOCK_STONE_FALL))
                .dynamicBounds().nonOpaque()
                .suffocates(predicate)
                .blockVision(predicate)
        );
        final Identifier identifier = new Identifier(MOD_ID, "dragon_core");
        Registry.register(Registry.ITEM, identifier, new BlockItem(block, none().rarity(Rarity.RARE)));
        DRAGON_CORE = Registry.register(Registry.BLOCK, identifier, block);
    }

    private static HatchableDragonEggBlock registerDragonEggBlock(String name, DragonType type, MapColor color, Item.Settings settings) {
        final Identifier identifier = new Identifier(MOD_ID, name);
        final HatchableDragonEggBlock block = new HatchableDragonEggBlock(type, FabricBlockSettings.of(Material.EGG, color).strength(0.0F, 9.0F).luminance(DRAGON_EGG_LUMINANCE).nonOpaque());
        type.bindInstance(HatchableDragonEggBlock.class, block);
        Registry.register(Registry.ITEM, identifier, new BlockItem(block, settings));
        return Registry.register(Registry.BLOCK, identifier, block);
    }

    public static void init() {}
}
