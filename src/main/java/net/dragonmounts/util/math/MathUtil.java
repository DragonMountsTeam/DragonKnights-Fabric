package net.dragonmounts.util.math;

import org.joml.Vector3f;

public class MathUtil {
    public static final float PI = (float) Math.PI;
    public static final float TO_RAD_FACTOR = PI / 180F;
    public static final float HALF_RAD_FACTOR = TO_RAD_FACTOR / 2F;

    public static Vector3f getColorVector(int color) {
        return new Vector3f(
                (color >> 16 & 255) / 255.0F,
                (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F
        );
    }
}
