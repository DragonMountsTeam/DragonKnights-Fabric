package net.dragonmounts.util;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.dragonmounts.api.IDragonFood;
import net.dragonmounts.entity.dragon.DragonLifeStage;
import net.dragonmounts.entity.dragon.TameableDragonEntity;
import net.dragonmounts.tag.DMItemTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

import static net.dragonmounts.util.EntityUtil.consumeStack;

public record DragonFood(int age, int health) implements IDragonFood {
    private static final Reference2ObjectOpenHashMap<Item, IDragonFood> REGISTRY = new Reference2ObjectOpenHashMap<>(32);
    public static final DragonFood RAW_MEAT = new DragonFood(1500, 2);
    public static final DragonFood COOKED_MEAT = new DragonFood(2500, 3);

    public static final IDragonFood HONEY_BOTTLE = (dragon, player, stack, hand) -> {
        boolean isOwner = dragon.isOwnedBy(player);
        if (dragon.isAgeLocked()) {
            if (isOwner) {
                dragon.setAgeLocked(false);
            }
        } else {
            dragon.ageUp(100, true);
        }
        dragon.setHealth(dragon.getHealth() + 1);
        if (!player.getAbilities().instabuild) {
            consumeStack(player, hand, stack, new ItemStack(Items.GLASS_BOTTLE));
        }
        if (isOwner) {
            if (dragon.getLifeStage() == DragonLifeStage.ADULT && dragon.canFallInLove()) {
                dragon.setInLove(player);
            }
        } else if (dragon.getRandom().nextFloat() < 0.25) {
            dragon.tame(player);
        }
    };

    public static final IDragonFood POISONOUS_POTATO = new IDragonFood() {
        @Override
        public boolean canFeed(TameableDragonEntity dragon, Player player, ItemStack stack, InteractionHand hand) {
            return dragon.isOwnedBy(player);
        }

        @Override
        public void feed(TameableDragonEntity dragon, Player player, ItemStack stack, InteractionHand hand) {
            if (!dragon.isAgeLocked()) {
                dragon.setAgeLocked(true);
            }
            stack.consume(1, player);
        }
    };

    static {
        bind(Items.HONEY_BOTTLE, HONEY_BOTTLE);
        bind(Items.POISONOUS_POTATO, POISONOUS_POTATO);
    }

    public static void bind(Item item, IDragonFood food) {
        if (REGISTRY.containsKey(item)) throw new IllegalArgumentException();
        REGISTRY.put(item, food);
    }

    @SuppressWarnings("deprecation")
    public static IDragonFood get(Item item) {
        if (item instanceof IDragonFood) return (IDragonFood) item;
        var food = REGISTRY.get(item);
        if (food != null) return food;
        var holder = item.builtInRegistryHolder();
        if (holder.is(ConventionalItemTags.FOOD_POISONING_FOODS)) return IDragonFood.UNKNOWN;
        if (holder.is(DMItemTags.COOKED_DRAGON_FOOD)) return COOKED_MEAT;
        if (holder.is(DMItemTags.RAW_DRAGON_FOOD)) return RAW_MEAT;
        return IDragonFood.UNKNOWN;
    }

    @SuppressWarnings("deprecation")
    public static boolean test(Item item) {
        if (item instanceof IDragonFood || REGISTRY.containsKey(item)) return true;
        var holder = item.builtInRegistryHolder();
        return !holder.is(ConventionalItemTags.FOOD_POISONING_FOODS) && (
                holder.is(DMItemTags.COOKED_DRAGON_FOOD) || holder.is(DMItemTags.RAW_DRAGON_FOOD)
        );
    }

    @Override
    public void feed(TameableDragonEntity dragon, Player player, ItemStack stack, InteractionHand hand) {
        var item = stack.getItem();
        if (this.age != 0) {
            dragon.ageUp(this.age, true);
        }
        if (this.health != 0) {
            dragon.setHealth(dragon.getHealth() + this.health);
        }
        if (!player.getAbilities().instabuild) {
            Item result = null;
            if (item instanceof SuspiciousStewItem) {
                result = Items.BOWL;
            } else if (item instanceof BucketItem) {
                result = Items.BUCKET;
            }
            consumeStack(player, hand, stack, result == null ? ItemStack.EMPTY : new ItemStack(result));
        }
        player.awardStat(Stats.ITEM_USED.get(item));
        if (dragon.isOwnedBy(player)) {
            if (dragon.getLifeStage() == DragonLifeStage.ADULT && dragon.canFallInLove()) {
                dragon.setInLove(player);
            }
        } else if (dragon.getRandom().nextFloat() < 0.25F) {
            dragon.tame(player);
        }
    }
}
