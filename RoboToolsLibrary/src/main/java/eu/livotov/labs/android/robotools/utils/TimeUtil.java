package eu.livotov.labs.android.robotools.utils;

import java.util.Calendar;

/**
 * Created by dlivotov on 22/10/2016.
 */

public class TimeUtil
{
    public static boolean isToday(long millis)
    {
        Calendar calendar = Calendar.getInstance();
        final int today = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.setTimeInMillis(millis);
        return today == calendar.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isTomorrow(long millis)
    {
        Calendar calendar = Calendar.getInstance();
        final int tomorrow = calendar.get(Calendar.DAY_OF_YEAR) + 1;
        calendar.setTimeInMillis(millis);
        return tomorrow == calendar.get(Calendar.DAY_OF_YEAR);
    }

    public static long roundTimeToNearestNextMinutes(long millis, int round)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % round;
        calendar.add(Calendar.MINUTE, mod > 0 ? round - mod : 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static long roundTimeToNearestMinutes(long millis, int round)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % round;
        calendar.add(Calendar.MINUTE, (mod < round / 2) ? -mod : (round - mod));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }
}
