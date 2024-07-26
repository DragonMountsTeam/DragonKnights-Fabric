package net.dragonmounts.util;

import com.google.gson.JsonObject;
import net.dragonmounts.api.IDragonTypified;
import net.dragonmounts.registry.DragonType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.registry.Registry;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

public class DragonTypifiedItemSupplier<T extends Item> implements Supplier<T>, Ingredient.Entry, IDragonTypified {
    public final DragonType type;
    protected final Class<T> clazz;

    public DragonTypifiedItemSupplier(DragonType type, Class<T> clazz) {
        this.type = type;
        this.clazz = clazz;
    }

    @Override
    public T get() {
        return this.type.getInstance(this.clazz, null);
    }

    public Collection<ItemStack> getStacks() {
        return Collections.singleton(new ItemStack(this.get()));
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", Registry.ITEM.getId(this.get()).toString());
        return obj;
    }

    @Override
    public final DragonType getDragonType() {
        return this.type;
    }
}
