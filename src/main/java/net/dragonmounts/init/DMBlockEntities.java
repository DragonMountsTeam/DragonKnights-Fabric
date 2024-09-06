package net.dragonmounts.init;

import net.dragonmounts.block.entity.DragonCoreBlockEntity;
import net.dragonmounts.block.entity.DragonHeadBlockEntity;
import net.dragonmounts.registry.DragonVariant;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Collections;
import java.util.Set;

import static net.dragonmounts.DragonMounts.makeId;

public class DMBlockEntities {
    public static final BlockEntityType<DragonCoreBlockEntity> DRAGON_CORE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, makeId("dragon_core"),
            new BlockEntityType<>(DragonCoreBlockEntity::new, Collections.singleton(DMBlocks.DRAGON_CORE), null)
    );
    public static final BlockEntityType<DragonHeadBlockEntity> DRAGON_HEAD;

    static {
        Block[] blocks = new Block[DragonVariants.BUILTIN_VALUES.length << 1];
        int i = 0;
        for (DragonVariant variant : DragonVariants.BUILTIN_VALUES) {
            blocks[i++] = variant.headBlock;
            blocks[i++] = variant.headWallBlock;
        }
        DRAGON_HEAD = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, makeId("dragon_head"), new BlockEntityType<>(DragonHeadBlockEntity::new, Set.of(blocks), null));
    }

    public static void init() {}
}
