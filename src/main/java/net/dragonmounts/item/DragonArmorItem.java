package net.dragonmounts.item;

import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR;

/**
 * @see net.minecraft.item.HorseArmorItem
 */
public class DragonArmorItem extends Item {
    public static final String TEXTURE_PREFIX = "textures/models/dragon_armor/";
    public static final UUID MODIFIER_UUID = UUID.fromString("f4dbd212-cf15-57e9-977c-0019cc5a8933");
    private final Identifier texture;
    private final int protection;

    public DragonArmorItem(Identifier texture, int protection, Settings settings) {
        super(settings);
        this.protection = protection;
        this.texture = texture;
    }

    public int getProtection() {
        return this.protection;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> tooltips, TooltipContext flag) {
        tooltips.add(LiteralText.EMPTY);
        tooltips.add((new TranslatableText("item.modifiers.equipped").formatted(Formatting.GRAY)));
        tooltips.add((new TranslatableText("attribute.modifier.plus.0", this.protection, new TranslatableText(GENERIC_ARMOR.getTranslationKey()))).formatted(Formatting.BLUE));
    }

    public Identifier getDragonArmorTexture(ItemStack stack, TameableDragonEntity dragon) {
        return this.texture;
    }
}
