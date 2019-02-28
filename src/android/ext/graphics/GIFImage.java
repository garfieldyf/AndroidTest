package android.ext.graphics;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import android.content.Context;
import android.content.res.AssetManager.AssetInputStream;
import android.content.res.Resources;
import android.ext.util.ArrayUtils.ByteArrayPool;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.UriUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Build;
import android.util.Log;
import android.util.Printer;

/**
 * Class GIFImage
 * @author Garfield
 */
public final class GIFImage {
    /**
     * The native GIF image handle.
     */
    private final long mNativeImage;

    /**
     * Decodes a {@link GIFImage} from the <tt>Resources</tt>.
     * @param res The resource containing the GIF data.
     * @param id The resource id to be decoded.
     * @return The <tt>GIFImage</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decode(byte[], int, int)
     * @see #decode(InputStream, byte[])
     * @see #decode(Context, Object, byte[])
     */
    public static GIFImage decode(Resources res, int id) {
        InputStream is = null;
        try {
            is = res.openRawResource(id);
            return decodeStream(is, null);
        } catch (Exception e) {
            Log.e(GIFImage.class.getName(), "Couldn't decode resource ID #0x" + Integer.toHexString(id), e);
            return null;
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
        try {
            return decodeStream(is, tempStorage);
        } catch (Exception e) {
            Log.e(GIFImage.class.getName(), "Couldn't decode from - " + is.getClass().getName(), e);
            return null;
        }
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
        DebugUtils.__checkRange(offset, length, data.length);
        final long nativeImage = nativeDecodeArray(data, offset, length);
        return (nativeImage != 0 ? new GIFImage(nativeImage) : null);
    }

    /**
     * Decodes a {@link GIFImage} from the specified <em>uri</em>.
     * <h5>Accepts the following URI schemes:</h5>
     * <ul><li>path (no scheme)</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android_asset ({@link #SCHEME_FILE})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param context The <tt>Context</tt>.
     * @param uri The uri to decode.
     * @param tempStorage May be <tt>null</tt>. The temporary storage to use for decoding. Suggest 16K.
     * @return The <tt>GIFImage</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decode(Resources, int)
     * @see #decode(byte[], int, int)
     * @see #decode(InputStream, byte[])
     */
    public static GIFImage decode(Context context, Object uri, byte[] tempStorage) {
        InputStream is = null;
        try {
            is = UriUtils.openInputStream(context, uri);
            return decodeStream(is, tempStorage);
        } catch (Exception e) {
            Log.e(GIFImage.class.getName(), "Couldn't decode from - " + uri, e);
            return null;
        } finally {
            FileUtils.close(is);
        }
    }

    /**
     * Returns this GIF image's width.
     * @return The width in pixels.
     * @see #getHeight()
     */
    public final int getWidth() {
        return nativeGetWidth(mNativeImage);
    }

    /**
     * Returns this GIF image's height.
     * @return The height in pixels.
     * @see #getWidth()
     */
    public final int getHeight() {
        return nativeGetHeight(mNativeImage);
    }

    /**
     * Returns this GIF image frame count.
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
        return nativeGetFrameDelay(mNativeImage, frameIndex);
    }

    /**
     * Draw this GIF image the specified frame into the <em>bitmapCanvas</em>.
     * The bitmap canvas can be call {@link #createBitmapCanvas()} to create.
     * @param bitmapCanvas The mutable <tt>Bitmap</tt> used to draw the frame.
     * @param frameIndex The specified frame to be drawn. The valid frame range
     * is 0 to {@link #getFrameCount} - 1.
     * @return <tt>true</tt> if the frame draw succeeded, <tt>false</tt> otherwise.
     * @see #createBitmapCanvas()
     */
    public final boolean draw(Bitmap bitmapCanvas, int frameIndex) {
        DebugUtils.__checkError(!bitmapCanvas.isMutable(), "The bitmap canvas must be a mutable bitmap.");
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

    public final void dump(Printer printer) {
        printer.println(new StringBuilder(96)
            .append("GIFImage { nativePtr = 0x").append(Long.toHexString(mNativeImage))
            .append(", width = ").append(nativeGetWidth(mNativeImage))
            .append(", height = ").append(nativeGetHeight(mNativeImage))
            .append(", frameCount = ").append(nativeGetFrameCount(mNativeImage))
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
    }

    private static GIFImage decodeStream(InputStream is, byte[] tempStorage) throws Exception {
        final long nativeImage;
        if (is instanceof FileInputStream) {
            nativeImage = nativeDecodeFile(((FileInputStream)is).getFD());
        } else if (is instanceof AssetInputStream && Build.VERSION.SDK_INT < 28) {
            final AssetInputStream asset = (AssetInputStream)is;
            nativeImage = nativeDecodeAsset(Build.VERSION.SDK_INT > 20 ? asset.getNativeAsset() : asset.getAssetInt());
        } else if (tempStorage != null) {
            nativeImage = nativeDecodeStream(is, tempStorage);
        } else {
            nativeImage = nativeDecodeStream(is, tempStorage = ByteArrayPool.obtain());
            ByteArrayPool.recycle(tempStorage);
        }

        return (nativeImage != 0 ? new GIFImage(nativeImage) : null);
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
}
