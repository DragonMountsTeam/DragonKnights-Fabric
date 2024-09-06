package net.dragonmounts.entity.dragon;

import net.minecraft.world.entity.AgeableMob;

public class DragonSpawnData extends AgeableMob.AgeableMobGroupData {
    public final DragonLifeStage stage;

    public DragonSpawnData(DragonLifeStage stage) {
        super(false);
        this.stage = stage;
    }

    public final boolean isShouldSpawnBaby() {
        return false;
    }

    public final float getBabySpawnChance() {
        return 0.0F;
    }
}
