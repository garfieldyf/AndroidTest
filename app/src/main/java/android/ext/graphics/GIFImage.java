package android.ext.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.Pools;
import android.ext.util.UriUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.util.Printer;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class GIFImage
 * @author Garfield
 */
public final class GIFImage {
    /**
     * The GIF image's width in pixels.
     */
    public final int width;

    /**
     * The GIF image's height in pixels.
     */
    public final int height;

    /**
     * The native GIF image handle.
     */
    private final long mNativeImage;

    /**
     * Decodes a {@link GIFImage} from the <tt>Resources</tt>.
     * @param res The resource containing the GIF data.
     * @param id The resource id to be decoded.
     * @return The <tt>GIFImage</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @throws NotFoundException if the given <em>id</em> does not exist.
     * @see #decode(byte[], int, int)
     * @see #decode(InputStream, byte[])
     * @see #decode(Context, Object, byte[])
     */
    public static GIFImage decode(Resources res, int id) {
        final InputStream is = res.openRawResource(id);
        try {
            return decode(is, null);
        } finally {
            FileUtils.close(is);
        }
    }

    /**
     * Decodes a {@link GIFImage} from the <tt>InputStream</tt>.
     * @param is The <tt>InputStream</tt> containing the GIF data.
     * @param tempStorage May be <tt>null</tt>. The temporary storage to use for decoding. Suggest 16K.
     * @return The <tt>GIFImage</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decode(Resources, int)
     * @see #decode(byte[], int, int)
     * @see #decode(Context, Object, byte[])
     */
    public static GIFImage decode(InputStream is, byte[] tempStorage) {
        DebugUtils.__checkError(is == null, "is == null");
        final long nativeImage;
        if (is instanceof FileInputStream) {
            nativeImage = nativeDecodeFile(getFileDescriptor(is));
        } else if (tempStorage == null) {
            nativeImage = decodeStreamInternal(is);
        } else {
            nativeImage = nativeDecodeStream(is, tempStorage);
        }

        return (nativeImage != 0 ? new GIFImage(nativeImage) : null);
    }

    /**
     * Decodes a {@link GIFImage} from the byte array.
     * @param data The byte array containing the GIF data.
     * @param offset The starting offset of the <em>data</em>.
     * @param length The number of bytes of the <em>data</em>, beginning at offset.
     * @return The <tt>GIFImage</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decode(Resources, int)
     * @see #decode(InputStream, byte[])
     * @see #decode(Context, Object, byte[])
     */
    public static GIFImage decode(byte[] data, int offset, int length) {
        ArrayUtils.checkRange(offset, length, data.length);
        final long nativeImage = nativeDecodeArray(data, offset, length);
        return (nativeImage != 0 ? new GIFImage(nativeImage) : null);
    }

    /**
     * Decodes a {@link GIFImage} from the specified <em>uri</em>.
     * <h3>The default implementation accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>{@link File} (no scheme)</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android.asset ({@link #SCHEME_ANDROID_ASSET})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param context The <tt>Context</tt>.
     * @param uri The uri to decode.
     * @param tempStorage May be <tt>null</tt>. The temporary storage to use for decoding. Suggest 16K.
     * @return The <tt>GIFImage</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decode(Resources, int)
     * @see #decode(byte[], int, int)
     * @see #decode(InputStream, byte[])
     * @see UriUtils#openInputStream(Context, Object)
     */
    public static GIFImage decode(Context context, Object uri, byte[] tempStorage) {
        try (InputStream is = UriUtils.openInputStream(context, uri)) {
            return decode(is, tempStorage);
        } catch (Exception e) {
            Log.e(GIFImage.class.getName(), "Couldn't decode from - " + uri, e);
            return null;
        }
    }

    /**
     * Returns the number of frames of this GIF image.
     * @return The frame count, must be >= 1.
     * @see #getFrameDelay(int)
     */
    public final int getFrameCount() {
        return nativeGetFrameCount(mNativeImage);
    }

    /**
     * Returns this GIF image frame delay with the specified <em>frameIndex</em>.
     * @param frameIndex The specified frame to return. The valid frame range is
     * 0 to {@link #getFrameCount} - 1.
     * @return The frame delay in milliseconds.
     * @see #getFrameCount()
     */
    public final int getFrameDelay(int frameIndex) {
        DebugUtils.__checkError(frameIndex < 0 || frameIndex >= getFrameCount(), "Invalid parameters - frameIndex out of bounds [ frameIndex = " + frameIndex + ", frameCount = " + getFrameCount() + " ]");
        return nativeGetFrameDelay(mNativeImage, frameIndex);
    }

