package android.ext.util;

import android.util.Printer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Like as <tt>ByteArrayOutputStream</tt>, but this class is <b>not</b> thread-safely.
 * @author Garfield
 */
public final class ByteArrayBuffer extends OutputStream {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private byte[] data;
    private int size;

    /**
     * Constructor
     * @see #ByteArrayBuffer(int)
     */
    public ByteArrayBuffer() {
        data = EMPTY_BYTE_ARRAY;
    }

    /**
     * Constructor
     * @param capacity The initial capacity of this buffer.
     * @see #ByteArrayBuffer()
     */
    public ByteArrayBuffer(int capacity) {
        data = (capacity > 0 ? new byte[capacity + 1] : EMPTY_BYTE_ARRAY);
    }

    /**
     * Resets this buffer to the beginning of the underlying byte array.
     */
    public final void reset() {
        size = 0;
    }

    /**
     * Returns the number of bytes in this buffer.
     * @return The number of bytes in this buffer.
     * @see #array()
     */
    public final int size() {
        return size;
    }

    /**
     * Returns the underlying byte array (not a copy) associated with this buffer.
     * @return The byte array.
     * @see #size()
     * @see #toByteArray()
     */
    public final byte[] array() {
        return data;
    }

    /**
     * Returns the contents of this buffer as a byte array.
     * @return A copy of byte array.
     * @see #array()
     */
    public final byte[] toByteArray() {
        return ArrayUtils.copyOf(data, size, size);
    }

    /**
     * Returns an {@link InputStream} which is based on the
     * contents of this buffer.
     * @return An <tt>InputStream</tt>.
     */
    public final InputStream asInputStream() {
        return new ByteArrayInputStream(data, 0, size);
    }

    /**
     * Returns the contents of this buffer as a string.
     * @return This buffer contents as a string.
     * @see #toString(String)
     */
    @Override
    public String toString() {
        return new String(data, 0, size);
    }

    /**
     * Returns the contents of this buffer as a string converted to
     * the given the <em>charset</em>.
     * @param charset The {@link Charset} that defines the character
     * converter of this buffer contents.
     * @return This buffer contents as an encoded string.
     * @see #toString()
     */
    public String toString(Charset charset) {
        return new String(data, 0, size, charset);
    }

    /**
     * @see #write(byte[])
     * @see #write(byte[], int, int)
     */
    @Override
    public void write(int oneByte) {
        expandCapacity(1, true);
        data[size++] = (byte)oneByte;
    }

    /**
     * @see #write(int)
     * @see #write(byte[], int, int)
     */
    @Override
    public void write(byte[] buffer) {
        DebugUtils.__checkError(buffer == null, "Invalid parameter - buffer == null");
        write(buffer, 0, buffer.length);
    }

    /**
     * @see #write(int)
     * @see #write(byte[])
     */
    @Override
    public void write(byte[] buffer, int offset, int count) {
        DebugUtils.__checkRange(offset, count, buffer.length);
        if (count > 0) {
            expandCapacity(count, true);
            System.arraycopy(buffer, offset, data, size, count);
            size += count;
        }
    }

    /**
     * Writes the <tt>ByteBuffer</tt> remaining contents to this buffer.
     * @param buffer The <tt>ByteBuffer</tt> to read the contents.
     * @see #readFrom(InputStream, int, Cancelable)
     */
    public final void readFrom(ByteBuffer buffer) {
        DebugUtils.__checkError(buffer == null, "Invalid parameter - buffer == null");
        final int count = buffer.remaining();
        if (count > 0) {
            expandCapacity(count, false);
            buffer.get(data, size, count);
            size += count;
        }
    }

    /**
     * Writes the specified <tt>InputStream</tt> contents to this buffer.
     * @param is The <tt>InputStream</tt> to read the contents.
     * @param contentLength May be <tt>0</tt>. The <tt>InputStream</tt> contents length in bytes.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or <tt>null</tt>
     * if none. If the operation was cancelled before it completed normally this buffer's contents is undefined.
     * @throws IOException if an error occurs while read from <em>is</em>.
     * @see #readFrom(ByteBuffer)
     */
    public final void readFrom(InputStream is, int contentLength, Cancelable cancelable) throws IOException {
        // Expands this buffer capacity.
        DebugUtils.__checkError(is == null, "Invalid parameter - is == null");
        DebugUtils.__checkDebug(true, "ByteArrayBuffer", "contentLength = " + contentLength);
        expandCapacity(contentLength, false);
        cancelable = Cancelable.ofNullable(cancelable);

        // Reads the all bytes from the InputStream.
        int count, readBytes, expandCount = 1024;
        while (!cancelable.isCancelled()) {
            if ((count = data.length - size) <= 0) {
                count = expandCount;
                expandCapacity(count, true);

                if (expandCount < 16384) {
                    expandCount <<= 1;  // expandCount * 2
                }
            }

            if ((readBytes = is.read(data, size, count)) == -1) {
                break;
            }

            size += readBytes;
        }
    }

    /**
     * Writes this buffer contents to the specified <em>buffer</em>.
     * @param buffer The <tt>ByteBuffer</tt> to write to.
     * @return The <em>buffer</em>.
     * @see #writeTo(OutputStream)
     */
    public final ByteBuffer writeTo(ByteBuffer buffer) {
        DebugUtils.__checkError(buffer == null, "Invalid parameter - buffer == null");
        return buffer.put(data, 0, size);
    }

    /**
     * Writes this buffer contents to the specified <tt>OutputStream</tt>.
     * @param out The <tt>OutputStream</tt> to write to.
     * @throws IOException if an error occurs while writing to <em>out</em>.
     * @see #writeTo(ByteBuffer)
     */
    public final void writeTo(OutputStream out) throws IOException {
        DebugUtils.__checkError(out == null, "Invalid parameter - out == null");
        out.write(data, 0, size);
    }

    public final void dump(Printer printer) {
        printer.println(new StringBuilder(128)
            .append("ByteArrayBuffer [ size = ").append(size).append('(').append(FileUtils.formatFileSize(size)).append(')')
            .append(", capacity = ").append(data.length).append('(').append(FileUtils.formatFileSize(data.length)).append(')')
            .append(", remaining = ").append(data.length - size).append('(').append(FileUtils.formatFileSize(data.length - size)).append(')')
            .append(" ]").toString());
    }

    private void expandCapacity(int expandCount, boolean growUp) {
        final int minCapacity = expandCount + size;
        if (minCapacity > data.length) {
            data = ArrayUtils.copyOf(data, size, growUp ? Math.max(minCapacity, (data.length * 3) / 2) : minCapacity + 1);
        }
    }
}
