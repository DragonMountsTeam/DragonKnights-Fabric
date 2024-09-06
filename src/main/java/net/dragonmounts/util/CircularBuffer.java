package net.dragonmounts.util;

import java.util.Arrays;

import static net.dragonmounts.util.math.Interpolation.clampedSmoothLinear;

/**
 * Very simple fixed size circular buffer implementation for animation purposes.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CircularBuffer {

    private final float[] buffer;
    private int index = 0;

    public CircularBuffer(int size) {
        this.buffer = new float[size];
    }

    public void fill(float value) {
        Arrays.fill(this.buffer, value);
    }

    public void update(float value) {
        // move forward
        this.index++;
        // restart pointer at the end to form a virtual ring
        this.index %= this.buffer.length;
        this.buffer[this.index] = value;
    }

    public float get(float x, int offset) {
        int i = this.index - offset;
        int len = this.buffer.length - 1;
        return clampedSmoothLinear(this.buffer[i - 1 & len], this.buffer[i & len], x);
    }

    public float get(float x, int offset1, int offset2) {
        return this.get(x, offset2) - this.get(x, offset1);
    }
}