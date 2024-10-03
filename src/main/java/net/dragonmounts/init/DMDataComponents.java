package net.dragonmounts.init;

import net.dragonmounts.registry.DragonType;
import net.dragonmounts.util.FluteSound;
import net.dragonmounts.util.ScoreboardInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.UUID;
import java.util.function.Function;

import static net.dragonmounts.DragonMounts.makeId;

public class DMDataComponents {
    public static final DataComponentType<DragonType> DRAGON_TYPE = register("dragon_type", new DataComponentType.Builder<DragonType>().persistent(DragonType.CODEC).networkSynchronized(DragonType.STREAM_CODEC));
    public static final DataComponentType<Component> PLAYER_NAME = register("player_name", new DataComponentType.Builder<Component>().persistent(ComponentSerialization.FLAT_CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC).cacheEncoding());
    public static final DataComponentType<ScoreboardInfo> SCORES = register("scores", new DataComponentType.Builder<ScoreboardInfo>().persistent(ScoreboardInfo.CODEC));
    public static final DataComponentType<FluteSound> FLUTE_SOUND;

    static {
        Function<FluteSound, UUID> encode = sound -> sound.uuid;
        Function<UUID, FluteSound> decode = FluteSound::getOrCreate;
        FLUTE_SOUND = register("flute_sound", new DataComponentType.Builder<FluteSound>().persistent(
                UUIDUtil.CODEC.xmap(decode, encode)
        ).networkSynchronized(
                UUIDUtil.STREAM_CODEC.map(decode, encode)
        ));
    }

    static <T> DataComponentType<T> register(String name, DataComponentType.Builder<T> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, makeId(name), builder.build());
    }
}
