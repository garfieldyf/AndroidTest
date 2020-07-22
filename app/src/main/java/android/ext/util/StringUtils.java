package android.ext.util;

/**
 * Class StringUtils
 * @author Garfield
 */
public final class StringUtils {
    /**
     * Returns the number of characters in the <em>s</em>,
     * handling <tt>null CharSequence</tt>.
     * @param s The <tt>CharSequence</tt>.
     * @return The number of characters.
     */
    public static int getLength(CharSequence s) {
        return (s != null ? s.length() : 0);
    }

    /**
     * Copies the <tt>CharSequence</tt> removing white space characters from
     * the beginning and end of the <em>s</em>.
     * @param s The <tt>CharSequence</tt> to trim.
     * @return A new <tt>CharSequence</tt> with characters <tt><='\u0020'</tt>
     * removed from the beginning and the end.
     */
    public static CharSequence trim(CharSequence s) {
        if (s instanceof String) {
            return ((String)s).trim();
        }

        final int last = s.length() - 1;
        int start = 0, end = last;

        // Trims the beginning white space characters.
        while (start <= end && s.charAt(start) <= ' ') {
            ++start;
        }

        // Trims the end white space characters.
        while (end >= start && s.charAt(end) <= ' ') {
            --end;
        }

        return (start == 0 && end == last ? s : s.subSequence(start, end + 1));
    }

    /**
     * Tests if the specified string <em>source</em> starts with
     * the specified character <em>c</em>.
     * @param source The source string.
     * @param c The character to search.
     * @return <tt>true</tt> if the <em>c</em> is a prefix of the
     * <em>source</em> string, <tt>false</tt> otherwise.
     * @see #endsWith(CharSequence, char)
     */
    public static boolean startsWith(CharSequence source, char c) {
        return (getLength(source) > 0 && source.charAt(0) == c);
    }

    /**
     * Tests if the specified string <em>source</em> ends with
     * the specified character <em>c</em>.
     * @param source The source string.
     * @param c The character to search.
     * @return <tt>true</tt> if the <em>c</em> is a suffix of
     * the <em>source</em> string, <tt>false</tt> otherwise.
     * @see #startsWith(CharSequence, char)
     */
    public static boolean endsWith(CharSequence source, char c) {
        final int index = getLength(source) - 1;
        return (index > 0 && source.charAt(index) == c);
    }

    /**
     * Tests if the specified string <em>source</em> starts with
     * the specified <em>prefix</em>.
     * @param source The source string.
     * @param prefix The prefix string to search.
     * @return <tt>true</tt> if the specified string is a prefix
     * of the <em>source</em> string, <tt>false</tt> otherwise.
     * @see #startsWith(CharSequence, int, CharSequence)
     * @see #endsWith(CharSequence, CharSequence)
     */
    public static boolean startsWith(CharSequence source, CharSequence prefix) {
        return regionMatches(source, 0, prefix);
    }

    /**
     * Tests if the specified string <em>source</em> starts with
     * the specified <em>prefix</em>.
     * @param source The source string.
     * @param start The starting offset of the <em>source</em>.
     * @param prefix The prefix string to search.
     * @return <tt>true</tt> if the specified string is a prefix
     * of the <em>source</em> string, <tt>false</tt> otherwise.
     * @see #startsWith(CharSequence, CharSequence)
     * @see #endsWith(CharSequence, CharSequence)
     */
    public static boolean startsWith(CharSequence source, int start, CharSequence prefix) {
        return regionMatches(source, start, prefix);
    }

    /**
     * Tests if the specified string <em>source</em> ends with
     * the specified <em>suffix</em>.
     * @param source The source string.
     * @param suffix The suffix string to search.
     * @return <tt>true</tt> if the specified string is a suffix
     * of the <em>source</em> string, <tt>false</tt> otherwise.
     * @see #startsWith(CharSequence, CharSequence)
     * @see #startsWith(CharSequence, int, CharSequence)
     */
    public static boolean endsWith(CharSequence source, CharSequence suffix) {
        return regionMatches(source, source.length() - suffix.length(), suffix);
    }

    /**
     * Converts the specified byte array to a hexadecimal string.
     * @param data The array to convert.
     * @return The hexadecimal string.
     * @see #toHexString(byte[], int, int)
     * @see #toHexString(StringBuilder, byte[], int, int)
     */
    public static String toHexString(byte[] data) {
        return toHexString(new StringBuilder(data.length << 1), data, 0, data.length).toString();
    }

    /**
     * Converts the specified byte array to a hexadecimal string.
     * @param data The array to convert.
     * @param start The inclusive beginning index of the <em>data</em>.
     * @param end The exclusive end index of the <em>data</em>.
     * @return The hexadecimal string.
     * @see #toHexString(byte[])
     * @see #toHexString(StringBuilder, byte[], int, int)
     */
    public static String toHexString(byte[] data, int start, int end) {
        return toHexString(new StringBuilder((end - start) << 1), data, start, end).toString();
    }

    /**
     * Converts the specified byte array to a hexadecimal string.
     * @param out The <tt>StringBuilder</tt> to append the converted hexadecimal string.
     * @param data The array to convert.
     * @param start The inclusive beginning index of the <em>data</em>.
     * @param end The exclusive end index of the <em>data</em>.
     * @return The <em>out</em>.
     * @see #toHexString(byte[])
     * @see #toHexString(byte[], int, int)
     */
    public static StringBuilder toHexString(StringBuilder out, byte[] data, int start, int end) {
        for (int digit; start < end; ++start) {
            digit = (data[start] & 0xFF);
            out.append(HEX_DIGITS[digit >>> 4]).append(HEX_DIGITS[digit & 0x0F]);
        }

        return out;
    }

    private static boolean regionMatches(CharSequence source, int start, CharSequence search) {
        final int length = search.length();
        if (start < 0 || source.length() - start < length) {
            return false;
        }

        for (int i = 0; i < length; ++i) {
            if (source.charAt(start + i) != search.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * The hexadecimal digit characters.
     */
    private final static char[] HEX_DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    /**
     * This utility class cannot be instantiated.
     */
    private StringUtils() {
    }
}
