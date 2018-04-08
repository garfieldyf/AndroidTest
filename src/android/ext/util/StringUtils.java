package android.ext.util;

import java.lang.reflect.Constructor;

/**
 * Class StringUtils
 * @author Garfield
 * @version 1.0
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
     * Equivalent to calling <tt>newString(chars, 0, chars.length)</tt>.
     * @param chars The character array to create.
     * @return A new <tt>String</tt>.
     * @see #newString(char[], int, int)
     */
    public static String newString(char[] chars) {
        return StringFactory.newString(chars, 0, chars.length);
    }

    /**
     * Returns a new <tt>String</tt> from the contents of the specified
     * <em>chars</em>. Unlike {@link String#String(char[], int, int)},
     * this method do not copy the <em>chars</em> array.
     * @param chars The character array to create.
     * @param offset The start position in the <em>chars</em>.
     * @param count The number of count in the <em>chars</em>.
     * @return A new <tt>String</tt>.
     * @see #newString(char[])
     */
    public static String newString(char[] chars, int offset, int count) {
        return StringFactory.newString(chars, offset, count);
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
     * @param lowerCase Whether to convert a lower case hexadecimal
     * characters, using "abcdef".
     * @return The hexadecimal string.
     * @see #toHexString(byte[], int, int, boolean)
     * @see #toHexString(StringBuilder, byte[], int, int, boolean)
     */
    public static String toHexString(byte[] data, boolean lowerCase) {
        return toHexString(new StringBuilder(data.length << 1), data, 0, data.length, lowerCase).toString();
    }

    /**
     * Converts the specified byte array to a hexadecimal string.
     * @param data The array to convert.
     * @param start The inclusive beginning index of the <em>data</em>.
     * @param end The exclusive end index of the <em>data</em>.
     * @param lowerCase Whether to convert a lower case hexadecimal
     * characters, using "abcdef".
     * @return The hexadecimal string.
     * @see #toHexString(byte[], boolean)
     * @see #toHexString(StringBuilder, byte[], int, int, boolean)
     */
    public static String toHexString(byte[] data, int start, int end, boolean lowerCase) {
        return toHexString(new StringBuilder((end - start) << 1), data, start, end, lowerCase).toString();
    }

    /**
     * Converts the specified byte array to a hexadecimal string.
     * @param out The <tt>StringBuilder</tt> to append the converted
     * hexadecimal string.
     * @param data The array to convert.
     * @param start The inclusive beginning index of the <em>data</em>.
     * @param end The exclusive end index of the <em>data</em>.
     * @param lowerCase Whether to convert a lower case hexadecimal
     * characters, using "abcdef".
     * @return The <em>out</em>.
     * @see #toHexString(byte[], boolean)
     * @see #toHexString(byte[], int, int, boolean)
     */
    public static StringBuilder toHexString(StringBuilder out, byte[] data, int start, int end, boolean lowerCase) {
        final char firstLetter = (lowerCase ? 'a' : 'A');
        for (int digit; start < end; ++start) {
            digit = data[start];
            out.append(toChar((digit >> 4) & 0xf, firstLetter)).append(toChar(digit & 0xf, firstLetter));
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

    private static char toChar(int digit, char firstLetter) {
        return (char)(digit < 10 ? digit + '0' : digit + firstLetter - 10);
    }

    /**
     * Class <tt>StringFactory</tt> used to create a new <tt>String</tt>.
     */
    private static final class StringFactory {
        private static final Constructor<String> sInstance;

        public static String newString(char[] chars, int offset, int count) {
            try {
                ArrayUtils.checkRange(offset, count, chars.length);
                return sInstance.newInstance(offset, count, chars);
            } catch (ReflectiveOperationException e) {
                throw new Error(e);
            }
        }

        static {
            try {
                sInstance = String.class.getDeclaredConstructor(int.class, int.class, char[].class);
                sInstance.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new Error(e);
            }
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private StringUtils() {
    }
}
