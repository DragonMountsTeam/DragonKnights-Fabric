package net.dragonmounts.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dragonmounts.api.ScoreboardAccessor;
import net.dragonmounts.init.DMDataComponents;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ScoreboardInfo(@Nullable String team, @NotNull List<Entry> scores) {
    public static final Codec<String> TEAM_CODEC = Codec.STRING.lenientOptionalFieldOf("Team", null).codec();
    public static final Codec<List<Entry>> SCORE_CODEC = RecordCodecBuilder.<Entry>create(
            instance -> instance.group(
                    Codec.STRING.fieldOf("Name").forGetter(Entry::name),
                    Codec.INT.fieldOf("Score").forGetter(Entry::value),
                    Codec.BOOL.lenientOptionalFieldOf("Locked", true).forGetter(Entry::locked),
                    ComponentSerialization.CODEC.lenientOptionalFieldOf("display").forGetter(Entry::display),
                    NumberFormatTypes.CODEC.lenientOptionalFieldOf("format").forGetter(Entry::format)
            ).apply(instance, Entry::new)
    ).listOf().fieldOf("Scores").codec();
    public static final ScoreboardCodec CODEC = new ScoreboardCodec();

    public static void applyScores(Scoreboard scoreboard, DataComponentHolder components, ScoreHolder holder) {
        var scores = components.get(DMDataComponents.SCORES);
        if (scores != null) {
            scores.apply((ScoreboardAccessor) scoreboard, holder);
        }
    }

    public void apply(ScoreboardAccessor accessor, ScoreHolder holder) {
        if (this.team != null) {
            accessor.dragonmounts$addPlayerToTeam(holder.getScoreboardName(), this.team);
        }
        accessor.dragonmounts$loadEntries(holder, this.scores);
    }

    public record Entry(
            String name,
            int value,
            boolean locked,
            Optional<Component> display,
            Optional<NumberFormat> format
    ) {
        public Entry(String name, Score score) {
            this(name, score.value(), score.isLocked(), Optional.ofNullable(score.display()), Optional.ofNullable(score.numberFormat()));
        }

        public Score apply(Score score) {
            score.value(this.value);
            score.setLocked(this.locked);
            score.display(this.display.orElse(null));
            score.numberFormat(this.format.orElse(null));
            return score;
        }
    }

    public static List<Entry> takeSnapshot(Map<Objective, Score> scores) {
        var list = new ObjectArrayList<Entry>(scores.size());
        for (var entry : scores.entrySet()) {
            list.add(new Entry(entry.getKey().getName(), entry.getValue()));
        }
        return list;
    }

    /**
     * @see com.mojang.serialization.codecs.PairCodec
     */
    public static final class ScoreboardCodec implements Codec<ScoreboardInfo> {
        @Override
        public <T> DataResult<Pair<ScoreboardInfo, T>> decode(final DynamicOps<T> ops, final T input) {
            return TEAM_CODEC.decode(ops, input).flatMap(pair1 -> SCORE_CODEC.decode(ops, pair1.getSecond()).map(pair2 -> Pair.of(new ScoreboardInfo(pair1.getFirst(), pair2.getFirst()), pair2.getSecond())));
        }

        @Override
        public <T> DataResult<T> encode(ScoreboardInfo value, DynamicOps<T> ops, T prefix) {
            return SCORE_CODEC.encode(value.scores, ops, prefix).flatMap(rest -> TEAM_CODEC.encode(value.team, ops, rest));
        }
    }
}
