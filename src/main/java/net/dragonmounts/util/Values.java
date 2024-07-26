package net.dragonmounts.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

public class Values<T> implements Iterable<T> {
    final T[] values;
    public final int length;

    @SuppressWarnings("unchecked")
    public Values(Class<?> holder, Class<T> target) {
        Field[] fields = holder.getDeclaredFields();
        Object[] values = new Object[fields.length];
        int index = 0;
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && target.isAssignableFrom(field.getType())) {
                try {
                    Object temp = field.get(null);
                    if (temp != null) {
                        values[index++] = temp;
                    }
                } catch (Exception ignored) {}
            }
        }
        this.values = (T[]) values;
        this.length = index;
    }

    @Override
    public @NotNull IteratorImpl iterator() {
        return new IteratorImpl();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        for (int i = 0; i < this.length; ++i) {
            action.accept(this.values[i]);
        }
    }

    public final class IteratorImpl implements Iterator<T> {
        int i = 0;

        @Override
        public boolean hasNext() {
            return this.i < Values.this.length;
        }

        @Override
        public T next() {
            return Values.this.values[this.i++];
        }
    }

    public final static class LazyIterator<T> implements Iterator<T> {
        final Class<T> target;
        final Field[] fields;
        int i = 0;
        T cache;

        public LazyIterator(Class<?> holder, Class<T> target) {
            this.fields = holder.getDeclaredFields();
            this.target = target;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean hasNext() {
            Field[] fields;
            int length = (fields = this.fields).length, i;
            if (length < (i = this.i)) return false;
            Class<T> target = this.target;
            while (i < length) {
                Field field = fields[i++];
                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && target.isAssignableFrom(field.getType())) {
                    try {
                        Object temp = field.get(null);
                        if (temp != null) {
                            this.i = i;
                            this.cache = (T) temp;
                            return true;
                        }
                    } catch (Exception ignored) {}
                }
            }
            this.i = i;
            return false;
        }

        @NotNull
        @Override
        public T next() {
            return Objects.requireNonNull(this.cache);
        }
    }
}
