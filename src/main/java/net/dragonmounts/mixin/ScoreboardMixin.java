package net.dragonmounts.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dragonmounts.api.ScoreboardAccessor;
import net.dragonmounts.util.ScoreboardInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.*;
import org.apache.commons.lang3.function.Consumers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin implements ScoreboardAccessor {
    @Unique
    private final ObjectArrayList<ScoreHolder> dragonmounts$reserved = new ObjectArrayList<>();
    @Shadow
    @Final
    private Map<String, PlayerScores> playerScores;

    @Shadow
    @Final
    private Object2ObjectMap<String, PlayerTeam> teamsByPlayer;

    @Shadow
    @Final
    private Object2ObjectMap<String, Objective> objectivesByName;

    @Shadow
    public abstract boolean addPlayerToTeam(String playerName, PlayerTeam team);

    @Shadow
    protected abstract PlayerScores getOrCreatePlayerInfo(String username);

    @Shadow
    protected abstract void onScoreChanged(ScoreHolder scoreHolder, Objective objective, Score score);

    @Override
    public void dragonmounts$preventRemoval(ScoreHolder holder) {
        if (!this.dragonmounts$reserved.contains(holder)) {
            this.dragonmounts$reserved.add(holder);
        }
    }

    @Override
    public ScoreboardInfo dragonmounts$getInfo(ScoreHolder holder) {
        var name = holder.getScoreboardName();
        var team = this.teamsByPlayer.get(name);
        var scores = this.playerScores.get(name);
        return new ScoreboardInfo(
                team == null ? null : team.getName(),
                scores == null ? Collections.emptyList() : ScoreboardInfo.takeSnapshot(scores.listRawScores())
        );
    }

    @Override
    public void dragonmounts$addPlayerToTeam(String name, String team) {
        var $team = this.teamsByPlayer.get(team);
        if ($team == null) return;
        this.addPlayerToTeam(name, $team);
    }

    @Override
    public void dragonmounts$loadEntries(ScoreHolder holder, List<ScoreboardInfo.Entry> entries) {
        if (entries.isEmpty()) return;
        var scores = this.getOrCreatePlayerInfo(holder.getScoreboardName());
        for (var entry : entries) {
            var objective = this.objectivesByName.get(entry.name());
            if (objective == null) continue;
            this.onScoreChanged(holder, objective, entry.apply(scores.getOrCreate(objective, Consumers.nop())));
        }
    }

    @Inject(method = "entityRemoved", at = @At("HEAD"), cancellable = true)
    public void reserveEntity(Entity entity, CallbackInfo info) {
        int index = this.dragonmounts$reserved.indexOf(entity);
        if (index == -1) return;
        this.dragonmounts$reserved.remove(index);
        info.cancel();
    }

    private ScoreboardMixin() {}
}
