package net.dragonmounts.init;

import net.dragonmounts.block.DragonCoreBlock;
import net.dragonmounts.block.HatchableDragonEggBlock;
import net.dragonmounts.block.entity.DragonCoreBlockEntity;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.util.Values;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.ToIntFunction;

import static net.dragonmounts.DragonMounts.makeId;

public class DMBlocks {
    private static final ToIntFunction<BlockState> DRAGON_EGG_LUMINANCE = state -> 1;
    public static final HatchableDragonEggBlock AETHER_DRAGON_EGG = registerDragonEggBlock("aether_dragon_egg", DragonTypes.AETHER, MapColor.COLOR_LIGHT_BLUE, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock ENCHANT_DRAGON_EGG = registerDragonEggBlock("enchant_dragon_egg", DragonTypes.ENCHANT, MapColor.COLOR_PURPLE, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock ENDER_DRAGON_EGG = registerDragonEggBlock("ender_dragon_egg", DragonTypes.ENDER, MapColor.COLOR_BLACK, new Item.Properties().rarity(Rarity.EPIC));
    public static final HatchableDragonEggBlock FIRE_DRAGON_EGG = registerDragonEggBlock("fire_dragon_egg", DragonTypes.FIRE, MapColor.FIRE, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock FOREST_DRAGON_EGG = registerDragonEggBlock("forest_dragon_egg", DragonTypes.FOREST, MapColor.COLOR_GREEN, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock ICE_DRAGON_EGG = registerDragonEggBlock("ice_dragon_egg", DragonTypes.ICE, MapColor.SNOW, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock MOONLIGHT_DRAGON_EGG = registerDragonEggBlock("moonlight_dragon_egg", DragonTypes.MOONLIGHT, MapColor.COLOR_BLUE, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock NETHER_DRAGON_EGG = registerDragonEggBlock("nether_dragon_egg", DragonTypes.NETHER, MapColor.NETHER, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock SCULK_DRAGON_EGG = registerDragonEggBlock("sculk_dragon_egg", DragonTypes.SCULK, MapColor.COLOR_BLACK, new Item.Properties().rarity(Rarity.RARE));
    public static final HatchableDragonEggBlock SKELETON_DRAGON_EGG = registerDragonEggBlock("skeleton_dragon_egg", DragonTypes.SKELETON, MapColor.QUARTZ, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock STORM_DRAGON_EGG = registerDragonEggBlock("storm_dragon_egg", DragonTypes.STORM, MapColor.WOOL, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock SUNLIGHT_DRAGON_EGG = registerDragonEggBlock("sunlight_dragon_egg", DragonTypes.SUNLIGHT, MapColor.COLOR_YELLOW, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock TERRA_DRAGON_EGG = registerDragonEggBlock("terra_dragon_egg", DragonTypes.TERRA, MapColor.DIRT, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock WATER_DRAGON_EGG = registerDragonEggBlock("water_dragon_egg", DragonTypes.WATER, MapColor.WATER, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock WITHER_DRAGON_EGG = registerDragonEggBlock("wither_dragon_egg", DragonTypes.WITHER, MapColor.COLOR_GRAY, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final HatchableDragonEggBlock ZOMBIE_DRAGON_EGG = registerDragonEggBlock("zombie_dragon_egg", DragonTypes.ZOMBIE, MapColor.TERRACOTTA_GREEN, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final Values<HatchableDragonEggBlock> BUILTIN_EGGS = new Values<>(DMBlocks.class, HatchableDragonEggBlock.class);
    public static final Block DRAGON_NEST;
    public static final DragonCoreBlock DRAGON_CORE;
    static {
        final Block block = new Block(BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(1.0F).sound(SoundType.WOOD).ignitedByLava());
        final ResourceLocation identifier = makeId("dragon_nest");
        FlammableBlockRegistry.getDefaultInstance().add(block, 30, 80);
        Registry.register(BuiltInRegistries.ITEM, identifier, new BlockItem(block, new Item.Properties()));
        DRAGON_NEST = Registry.register(BuiltInRegistries.BLOCK, identifier, block);
    }

    static {
        BlockBehaviour.StatePredicate predicate = ($, level, pos) -> level.getBlockEntity(pos) instanceof DragonCoreBlockEntity core && core.isClosed();
        var block = new DragonCoreBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(2000, 600)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .sound(new SoundType(1.0F, 1.0F, SoundEvents.CHEST_LOCKED, SoundEvents.DEEPSLATE_STEP, SoundEvents.BOOK_PUT, SoundEvents.DEEPSLATE_STEP, SoundEvents.DEEPSLATE_FALL))
                .noLootTable()
                .forceSolidOn()
                .dynamicShape()
                .noOcclusion()
                .isSuffocating(predicate)
                .isViewBlocking(predicate)
                .pushReaction(PushReaction.BLOCK)
        );
        var identifier = makeId("dragon_core");
        Registry.register(BuiltInRegistries.ITEM, identifier, new BlockItem(block, new Item.Properties().rarity(Rarity.RARE)));
        DRAGON_CORE = Registry.register(BuiltInRegistries.BLOCK, identifier, block);
    }

    static HatchableDragonEggBlock registerDragonEggBlock(String name, DragonType type, MapColor color, Item.Properties props) {
        var block = new HatchableDragonEggBlock(type, BlockBehaviour.Properties.of().mapColor(color).strength(0.0F, 9.0F).lightLevel(DRAGON_EGG_LUMINANCE).noOcclusion());
        var identifier = makeId(name);
        type.bindInstance(HatchableDragonEggBlock.class, block);
        Registry.register(BuiltInRegistries.ITEM, identifier, new BlockItem(block, props.component(DMDataComponents.DRAGON_TYPE, type)));
        return Registry.register(BuiltInRegistries.BLOCK, identifier, block);
    }

    public static void attachBlocks(CreativeModeTab.ItemDisplayParameters context, CreativeModeTab.Output entries) {
        entries.accept(DRAGON_NEST);
        BUILTIN_EGGS.forEach(entries::accept);
        DragonVariants.BUILTIN_VALUES.forEach(variant -> entries.accept(variant.headItem));
    }

    public static void init() {}
}
