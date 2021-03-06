package android.ext.util;

/**
 * Class MessageDigests
 * @author Garfield
 */
public final class MessageDigests {
    /**
     * The message digest hash algorithm.
     */
    public static enum Algorithm {
        /**
         * The MD5 hashing algorithm.
         */
        MD5(16),

        /**
         * The SHA1(SHA) hashing algorithm.
         */
        SHA1(20),

        /**
         * The SHA256 hashing algorithm.
         */
        SHA256(32),

        /**
         * The SHA384 hashing algorithm.
         */
        SHA384(48),

        /**
         * The SHA512 hashing algorithm.
         */
        SHA512(64);

        /**
         * The message digest length in bytes.
         */
        public final int digestLength;

        /**
         * Constructor
         */
        private Algorithm(int digestLength) {
            this.digestLength = digestLength;
        }
    }

    /**
     * Computes the file's contents the final hash value using the given <em>filename</em>
     * and <em>algorithm</em>.
     * @param filename The filename, must be absolute file path.
     * @param algorithm The {@link Algorithm} to compute.
     * @return The computed hash value if the operation succeeded, <tt>null</tt> otherwise.
     * @see #computeFile(String, byte[], int, Algorithm)
     */
    public static byte[] computeFile(String filename, Algorithm algorithm) {
        DebugUtils.__checkError(algorithm == null, "Invalid parameter - algorithm == null");
        if (StringUtils.getLength(filename) == 0) {
            throw new IllegalArgumentException("Invalid parameter - The filename is null or 0-length");
        }

        final byte[] result = new byte[algorithm.digestLength];
        return (computeFile(filename, result, 0, algorithm.ordinal()) > 0 ? result : null);
    }

    /**
     * Computes the file's contents the final hash value using the given <em>filename</em>
     * and <em>algorithm</em>, Stores the computed hash value in <em>result</em>.
     * @param filename The filename, must be absolute file path.
     * @param result The byte array to store the computed hash value.
     * @param offset The start position in the <em>result</em>.
     * @param algorithm The {@link Algorithm} to compute.
     * @return The number of bytes written to <em>result</em> if the operation succeeded,
     * <tt>0</tt> otherwise.
     * @see #computeFile(String, Algorithm)
     */
    public static int computeFile(String filename, byte[] result, int offset, Algorithm algorithm) {
        DebugUtils.__checkError(algorithm == null, "Invalid parameter - algorithm == null");
        if (StringUtils.getLength(filename) == 0) {
            throw new IllegalArgumentException("Invalid parameter - The filename is null or 0-length");
        }

        ArrayUtils.checkRange(offset, algorithm.digestLength, result.length);
        return computeFile(filename, result, offset, algorithm.ordinal());
    }

    /**
     * Computes the final hash value using the given <em>string</em>.
     * The <em>string</em> converted using the UTF-8 charset.
     * @param string The <tt>String</tt> to compute.
     * @param algorithm The {@link Algorithm} to compute.
     * @return The computed hash value.
     * @see #computeString(String, byte[], int, Algorithm)
     */
    public static byte[] computeString(String string, Algorithm algorithm) {
        DebugUtils.__checkError(algorithm == null, "Invalid parameter - algorithm == null");
        if (string == null) {
            throw new NullPointerException("Invalid parameter - string == null");
        }

        final byte[] result = new byte[algorithm.digestLength];
        computeString(string, result, 0, algorithm.ordinal());
        return result;
    }

    /**
     * Computes the final hash value using the given <em>string</em>
     * and <em>algorithm</em>, Stores the result in <em>result</em>.
     * The <em>string</em> converted using the UTF-8 charset.
     * @param string The <tt>String</tt> to compute.
     * @param result The byte array to store the computed hash value.
     * @param offset The start position in the <em>result</em>.
     * @param algorithm The {@link Algorithm} to compute.
     * @return The number of bytes written to <em>result</em>.
     * @see #computeString(String, Algorithm)
     */
    public static int computeString(String string, byte[] result, int offset, Algorithm algorithm) {
        DebugUtils.__checkError(algorithm == null, "Invalid parameter - algorithm == null");
        if (string == null) {
            throw new NullPointerException("Invalid parameter - string == null");
        }

        ArrayUtils.checkRange(offset, algorithm.digestLength, result.length);
        return computeString(string, result, offset, algorithm.ordinal());
    }

    /**
     * Computes the final hash value using the given byte array.
     * @param data The byte array to compute.
     * @param algorithm The {@link Algorithm} to compute.
     * @return The computed hash value.
     * @see #computeByteArray(byte[], int, int, Algorithm)
     * @see #computeByteArray(byte[], int, int, byte[], int, Algorithm)
     */
    public static byte[] computeByteArray(byte[] data, Algorithm algorithm) {
        DebugUtils.__checkError(data == null || algorithm == null, "Invalid parameters - data == null || algorithm == null");
        return computeByteArray(data, 0, data.length, algorithm);
    }

    /**
     * Computes the final hash value using the given byte array from the
     * specified <em>offset</em>, <em>length</em> and <em>algorithm</em>.
     * @param data The byte array to compute.
     * @param offset The start position in the <em>data</em>.
     * @param count The number of count in the <em>data</em>.
     * @param algorithm The {@link Algorithm} to compute.
     * @return The computed hash value.
     * @see #computeByteArray(byte[], Algorithm)
     * @see #computeByteArray(byte[], int, int, byte[], int, Algorithm)
     */
    public static byte[] computeByteArray(byte[] data, int offset, int count, Algorithm algorithm) {
        DebugUtils.__checkError(data == null || algorithm == null, "Invalid parameters - data == null || algorithm == null");
        ArrayUtils.checkRange(offset, count, data.length);
        final byte[] result = new byte[algorithm.digestLength];
        computeByteArray(data, offset, count, result, 0, algorithm.ordinal());
        return result;
    }

    /**
     * Computes the final hash value using the given byte array from the specified
     * <em>dataOffset</em>, <em>dataCount</em> and <em>algorithm</em>, Stores the
     * result in <em>result</em>.
     * @param data The byte array to compute.
     * @param dataOffset The start position in the <em>data</em>.
     * @param dataCount The number of count in the <em>data</em>.
     * @param result The byte array to store the computed hash value.
     * @param offset The start position in the <em>result</em>.
     * @param algorithm The {@link Algorithm} to compute.
     * @return The number of bytes written to <em>result</em>.
     * @see #computeByteArray(byte[], Algorithm)
     * @see #computeByteArray(byte[], int, int, Algorithm)
     */
    public static int computeByteArray(byte[] data, int dataOffset, int dataCount, byte[] result, int offset, Algorithm algorithm) {
        DebugUtils.__checkError(data == null || result == null || algorithm == null, "Invalid parameters - data == null || result == null || algorithm == null");
        ArrayUtils.checkRange(dataOffset, dataCount, data.length);
        ArrayUtils.checkRange(offset, algorithm.digestLength, result.length);
        return computeByteArray(data, dataOffset, dataCount, result, offset, algorithm.ordinal());
    }

    private static native int computeString(String string, byte[] result, int offset, int algorithm);
    private static native int computeFile(String filename, byte[] result, int offset, int algorithm);
    private static native int computeByteArray(byte[] data, int dataOffset, int dataCount, byte[] result, int offset, int algorithm);

    /**
     * This class cannot be instantiated.
     */
    private MessageDigests() {
    }
}
