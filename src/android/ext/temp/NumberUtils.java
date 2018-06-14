package android.ext.temp;

/**
 * Class NumberUtils
 * @author Garfield
 * @version 1.5
 */
public final class NumberUtils {
    /**
     * Converts the two bytes to a short value.
     * @param high The high byte value.
     * @param low The low byte value.
     * @return A converted short value.
     * @see #lowValue(short)
     * @see #highValue(short)
     */
    public static short makeShort(byte high, byte low) {
        return (short)((high << 8) | (low & 0xFF));
    }

    /**
     * Returns low byte value from the specified short value.
     * @param value The short value.
     * @return The low byte value.
     * @see #highValue(short)
     * @see #makeShort(byte, byte)
     */
    public static byte lowValue(short value) {
        return (byte)value;
    }

    /**
     * Returns high byte value from the specified short value.
     * @param value The short value.
     * @return The high byte value.
     * @see #lowValue(short)
     * @see #makeShort(byte, byte)
     */
    public static byte highValue(short value) {
        return (byte)(value >>> 8);
    }

    /**
     * Converts the two shorts to a integer value.
     * @param high The high 16 bits value.
     * @param low The low 16 bits value.
     * @return A converted integer value.
     * @see #lowValue(int)
     * @see #highValue(int)
     */
    public static int makeInt(short high, short low) {
        return ((high << 16) | (low & 0xFFFF));
    }

    /**
     * Returns low 16 bits value from the specified integer value.
     * @param value The integer value.
     * @return The low 16 bits value.
     * @see #highValue(int)
     * @see #makeInt(short, short)
     */
    public static short lowValue(int value) {
        return (short)value;
    }

    /**
     * Returns high 16 bits value from the specified integer value.
     * @param value The integer value.
     * @return The high 16 bits value.
     * @see #lowValue(int)
     * @see #makeInt(short, short)
     */
    public static short highValue(int value) {
        return (short)(value >>> 16);
    }

    /**
     * Converts the two integers to a long value.
     * @param high The high 32 bits value.
     * @param low The low 32 bits value.
     * @return A converted long value.
     * @see #lowValue(long)
     * @see #highValue(long)
     */
    public static long makeLong(int high, int low) {
        return (((long)high << 32) | (low & 0xFFFFFFFFL));
    }

    /**
     * Returns low 32 bits value from the specified long value.
     * @param value The long value.
     * @return The low 32 bits value.
     * @see #highValue(long)
     * @see #makeLong(int, int)
     */
    public static int lowValue(long value) {
        return (int)value;
    }

    /**
     * Returns high 32 bits value from the specified long value.
     * @param value The long value.
     * @return The high 32 bits value.
     * @see #lowValue(long)
     * @see #makeLong(int, int)
     */
    public static int highValue(long value) {
        return (int)(value >>> 32);
    }

    /**
     * This utility class cannot be instantiated.
     */
    private NumberUtils() {
    }
}