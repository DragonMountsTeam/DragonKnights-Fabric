package net.dragonmounts.util;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.dragonmounts.api.IDragonFood;
import net.dragonmounts.entity.dragon.DragonLifeStage;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;

import static net.dragonmounts.util.EntityUtil.consume;

public class DragonFood implements IDragonFood {
    private static final Reference2ObjectOpenHashMap<Item, IDragonFood> REGISTRY = new Reference2ObjectOpenHashMap<>(32);
    public static final DragonFood RAW_MEAT = new DragonFood(1500, 2);
    public static final DragonFood COOKED_MEAT = new DragonFood(2500, 3);

    public static final IDragonFood HONEY_BOTTLE = (dragon, player, stack, hand) -> {
        boolean isOwner = dragon.isOwner(player);
        if (dragon.isAgeLocked()) {
            if (isOwner) {
                dragon.setAgeLocked(false);
            }
        } else {
            dragon.growUp(100, true);
        }
        dragon.setHealth(dragon.getHealth() + 1);
        if (!player.abilities.creativeMode) {
            consume(player, hand, stack, new ItemStack(Items.GLASS_BOTTLE));
        }
        if (isOwner) {
            if (dragon.getLifeStage() == DragonLifeStage.ADULT && dragon.canEat()) {
                dragon.lovePlayer(player);
            }
        } else if (dragon.getRandom().nextFloat() < 0.25) {
            dragon.setOwner(player);
        }
    };

    public static final IDragonFood POISONOUS_POTATO = new IDragonFood() {
        @Override
        public boolean isEatable(TameableDragonEntity dragon, PlayerEntity player, ItemStack stack, Hand hand) {
            return dragon.isOwner(player);
        }

        @Override
        public void eat(TameableDragonEntity dragon, PlayerEntity player, ItemStack stack, Hand hand) {
            if (!dragon.isAgeLocked()) {
                dragon.setAgeLocked(true);
            }
            if (!player.abilities.creativeMode) {
                stack.decrement(1);
            }
        }
    };

    static {
        bind(Items.COD, RAW_MEAT);
        bind(Items.SALMON, RAW_MEAT);
        bind(Items.TROPICAL_FISH, RAW_MEAT);
        bind(Items.COD_BUCKET, RAW_MEAT);
        bind(Items.SALMON_BUCKET, RAW_MEAT);
        bind(Items.TROPICAL_FISH_BUCKET, RAW_MEAT);
        bind(Items.CHICKEN, RAW_MEAT);
        bind(Items.BEEF, RAW_MEAT);
        bind(Items.MUTTON, RAW_MEAT);
        bind(Items.RABBIT, RAW_MEAT);
        bind(Items.PORKCHOP, RAW_MEAT);
        bind(Items.COOKED_SALMON, COOKED_MEAT);
        bind(Items.COOKED_COD, COOKED_MEAT);
        bind(Items.COOKED_CHICKEN, COOKED_MEAT);
        bind(Items.COOKED_BEEF, COOKED_MEAT);
        bind(Items.COOKED_MUTTON, COOKED_MEAT);
        bind(Items.RABBIT_STEW, COOKED_MEAT);
        bind(Items.COOKED_RABBIT, COOKED_MEAT);
        bind(Items.COOKED_PORKCHOP, COOKED_MEAT);
        bind(Items.HONEY_BOTTLE, HONEY_BOTTLE);
        bind(Items.POISONOUS_POTATO, POISONOUS_POTATO);
    }

    public static void bind(Item item, IDragonFood food) {
        if (REGISTRY.containsKey(item)) throw new IllegalArgumentException();
        REGISTRY.put(item, food);
    }

    public static IDragonFood get(Item item) {
        return item instanceof IDragonFood ? (IDragonFood) item : REGISTRY.getOrDefault(item, UNKNOWN);
    }

    public static boolean test(Item item) {
        return item instanceof IDragonFood || REGISTRY.containsKey(item);
    }

    public final int age;
    public final int health;

    public DragonFood(int age, int health) {
        this.age = age;
        this.health = health;
    }

    @Override
    public void eat(TameableDragonEntity dragon, PlayerEntity player, ItemStack stack, Hand hand) {
        Item item = stack.getItem();
        if (this.age != 0) dragon.growUp(this.age, true);
        if (this.health != 0) dragon.setHealth(dragon.getHealth() + this.health);
        if (!player.abilities.creativeMode) {
            Item result = null;
            if (item instanceof MushroomStewItem) {
                result = Items.BOWL;
            } else if (item instanceof BucketItem) {
                result = Items.BUCKET;
            }
            consume(player, hand, stack, result == null ? null : new ItemStack(result));
        }
        player.incrementStat(Stats.USED.getOrCreateStat(item));
        if (dragon.isOwner(player)) {
            if (dragon.getLifeStage() == DragonLifeStage.ADULT && dragon.canEat()) {
                dragon.lovePlayer(player);
            }
        } else if (dragon.getRandom().nextFloat() < 0.25) {
            dragon.setOwner(player);
        }
    }
}
