package net.dragonmounts.api;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import org.apache.logging.log4j.core.util.ObjectArrayIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public class ArmorSuit<T extends ArmorItem> implements Collection<T> {
    public final T helmet;
    public final T chestplate;
    public final T leggings;
    public final T boots;

    public ArmorSuit(T helmet, T chestplate, T leggings, T boots) {
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
    }

    public final T bySlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> this.helmet;
            case CHEST -> this.chestplate;
            case LEGS -> this.leggings;
            case FEET -> this.boots;
            default -> null;
        };
    }

    @Override
    public Object @NotNull [] toArray() {
        return new Object[]{this.helmet, this.chestplate, this.leggings, this.boots};
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E @NotNull [] toArray(E @NotNull [] a) {
        return (E[]) this.toArray();// it is safe to be cast, as a result of type erasure
    }

    @Override
    public boolean add(T t) {
        return false;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new ObjectArrayIterator<>(this.helmet, this.chestplate, this.leggings, this.boots);
    }

    @Override
    public final int size() {
        return 4;
    }

    @Override
    public final boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return this.helmet == o || this.chestplate == o || this.leggings == o || this.boots == o;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (var e : c) {
            if (!this.contains(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
