package net.dragonmounts.item;

import net.dragonmounts.api.DragonTypified;
import net.dragonmounts.block.AbstractDragonHeadBlock;
import net.dragonmounts.init.DMDataComponents;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.registry.DragonVariant;
import net.minecraft.core.Direction;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class DragonHeadItem extends StandingAndWallBlockItem implements DragonTypified {
    public final DragonVariant variant;

    public DragonHeadItem(DragonVariant variant, Block head, Block wall, Properties props) {
        super(head, wall, props.component(DMDataComponents.DRAGON_TYPE, variant.type), Direction.DOWN);
        this.variant = variant;
    }

    @Override
    public @NotNull String getDescriptionId() {
        return AbstractDragonHeadBlock.TRANSLATION_KEY;
    }

    @Override
    public DragonType getDragonType() {
        return this.variant.type;
    }
}
