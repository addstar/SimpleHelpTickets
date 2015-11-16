package me.odium.simplehelptickets;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Chinwe
 */
public class TimeParser
{
    private static final Pattern TIME_PATTERN = Pattern.compile("([0-9]+)([wdhms])");
    private static final int SECOND = 1;
    private static final int MINUTE = SECOND * 60;
    private static final int HOUR = MINUTE * 60;
    private static final int DAY = HOUR * 24;
    private static final int WEEK = DAY * 7;

    /**
     * Parse a string input into seconds, using w(eeks), d(ays), h(ours), m(inutes) and s(econds) For example: 4d8m2s -> 4 days, 8 minutes and 2 seconds
     *
     * @param string String to convert to Seconds
     * @return Seconds
     */
    public static int parseStringToSecs(String string)
    {
        Matcher m = TIME_PATTERN.matcher(string);
        int total = 0;
        while (m.find()) {
            int amount = Integer.parseInt(m.group(1));
            switch (m.group(2).charAt(0)) {
                case 's':
                    total += amount * SECOND;
                    break;
                case 'm':
                    total += amount * MINUTE;
                    break;
                case 'h':
                    total += amount * HOUR;
                    break;
                case 'd':
                    total += amount * DAY;
                    break;
                case 'w':
                    total += amount * WEEK;
                    break;
                default:
                    total = 0;
                    break;
            }
        }
        return total;
    }

    public static long parseStringtoMillisec(String string) {
        long total = parseStringToSecs(string);
        return total * 1000;
    }
}
