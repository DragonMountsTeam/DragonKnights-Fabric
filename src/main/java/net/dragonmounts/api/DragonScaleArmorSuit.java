package net.dragonmounts.api;

import net.dragonmounts.item.DragonScaleArmorItem;
import net.dragonmounts.registry.DragonType;
import net.minecraft.world.item.Item;

import static net.minecraft.world.item.ArmorItem.Type.*;

public class DragonScaleArmorSuit extends ArmorSuit<DragonScaleArmorItem> implements DragonTypified {
    public final IDragonScaleArmorEffect effect;
    public final DragonType type;

    public DragonScaleArmorSuit(DragonType type, IDragonScaleArmorEffect effect, Item.Properties props) {
        super(
                new DragonScaleArmorItem(type, HELMET, effect, props),
                new DragonScaleArmorItem(type, CHESTPLATE, effect, props),
                new DragonScaleArmorItem(type, LEGGINGS, effect, props),
                new DragonScaleArmorItem(type, BOOTS, effect, props)
        );
        this.type = type;
        this.effect = effect;
    }

    @Override
    public DragonType getDragonType() {
        return this.type;
    }
}
