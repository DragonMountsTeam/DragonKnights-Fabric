package net.dragonmounts.util.math;


import static net.minecraft.util.Mth.clamp;

/**
 * Simple class to store and limitate a float value that is smoothed between its
 * current and previous tick value.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LinearInterpolation {
    protected float current;
    protected float previous;

    public LinearInterpolation(float start) {
        this.current = this.previous = start;
    }

    public float get() {
        return this.current;
    }

    public float get(float delta) {
        if (delta <= 0.0F) return this.previous;
        if (delta >= 1.0F) return this.current;
        return this.previous + delta * (this.current - this.previous);
    }

    public void set(float value) {
        sync();
        this.current = value;
    }

    public void add(float value) {
        this.set(this.current + value);
    }

    public void sync() {
        this.previous = this.current;
    }

    public static class Clamped extends LinearInterpolation {
        public final float min;
        public final float max;

        public Clamped(float start, float min, float max) {
            super(clamp(start, min, max));
            this.min = min;
            this.max = max;
        }

        @Override
        public void set(float value) {
            super.set(clamp(value, this.min, this.max));
        }
    }
}
