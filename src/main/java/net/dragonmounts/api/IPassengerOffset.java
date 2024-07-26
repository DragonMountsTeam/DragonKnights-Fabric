package net.dragonmounts.api;

import net.minecraft.util.math.Vec3d;

@FunctionalInterface
public interface IPassengerOffset {
    IPassengerOffset DEFAULT = (index, sitting) -> {
        double yOffset = sitting ? 3.4 : 4.4;
        double yOffset2 = sitting ? 2.1 : 2.5; // maybe not needed
        // dragon position is the middle of the model, and the saddle is on
        // the shoulders, so move player forwards on Z axis relative to the
        // dragon's rotation to fix that
        switch (index) {
            case 1:
                return new Vec3d(0.6, yOffset, 0.1);
            case 2:
                return new Vec3d(-0.6, yOffset, 0.1);
            case 3:
                return new Vec3d(1.6, yOffset2, 0.2);
            case 4:
                return new Vec3d(-1.6, yOffset2, 0.2);
            default:
                return new Vec3d(0, yOffset, 2.2);
        }
    };

    Vec3d get(int index, boolean sitting);
}
