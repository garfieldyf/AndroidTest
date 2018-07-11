package android.ext.temp;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Class TimeUtils
 * @author Garfield
 * @version 1.0
 */
public final class TimeUtils {
    /**
     * Returns a <tt>Calendar</tt> instance from the current <tt>thread-local</tt>.
     * @return A <tt>Calendar</tt> instance set to the current date and time in the
     * default <tt>TimeZone</tt>.
     * @see #getCalendar(long)
     */
    public static Calendar getCalendar() {
        return getCalendar(System.currentTimeMillis());
    }

    /**
     * Same as {@link #getCalendar()}, but the returns <tt>Calendar</tt>
     * instance use <em>timeInMillis</em> parameter to initialization.
     * @param timeInMillis The initial time as the number of milliseconds.
     * @return A <tt>Calendar</tt> instance.
     * @see #getCalendar()
     */
    public static Calendar getCalendar(long timeInMillis) {
        final Calendar calendar = sThreadLocal.get();
        calendar.setTimeInMillis(timeInMillis);
        calendar.setTimeZone(TimeZone.getDefault());
        return calendar;
    }

    /**
     * Converts the date based on current date and time.
     * @param yearDelta The {@link Calendar#YEAR} field delta.
     * @param monthDelta The {@link Calendar#MONTH} field delta.
     * @param dayDelta The {@link Calendar#DAY_OF_MONTH} field delta.
     * @return The converted date in milliseconds.
     * @see #convertTime(int, int, int)
     * @see #convertDateTime(int, int, int, int, int, int)
     */
    public static long convertDate(int yearDelta, int monthDelta, int dayDelta) {
        return convertDateTime(yearDelta, monthDelta, dayDelta, 0, 0, 0);
    }

    /**
     * Converts the time based on current date and time.
     * @param hourDelta The {@link Calendar#HOUR_OF_DAY} field delta.
     * @param minuteDelta The {@link Calendar#MINUTE} field delta.
     * @param secondDelta The {@link Calendar#SECOND} field delta.
     * @return The converted time in milliseconds.
     * @see #convertDate(int, int, int)
     * @see #convertDateTime(int, int, int, int, int, int)
     */
    public static long convertTime(int hourDelta, int minuteDelta, int secondDelta) {
        return convertDateTime(0, 0, 0, hourDelta, minuteDelta, secondDelta);
    }

    /**
     * Converts the date and time based on current date and time.
     * @param yearDelta The {@link Calendar#YEAR} field delta.
     * @param monthDelta The {@link Calendar#MONTH} field delta.
     * @param dayDelta The {@link Calendar#DAY_OF_MONTH} field delta.
     * @param hourDelta The {@link Calendar#HOUR_OF_DAY} field delta.
     * @param minuteDelta The {@link Calendar#MINUTE} field delta.
     * @param secondDelta The {@link Calendar#SECOND} field delta.
     * @return The converted date and time in milliseconds.
     * @see #convertDate(int, int, int)
     * @see #convertTime(int, int, int)
     */
    public static long convertDateTime(int yearDelta, int monthDelta, int dayDelta, int hourDelta, int minuteDelta, int secondDelta) {
        final Calendar calendar = getCalendar(System.currentTimeMillis());
        calendar.add(Calendar.YEAR, yearDelta);
        calendar.add(Calendar.MONTH, monthDelta);
        calendar.add(Calendar.DAY_OF_MONTH, dayDelta);
        calendar.add(Calendar.HOUR_OF_DAY, hourDelta);
        calendar.add(Calendar.MINUTE, minuteDelta);
        calendar.add(Calendar.SECOND, secondDelta);

        return calendar.getTimeInMillis();
    }

    /**
     * Converts the time and sets the date based on current date and time.
     * @param year The {@link Calendar#YEAR} field value.
     * @param month The {@link Calendar#MONTH} field value (0-11).
     * @param day The {@link Calendar#DAY_OF_MONTH} field value (1-based).
     * @param hourDelta The {@link Calendar#HOUR_OF_DAY} field delta.
     * @param minuteDelta The {@link Calendar#MINUTE} field delta.
     * @param secondDelta The {@link Calendar#SECOND} field delta.
     * @return The converted date and time in milliseconds.
     * @see #convertDateAndSetTime(int, int, int, int, int, int)
     */
    public static long convertTimeAndSetDate(int year, int month, int day, int hourDelta, int minuteDelta, int secondDelta) {
        final Calendar calendar = getCalendar(System.currentTimeMillis());
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.add(Calendar.HOUR_OF_DAY, hourDelta);
        calendar.add(Calendar.MINUTE, minuteDelta);
        calendar.add(Calendar.SECOND, secondDelta);

        return calendar.getTimeInMillis();
    }

    /**
     * Converts the date and sets the time based on current date and time.
     * @param yearDelta The {@link Calendar#YEAR} field delta.
     * @param monthDelta The {@link Calendar#MONTH} field delta.
     * @param dayDelta The {@link Calendar#DAY_OF_MONTH} field delta.
     * @param hour The {@link Calendar#HOUR_OF_DAY} field value (0-23).
     * @param minute The {@link Calendar#MINUTE} field value (0-59).
     * @param second The {@link Calendar#SECOND} field value (0-59).
     * @return The converted date and time in milliseconds.
     * @see #convertTimeAndSetDate(int, int, int, int, int, int)
     */
    public static long convertDateAndSetTime(int yearDelta, int monthDelta, int dayDelta, int hour, int minute, int second) {
        final Calendar calendar = getCalendar(System.currentTimeMillis());
        calendar.add(Calendar.YEAR, yearDelta);
        calendar.add(Calendar.MONTH, monthDelta);
        calendar.add(Calendar.DAY_OF_MONTH, dayDelta);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        return calendar.getTimeInMillis();
    }

    private static final ThreadLocal<Calendar> sThreadLocal = new ThreadLocal<Calendar>() {
        @Override
        protected Calendar initialValue() {
            return Calendar.getInstance();
        }
    };

    /**
     * This utility class cannot be instantiated.
     */
    private TimeUtils() {
    }
}
