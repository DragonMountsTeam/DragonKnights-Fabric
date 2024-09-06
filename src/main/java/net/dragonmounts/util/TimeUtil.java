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
    public static String formatAsFloat(int value) {
        value = (value + 1) >> 1;
        if (value < 10) {
            return "0." + value;
        }
        var builder = new StringBuilder().append(value);
        value = builder.length() - 1;
        builder.append(builder.charAt(value)).setCharAt(value, '.');
        return builder.toString();
    }
}
