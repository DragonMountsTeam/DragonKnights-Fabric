package net.dragonmounts.util.math;

public class MathUtil {
    public static final float PI = (float) Math.PI;
    public static final float TO_RAD_FACTOR = PI / 180F;
    public static final float HALF_RAD_FACTOR = TO_RAD_FACTOR / 2F;

    public static float getColor(int color, int area) {
        return (color >> (area * 8) & 0xFF) / 255F;
    }
}
