package net.dragonmounts.init;

import net.dragonmounts.registry.CarriageType;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Items;

import static net.dragonmounts.DragonMounts.makeId;

public class CarriageTypes {
    // DMItems.xxx 还未初始化，不能使用 DMItems.xxx::getItem()
    public static final CarriageType OAK = new CarriageType.Default(() -> Items.OAK_BOAT, makeId("textures/entity/dragon_carriage/carriage_oak.png"));

    static {
        Registry.register(CarriageType.REGISTRY, CarriageType.DEFAULT_KEY, OAK);
    }

    /*

    public static final CarriageType OAK = new CarriageType.Default(() -> DMItems.OAK_CARRIAGE, makeId("textures/entity/dragon_carriage/carriage_oak.png")).setRegistryName(CarriageType.DEFAULT_KEY);
    public static final CarriageType SPRUCE = new CarriageType.Default(() -> DMItems.SPRUCE_CARRIAGE, makeId("textures/entity/dragon_carriage/carriage_spruce.png")).setRegistryName(MOD_ID, "spruce");
    public static final CarriageType BIRCH = new CarriageType.Default(() -> DMItems.BIRCH_CARRIAGE, makeId("textures/entity/dragon_carriage/carriage_birch.png")).setRegistryName(MOD_ID, "birch");
    public static final CarriageType JUNGLE = new CarriageType.Default(() -> DMItems.JUNGLE_CARRIAGE, makeId("textures/entity/dragon_carriage/carriage_jungle.png")).setRegistryName(MOD_ID, "jungle");
    public static final CarriageType ACACIA = new CarriageType.Default(() -> DMItems.ACACIA_CARRIAGE, makeId("textures/entity/dragon_carriage/carriage_acacia.png")).setRegistryName(MOD_ID, "acacia");
    public static final CarriageType DARK_OAK = new CarriageType.Default(() -> DMItems.DARK_OAK_CARRIAGE, makeId("textures/entity/dragon_carriage/carriage_dark_oak.png")).setRegistryName(MOD_ID, "dark_oak");
*/
    /* upcoming types:
    public static final CarriageType MANGROVE = new CarriageType(Blocks.CRIMSON_PLANKS, DMItems.MANGROVE_CARRIAGE);
    public static final CarriageType CHERRY = new CarriageType(Blocks.CRIMSON_PLANKS, DMItems.CHERRY_CARRIAGE);
    public static final CarriageType BAMBOO = new CarriageType(Blocks.CRIMSON_PLANKS, DMItems.BAMBOO_CARRIAGE);
    public static final CarriageType CRIMSON = new CarriageType(Blocks.CRIMSON_PLANKS, DMItems.CRIMSON_CARRIAGE);
    public static final CarriageType WARPED = new CarriageType(Blocks.WARPED_PLANKS, DMItems.WARPED_CARRIAGE);*/
    public static void init() {
        /*IForgeRegistry<CarriageType> registry = event.getRegistry();
        Values.LazyIterator<CarriageType> iterator = new Values.LazyIterator<>(CarriageTypes.class, CarriageType.class);
        while (iterator.hasNext()) {
            registry.register(iterator.next());
        }*/
    }
}

