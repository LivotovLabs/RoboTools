package eu.livotov.labs.android.robotools.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by dlivotov on 22/10/2016.
 */

public class TimeUtil {

    private static final Calendar CALENDAR = Calendar.getInstance();

    public static boolean isToday(long millis) {
        Calendar calendar = Calendar.getInstance();
        final int today = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.setTimeInMillis(millis);
        return today == calendar.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isTomorrow(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        final int tomorrow = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.setTimeInMillis(millis);
        return tomorrow == calendar.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isEqual(Date dateFirst, Date dateSecond) {
        CALENDAR.setTime(dateFirst);
        resetCalendarTime(CALENDAR);
        dateFirst = CALENDAR.getTime();

        CALENDAR.setTime(dateSecond);
        resetCalendarTime(CALENDAR);
        dateSecond = CALENDAR.getTime();
        return dateFirst.equals(dateSecond);
    }

    public static long roundTimeToNearestNextMinutes(long millis, int round) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % round;
        calendar.add(Calendar.MINUTE, mod > 0 ? round - mod : 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static long roundTimeToNearestMinutes(long millis, int round) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % round;
        calendar.add(Calendar.MINUTE, (mod < round / 2) ? -mod : (round - mod));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static void resetCalendarTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static int nextMonth(int month) {
        return month == Calendar.DECEMBER ? Calendar.JANUARY : month + 1;
    }

    public static int previousMonth(int month) {
        return month == Calendar.JANUARY ? Calendar.DECEMBER : month - 1;
    }

    public static boolean isWeekendDay(int dayInWeek) {
        return dayInWeek == Calendar.SUNDAY || dayInWeek == Calendar.SATURDAY;
    }

    public static int getDaysInMonth(int month, int year) {
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
            case Calendar.DECEMBER:
                return 31;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                return 30;
            case Calendar.FEBRUARY:
                return ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) ? 29 : 28;
            default:
                throw new IllegalArgumentException("Invalid Month");
        }
    }

}
