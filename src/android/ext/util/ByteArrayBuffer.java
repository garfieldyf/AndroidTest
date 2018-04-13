package android.ext.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import android.content.Context;
import android.text.format.Formatter;
import android.util.Printer;

/**
 * Class ByteArrayBuffer
 * @author Garfield
 * @version 2.0
 */
public final class ByteArrayBuffer extends OutputStream {
    private byte[] data;
    private int size;

    /**
     * Constructor
     * @see #ByteArrayBuffer(int)
     */
    public ByteArrayBuffer() {
        data = ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    /**
     * Constructor
     * @param capacity The initial capacity of this buffer.
     * @see #ByteArrayBuffer()
     */
    public ByteArrayBuffer(int capacity) {
        data = (capacity > 0 ? new byte[capacity + 1] : ArrayUtils.EMPTY_BYTE_ARRAY);
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
     * Removes all contents from this buffer, leaving it empty.
     * @see #reset()
     */
    public final void clear() {
        size = 0;
        data = ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    /**
     * Resets this buffer the underlying byte array size to 0.
     * @see #clear()
     */
    public final void reset() {
        size = 0;
    }

    /**
     * Returns the contents of this buffer as a byte array.
     * @return A copy of byte array.
     * @see #array()
     */
    public final byte[] toByteArray() {
        return (size > 0 ? copyOf(size) : ArrayUtils.EMPTY_BYTE_ARRAY);
    }

    /**
     * Returns an <tt>InputStream</tt> which is based on the
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
     * Returns the contents of this buffer as a string converted
     * according to the encoding declared in <em>charsetName</em>.
     * @param charsetName The charset name of this buffer contents.
     * @return This buffer contents as an encoded string.
     * @see #toString()
     */
    public String toString(String charsetName) {
        return new String(data, 0, size, Charset.forName(charsetName));
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
        DebugUtils.__checkError(buffer == null, "buffer == null");
        write(buffer, 0, buffer.length);
    }

    /**
     * @see #write(int)
     * @see #write(byte[])
     */
    @Override
    public void write(byte[] buffer, int offset, int count) {
        ArrayUtils.checkRange(offset, count, buffer.length);
        if (count > 0) {
            expandCapacity(count, true);
            System.arraycopy(buffer, offset, data, size, count);
            size += count;
        }
    }

    /**
     * Writes the <tt>ByteBuffer</tt> remaining contents to this buffer.
     * @param buffer The <tt>ByteBuffer</tt> to read the contents.
     * @see #readFrom(InputStream, Cancelable)
     */
    public final void readFrom(ByteBuffer buffer) {
        DebugUtils.__checkError(buffer == null, "buffer == null");
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
     * @param cancelable A {@link Cancelable} that can be cancelled,
     * or <tt>null</tt> if none. If the operation was cancelled before it
     * completed normally then this buffer's contents undefined.
     * @throws IOException if an error occurs while read from <em>is</em>.
     * @see #readFrom(ByteBuffer)
     */
    public final void readFrom(InputStream is, Cancelable cancelable) throws IOException {
        DebugUtils.__checkError(is == null, "is == null");
        expandCapacity(is.available(), false);
        int count, readBytes, expandCount = 256;
        while (cancelable == null || !cancelable.isCancelled()) {
            if ((count = data.length - size) <= 0) {
                count = expandCount;
                expandCapacity(count, true);

                if (expandCount < 8192) {
                    expandCount <<= 1;  // expandCount * 2
                }
            }

            if ((readBytes = is.read(data, size, count)) <= 0) {
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
        DebugUtils.__checkError(buffer == null, "buffer == null");
        return buffer.put(data, 0, size);
    }

    /**
     * Writes this buffer contents to the specified <tt>OutputStream</tt>.
     * @param out The <tt>OutputStream</tt> to write to.
     * @throws IOException if an error occurs while writing to <em>out</em>.
     * @see #writeTo(ByteBuffer)
     */
    public final void writeTo(OutputStream out) throws IOException {
        DebugUtils.__checkError(out == null, "out == null");
        out.write(data, 0, size);
    }

    public final void dump(Context context, Printer printer) {
        printer.println(new StringBuilder(128)
               .append(" ByteArrayBuffer [ size = ").append(size).append('(').append(Formatter.formatFileSize(context, size)).append(')')
               .append(", capacity = ").append(data.length).append('(').append(Formatter.formatFileSize(context, data.length)).append(')')
               .append(", remaining = ").append(data.length - size).append('(').append(Formatter.formatFileSize(context, data.length - size)).append(')')
               .append(" ]").toString());
    }

    private byte[] copyOf(int newLength) {
        final byte[] newData = new byte[newLength];
        System.arraycopy(data, 0, newData, 0, size);
        return newData;
    }

    private void expandCapacity(int expandCount, boolean growUp) {
        final int minCapacity = expandCount + size;
        if (minCapacity > data.length) {
            data = copyOf(growUp ? Math.max(minCapacity, (data.length * 3) / 2) : minCapacity + 1);
        }
    }
}
