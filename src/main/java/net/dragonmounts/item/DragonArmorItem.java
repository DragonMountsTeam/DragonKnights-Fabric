package net.dragonmounts.item;

import net.dragonmounts.api.DragonArmorMaterial;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;

/**
 * @see net.minecraft.world.item.AnimalArmorItem
 */
public class DragonArmorItem extends ArmorItem {
    public static final DragonArmorMaterial NETHERITE = new DragonArmorMaterial(ArmorMaterials.NETHERITE, 15);
    public static final DragonArmorMaterial DIAMOND = new DragonArmorMaterial(ArmorMaterials.DIAMOND, 11);
    public static final String TEXTURE_PREFIX = "textures/models/dragon_armor/";
    public final ResourceLocation texture;

    public DragonArmorItem(Holder<ArmorMaterial> material, ResourceLocation texture, Properties props) {
        super(material, ArmorItem.Type.BODY, props);
        this.texture = texture;
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
