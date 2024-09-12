package net.dragonmounts.api;

import net.dragonmounts.item.DragonScaleArmorItem;
import net.dragonmounts.registry.DragonType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static net.dragonmounts.init.DMItemGroups.TOOL_TAB;
import static net.minecraft.entity.EquipmentSlot.*;

public class DragonScaleArmorSuit extends ArmorSuit<DragonScaleArmorItem> implements IDragonTypified {
    public static final Function<EquipmentSlot, Item.Settings> DRAGONMOUNTS_TOOL_TAB = slot -> new Item.Settings().group(TOOL_TAB);
    public final IDragonScaleArmorEffect effect;
    public final DragonType type;
    public final String namespace;
    public final String locPrefix;

    public DragonScaleArmorSuit(DragonScaleMaterial material, IDragonScaleArmorEffect effect, Function<EquipmentSlot, Item.Settings> factory) {
        super(
                new DragonScaleArmorItem(material, HEAD, effect, factory.apply(HEAD)),
                new DragonScaleArmorItem(material, CHEST, effect, factory.apply(CHEST)),
                new DragonScaleArmorItem(material, LEGS, effect, factory.apply(LEGS)),
                new DragonScaleArmorItem(material, FEET, effect, factory.apply(FEET))
        );
        this.type = material.type;
        Identifier typeId = this.type.identifier;
        this.namespace = typeId.getNamespace();
        this.locPrefix = "textures/models/armor/" + typeId.getPath() + "_layer_";
        this.effect = effect;
    }

    public Identifier getArmorTexture(LivingEntity entity, ItemStack stack, EquipmentSlot slot, boolean upper, @Nullable String suffix, Identifier defaultTexture) {
        return new Identifier(this.namespace, this.locPrefix + (upper ? 2 : 1) + (suffix == null ? "" : "_" + suffix) + ".png");
    }

    @Override
    public DragonType getDragonType() {
        return this.type;
    }
}
