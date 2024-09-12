package net.dragonmounts.util;

public class TimeUtil {
    public static final int RATE = 72;
    public static final int SECOND_PRE_MINUTE = 60;
    public static final int MINUTE_PRE_HOUR = 60;
    public static final int SECOND_PRE_HOUR = SECOND_PRE_MINUTE * MINUTE_PRE_HOUR;
    public static final int TICKS_PER_REAL_SECOND = 20;
    public static final int TICKS_PER_GAME_HOUR = TICKS_PER_REAL_SECOND * SECOND_PRE_HOUR / RATE;

    /**
     * @param value raw time (in ticks)
     * @return formatted time (in seconds)
     */
    public static String stringifyTick(int value) {
        if (value < 19) return "0." + ((value + 1) >> 1);
        StringBuilder builder = new StringBuilder().append((value + 1) >> 1);//value: ticks
        builder.append(builder.charAt(value = builder.length() - 1)).setCharAt(value, '.');//value: index
        return builder.toString();
    }
}
