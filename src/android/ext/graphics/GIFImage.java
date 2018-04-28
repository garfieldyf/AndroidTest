package android.ext.graphics;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import android.content.res.AssetManager.AssetInputStream;
import android.content.res.Resources;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;

/**
 * Class GIFImage
 * @author Garfield
 * @version 1.0
 */
public class GIFImage {
    /**
     * The native GIF image handle.
     */
    private final long mNativeImage;

    /**
     * Decodes a GIF file into a GIF image. If the specified <em>filename</em> is null,
     * or cannot be decoded into a GIF image, the function returns <tt>null</tt>.
     * @param filename The file to be decoded, must be absolute file path.
     * @return The decoded {@link GIFImage}, or <tt>null</tt> if it could not be decoded.
     * @see #decodeStream(InputStream, byte[])
     * @see #decodeResource(Resources, int)
     * @see #decodeByteArray(byte[], int, int)
     */
    public static GIFImage decodeFile(String filename) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(filename);
            final long nativeImage = nativeDecodeFile(is.getFD());
            return (nativeImage != 0 ? new GIFImage(nativeImage) : null);
        } catch (Exception e) {
            Log.e(GIFImage.class.getName(), new StringBuilder("Couldn't decode file - ").append(filename).toString(), e);
            return null;
        } finally {
            FileUtils.close(is);
        }
    }

    /**
     * Decodes an <tt>InputStream</tt> into a GIF image. If the specified <em>is</em> is null,
     * or cannot be decoded into a GIF image, the function returns <tt>null</tt>.
     * @param is The <tt>InputStream</tt> containing the GIF data.
     * @param tempStorage May be <tt>null</tt>. The temp storage to use for decoding. Suggest 16K.
     * @return The decoded {@link GIFImage}, or <tt>null</tt> if it could not be decoded.
     * @see #decodeFile(String)
     * @see #decodeResource(Resources, int)
     * @see #decodeByteArray(byte[], int, int)
     */
    public static GIFImage decodeStream(InputStream is, byte[] tempStorage) {
        long nativeImage = 0;
        try {
            if (is instanceof AssetInputStream) {
                nativeImage = nativeDecodeAsset(Asset.sField.getLong(is));
            } else if (is instanceof FileInputStream) {
                nativeImage = nativeDecodeFile(((FileInputStream)is).getFD());
            } else if (is != null) {
                nativeImage = decodeStreamInternal(is, tempStorage);
            }
        } catch (Exception e) {
            Log.e(GIFImage.class.getName(), "Couldn't decode - " + is.getClass().getName(), e);
        }

        return (nativeImage != 0 ? new GIFImage(nativeImage) : null);
    }

    /**
     * Decodes a resource into a GIF image. If the specified <em>res</em> cannot be decoded
     * into a GIF image, the function returns <tt>null</tt>.
     * @param res The resource containing the GIF data.
     * @param id The resource id to be decoded.
     * @return The decoded {@link GIFImage}, or <tt>null</tt> if it could not be decoded.
     * @see #decodeFile(String)
     * @see #decodeStream(InputStream, byte[])
     * @see #decodeByteArray(byte[], int, int)
     */
    public static GIFImage decodeResource(Resources res, int id) {
        InputStream is = null;
        try {
            is = res.openRawResource(id);
            return decodeStream(is, null);
        } catch (Exception e) {
            Log.e(GIFImage.class.getName(), "Couldn't found resource ID #0x" + Integer.toHexString(id), e);
            return null;
        } finally {
            FileUtils.close(is);
        }
    }

    /**
     * Decodes a byte array into a GIF image. If the specified <em>data</em> cannot be
     * decoded into a GIF image, the function returns <tt>null</tt>.
     * @param data The byte array containing the GIF data.
     * @param offset The starting offset of the <em>data</em>.
     * @param length The number of bytes of the <em>data</em>, beginning at offset.
     * @return The decoded {@link GIFImage}, or <tt>null</tt> if it could not be decoded.
     * @see #decodeFile(String)
     * @see #decodeStream(InputStream, byte[])
     * @see #decodeResource(Resources, int)
     */
    public static GIFImage decodeByteArray(byte[] data, int offset, int length) {
        ArrayUtils.checkRange(offset, length, data.length);
        final long nativeImage = nativeDecodeArray(data, offset, length);
        return (nativeImage != 0 ? new GIFImage(nativeImage) : null);
    }

    /**
     * Returns this GIF image's width.
     * @return The width in pixels.
     * @see #getHeight()
     */
    public int getWidth() {
        return nativeGetWidth(mNativeImage);
    }

    /**
     * Returns this GIF image's height.
     * @return The height in pixels.
     * @see #getWidth()
     */
    public int getHeight() {
        return nativeGetHeight(mNativeImage);
    }

    /**
     * Returns this GIF image frame count.
     * @return The frame count, must be >= 1.
     * @see #getFrameDelay(int)
     */
    public int getFrameCount() {
        return nativeGetFrameCount(mNativeImage);
    }

    /**
     * Returns this GIF image frame delay with the specified <em>frameIndex</em>.
     * @param frameIndex The specified frame to return. The valid frame range is
     * 0 to {@link #getFrameCount} - 1.
     * @return The frame delay in milliseconds.
     * @see #getFrameCount()
     */
    public int getFrameDelay(int frameIndex) {
        return nativeGetFrameDelay(mNativeImage, frameIndex);
    }

    /**
     * Draw this GIF image the specified frame into the <em>bitmapCanvas</em>.
     * The bitmap canvas can be invoke {@link #createBitmapCanvas()} to create.
     * @param bitmapCanvas The mutable <tt>Bitmap</tt> used to draw the frame.
     * @param frameIndex The specified frame to be drawn. The valid frame range
     * is 0 to {@link #getFrameCount} - 1.
     * @return <tt>true</tt> if the frame draw succeeded, <tt>false</tt> otherwise.
     * @see #createBitmapCanvas()
     */
    public boolean draw(Bitmap bitmapCanvas, int frameIndex) {
        DebugUtils.__checkError(!bitmapCanvas.isMutable(), "Immutable bitmap passed to GIFImage draw");
        DebugUtils.__checkError(bitmapCanvas.getConfig() != Config.ARGB_8888, "The bitmap canvas pixel format must be ARGB_8888");
        return nativeDraw(bitmapCanvas, mNativeImage, frameIndex);
    }

    /**
     * Returns a mutable {@link Bitmap} to draw this GIF image frames.
     * @return The <tt>Bitmap</tt>.
     * @see #draw(Bitmap, int)
     */
    public final Bitmap createBitmapCanvas() {
        return Bitmap.createBitmap(nativeGetWidth(mNativeImage), nativeGetHeight(mNativeImage), Config.ARGB_8888);
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
    }

    private static long decodeStreamInternal(InputStream is, byte[] tempStorage) {
        if (tempStorage == null) {
            tempStorage = new byte[16384];
        }

        return nativeDecodeStream(is, tempStorage);
    }

    private static native long nativeDecodeAsset(long asset);
    private static native long nativeDecodeFile(FileDescriptor fd);
    private static native long nativeDecodeArray(byte[] data, int offset, int length);
    private static native long nativeDecodeStream(InputStream is, byte[] tempStorage);
    private static native void nativeDestroy(long nativeImage);
    private static native int nativeGetWidth(long nativeImage);
    private static native int nativeGetHeight(long nativeImage);
    private static native int nativeGetFrameCount(long nativeImage);
    private static native int nativeGetFrameDelay(long nativeImage, int frameIndex);
    private static native boolean nativeDraw(Bitmap bitmapCanvas, long nativeImage, int frameIndex);

    /**
     * Class <tt>Asset</tt> used to obtains the native asset from the <tt>AssetInputStream</tt>.
     */
    private static final class Asset {
        public static final Field sField;

        static {
            try {
                sField = AssetInputStream.class.getDeclaredField("mAsset");
                sField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new Error(e);
            }
        }
    }
}
