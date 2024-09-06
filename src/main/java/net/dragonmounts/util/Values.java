package net.dragonmounts.util;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class Values<T> implements Iterable<T> {
    final T[] values;
    public final int length;

    @SuppressWarnings("unchecked")
    public Values(Class<?> holder, Class<T> target) {
        Field[] fields = holder.getDeclaredFields();
        Object[] values = new Object[fields.length];
        int index = 0;
        for (var field : fields) {
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

    public Stream<T> stream() {
        return Arrays.stream(this.values);
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

    public final static class LazyIterator<T> {
        @SuppressWarnings("rawtypes")
        final Reference2ObjectOpenHashMap<Class, Consumer> consumers = new Reference2ObjectOpenHashMap<>();
        final Field[] fields;

        public LazyIterator(Class<?> holder) {
            this.fields = holder.getDeclaredFields();
        }

        public <E> LazyIterator<T> bind(Class<E> clazz, Consumer<E> consumer) {
            this.consumers.put(clazz, consumer);
            return this;
        }

        @SuppressWarnings("unchecked")
        public void apply() {
            Field[] fields = this.fields;
            for (int i = 0, n = fields.length; i < n; ++i) {
                var field = fields[i++];
                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                    var consumer = this.consumers.get(field.getType());
                    if (consumer == null) continue;
                    try {
                        var temp = field.get(null);
                        if (temp != null) {
                            consumer.accept(temp);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }
}