    /**
     * Draw this GIF image the specified frame into the <em>bitmapCanvas</em>.
     * The bitmap canvas can be created by {@link #createBitmapCanvas()}.
     * @param bitmapCanvas The mutable <tt>Bitmap</tt> used to draw the frame.
     * @param frameIndex The specified frame to be drawn. The valid frame range
     * is 0 to {@link #getFrameCount} - 1.
     * @return <tt>true</tt> if the frame draw succeeded, <tt>false</tt> otherwise.
     * @see #createBitmapCanvas()
     */
    public final boolean draw(Bitmap bitmapCanvas, int frameIndex) {
        DebugUtils.__checkError(bitmapCanvas == null, "Invalid parameters - bitmapCanvas == null");
        DebugUtils.__checkError(bitmapCanvas.isRecycled(), "The bitmap canvas was recycled.");
        DebugUtils.__checkError(!bitmapCanvas.isMutable(), "The bitmap canvas must be a mutable bitmap.");
        DebugUtils.__checkError(bitmapCanvas.getConfig() != Config.ARGB_8888, "The bitmap canvas pixel format must be ARGB_8888");
        DebugUtils.__checkError(bitmapCanvas.getWidth() < width, "The bitmap canvas width (" + bitmapCanvas.getWidth() + ") must be >= GIF image width (" + width + ")");
        DebugUtils.__checkError(bitmapCanvas.getHeight() < height, "The bitmap canvas height (" + bitmapCanvas.getHeight() + ") must be >= GIF image height (" + height + ")");
        DebugUtils.__checkError(frameIndex < 0 || frameIndex >= getFrameCount(), "Invalid parameters - frameIndex out of bounds [ frameIndex = " + frameIndex + ", frameCount = " + getFrameCount() + " ]");
        return nativeDraw(bitmapCanvas, mNativeImage, frameIndex);
    }

    /**
     * Returns the minimum number of bytes that can
     * be used to store the bitmap canvas pixels.
     */
    public final int getBitmapCanvasBytes() {
        return width * height * 4;
    }

    /**
     * Returns a mutable {@link Bitmap} to draw this GIF image frames.
     * @return The <tt>Bitmap</tt>.
     * @see #draw(Bitmap, int)
     */
    public final Bitmap createBitmapCanvas() {
        return Bitmap.createBitmap(width, height, Config.ARGB_8888);
    }

    public final void dump(Printer printer) {
        printer.println(new StringBuilder(96)
            .append("GIFImage { nativePtr = 0x").append(Long.toHexString(mNativeImage))
            .append(", width = ").append(width).append(", height = ").append(height)
            .append(", frameCount = ").append(getFrameCount())
            .append(" }").toString());
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            nativeDestroy(mNativeImage);
        } finally {
            super.finalize();
        }
    }

    private GIFImage(long nativeImage) {
        mNativeImage = nativeImage;
        width  = nativeGetWidth(nativeImage);
        height = nativeGetHeight(nativeImage);
    }

    private static long decodeStreamInternal(InputStream is) {
        final byte[] buffer = Pools.BYTE_ARRAY_POOL.obtain();
        try {
            return nativeDecodeStream(is, buffer);
        } finally {
            Pools.BYTE_ARRAY_POOL.recycle(buffer);
        }
    }

    private static FileDescriptor getFileDescriptor(InputStream is) {
        try {
            return ((FileInputStream)is).getFD();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static native long nativeDecodeFile(FileDescriptor fd);
    private static native long nativeDecodeArray(byte[] data, int offset, int length);
    private static native long nativeDecodeStream(InputStream is, byte[] tempStorage);
    private static native void nativeDestroy(long nativeImage);
    private static native int nativeGetWidth(long nativeImage);
    private static native int nativeGetHeight(long nativeImage);
    private static native int nativeGetFrameCount(long nativeImage);
    private static native int nativeGetFrameDelay(long nativeImage, int frameIndex);
    private static native boolean nativeDraw(Bitmap bitmapCanvas, long nativeImage, int frameIndex);
}
