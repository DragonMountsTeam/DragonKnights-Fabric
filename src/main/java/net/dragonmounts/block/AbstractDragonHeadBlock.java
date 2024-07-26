package net.dragonmounts.block;

import net.dragonmounts.api.IDragonTypified;
import net.dragonmounts.block.entity.DragonHeadBlockEntity;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.registry.DragonVariant;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Wearable;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.dragonmounts.DragonMounts.BLOCK_TRANSLATION_KEY_PREFIX;

public abstract class AbstractDragonHeadBlock extends BlockWithEntity implements Wearable, IDragonTypified {
    public static final String TRANSLATION_KEY = BLOCK_TRANSLATION_KEY_PREFIX + "dragon_head";
    public final DragonVariant variant;
    public final boolean isOnWall;

    public AbstractDragonHeadBlock(DragonVariant variant, Settings settings, boolean isOnWall) {
        super(settings);
        this.variant = variant;
        this.isOnWall = isOnWall;
    }

    public abstract float getYRotation(BlockState state);

    @SuppressWarnings("deprecation")
    public boolean canPathfindThrough(BlockState state, BlockView level, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public DragonHeadBlockEntity createBlockEntity(BlockView level) {
        return new DragonHeadBlockEntity();
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView level, List<Text> tooltips, TooltipContext flag) {
        tooltips.add(this.variant.type.getName());
    }

    @Override
    public String getTranslationKey() {
        return TRANSLATION_KEY;
    }

    @Override
    public DragonType getDragonType() {
        return this.variant.type;
    }
}
