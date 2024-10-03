package net.dragonmounts.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.dragonmounts.entity.dragon.ServerDragonEntity;
import net.dragonmounts.init.DMDataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;

public final class FluteSound {
    private static final Object2ObjectOpenHashMap<UUID, FluteSound> INSTANCES = new Object2ObjectOpenHashMap<>();

    public static FluteSound getOrCreate(final UUID uuid) {
        return INSTANCES.computeIfAbsent(uuid, FluteSound::new);
    }

    public static FluteSound getOrCreate(final ItemStack stack) {
        var sound = stack.get(DMDataComponents.FLUTE_SOUND);
        while (sound == null) {
            var uuid = UUID.randomUUID();
            if (!INSTANCES.containsKey(uuid)) {
                sound = new FluteSound(uuid);
                INSTANCES.put(uuid, sound);
                stack.set(DMDataComponents.FLUTE_SOUND, sound);
            }
        }
        return sound;
    }

    public final UUID uuid;
    private final ObjectOpenHashSet<ServerDragonEntity> listeners = new ObjectOpenHashSet<>();

    private FluteSound(final UUID uuid) {
        this.uuid = uuid;
    }

    public FluteSound listen(final ServerDragonEntity dragon) {
        this.listeners.add(dragon);
        return this;
    }

    public void play(final Consumer<ServerDragonEntity> command) {
        this.listeners.forEach(command);
    }
}
