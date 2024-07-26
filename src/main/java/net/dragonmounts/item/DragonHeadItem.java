package net.dragonmounts.item;

import net.dragonmounts.api.IDragonTypified;
import net.dragonmounts.block.AbstractDragonHeadBlock;
import net.dragonmounts.registry.DragonType;
import net.dragonmounts.registry.DragonVariant;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WallStandingBlockItem;
import net.minecraft.item.Wearable;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DragonHeadItem extends WallStandingBlockItem implements Wearable, IDragonTypified {
    public final static EquipmentSlotProvider WEARABLE_HEAD = $ -> EquipmentSlot.HEAD;
    public final DragonVariant variant;

    public DragonHeadItem(DragonVariant variant, Block head, Block headWall, FabricItemSettings settings) {
        super(head, headWall, settings.equipmentSlot(WEARABLE_HEAD));
        this.variant = variant;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> tooltips, TooltipContext flag) {
        tooltips.add(this.variant.type.getName());
    }

    @Override
    public String getTranslationKey() {
        return AbstractDragonHeadBlock.TRANSLATION_KEY;
    }

    @Override
    public DragonType getDragonType() {
        return this.variant.type;
    }
}
