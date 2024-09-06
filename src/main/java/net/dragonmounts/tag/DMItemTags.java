package net.dragonmounts.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static net.dragonmounts.DragonMounts.makeId;

public class DMItemTags {
    public static final TagKey<Item> DRAGON_EGGS = create("dragon_eggs");
    public static final TagKey<Item> DRAGON_SCALE_BOWS = create("dragon_scale_bows");
    public static final TagKey<Item> DRAGON_SCALES = create("dragon_scales");
    public static final TagKey<Item> DRAGON_INEDIBLE = create("dragon_inedible");
    public static final TagKey<Item> COOKED_DRAGON_FOOD = create("dragon_food/cooked");
    public static final TagKey<Item> RAW_DRAGON_FOOD = create("dragon_food/raw");
    public static final TagKey<Item> BATONS = create("batons");
    public static final TagKey<Item> HARD_SHEARS = create("hard_shears");

    static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, makeId(name));
    }
}
