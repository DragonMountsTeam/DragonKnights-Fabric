package net.dragonmounts.tag;


import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static net.dragonmounts.DragonMounts.makeId;

public class DMBlockTags {
    public static final TagKey<Block> DRAGON_EGGS = TagKey.create(Registries.BLOCK, makeId("dragon_eggs"));
}
