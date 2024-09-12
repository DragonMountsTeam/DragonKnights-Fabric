package net.dragonmounts.init;

import net.dragonmounts.block.AbstractDragonHeadBlock;
import net.dragonmounts.block.entity.DragonCoreBlockEntity;
import net.dragonmounts.block.entity.DragonHeadBlockEntity;
import net.dragonmounts.registry.DragonVariant;
import net.dragonmounts.util.Values;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static net.dragonmounts.DragonMounts.MOD_ID;

public class DMBlockEntities {
    public static final BlockEntityType<DragonCoreBlockEntity> DRAGON_CORE = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "dragon_core"), BlockEntityType.Builder.create(DragonCoreBlockEntity::new, DMBlocks.DRAGON_CORE).build(null));
    public static final BlockEntityType<DragonHeadBlockEntity> DRAGON_HEAD;

    static {
        Values<DragonVariant> variants = DragonVariants.BUILTIN_VALUES;
        AbstractDragonHeadBlock[] blocks = new AbstractDragonHeadBlock[variants.length << 1];
        int i = 0;
        for (DragonVariant variant : variants) {
            blocks[i++] = variant.headBlock;
            blocks[i++] = variant.headWallBlock;
        }
        DRAGON_HEAD = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "dragon_head"), BlockEntityType.Builder.create(DragonHeadBlockEntity::new, blocks).build(null));
    }
}
