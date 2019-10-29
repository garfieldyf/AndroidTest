package android.ext.barcode;

import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Pair;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.HybridBinarizer;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Class BarcodeDecoder
 * @author Garfield
 */
public class BarcodeDecoder {
    private final MultiFormatReader mReader;

    /**
     * Constructor
     * @see #BarcodeDecoder(Map)
     */
    public BarcodeDecoder() {
        mReader = new MultiFormatReader();
    }

    /**
     * Constructor
     * @param hints The set of hints to use for subsequent calls to <em>decode</em>.
     * @see #BarcodeDecoder()
     * @see Builder
     */
    public BarcodeDecoder(Map<DecodeHintType, ?> hints) {
        mReader = new MultiFormatReader();
        mReader.setHints(hints);
    }

    /**
     * Decodes the barcode image from the bitmap pixels synchronously.
     * <p><b>Note: This method will block the calling thread until it was returned.</b></p>
     * @param pixels The array of colors representing the pixels of the bitmap.
     * @param width The width of the bitmap in pixels.
     * @param height The height of the bitmap in pixels.
     * @return The contents of the image if succeeded, <tt>null</tt> otherwise.
     * @see #decode(byte[], int, int, Rect)
     * @see #decode(byte[], int, int, int, int, int, int)
     */
    public final Result decode(int[] pixels, int width, int height) {
        return decode(new RGBLuminanceSource(width, height, pixels));
    }

    /**
     * Decodes the barcode image from the camera device synchronously.
     * <p><b>Note: This method will block the calling thread until it was returned.</b></p>
     * @param data The array of YUV data returned from the camera device.
     * @param width The <em>data</em> width in pixels.
     * @param height The <em>data</em> height in pixels.
     * @param clipBounds The <tt>Rect</tt> expressing the barcode clip area.
     * @return The contents of the image if succeeded, <tt>null</tt> otherwise.
     * @see #decode(int[], int, int)
     * @see #decode(byte[], int, int, int, int, int, int)
     */
    public final Result decode(byte[] data, int width, int height, Rect clipBounds) {
        return decode(data, width, height, clipBounds.left, clipBounds.top, clipBounds.right, clipBounds.bottom);
    }

    /**
     * Decodes the barcode image from the camera device synchronously.
     * <p><b>Note: This method will block the calling thread until it was returned.</b></p>
     * @param data The array of YUV data returned from the camera device.
     * @param width The <em>data</em> width in pixels.
     * @param height The <em>data</em> height in pixels.
     * @param left The left side of the rectangle expressing the barcode clip area.
     * @param top The top side of the rectangle expressing the barcode clip area.
     * @param right The right side of the rectangle expressing the barcode clip area.
     * @param bottom The bottom side of the rectangle expressing the barcode clip area.
     * @return The contents of the image if succeeded, <tt>null</tt> otherwise.
     * @see #decode(int[], int, int)
     * @see #decode(byte[], int, int, Rect)
     */
    public final Result decode(byte[] data, int width, int height, int left, int top, int right, int bottom) {
        return decode(new PlanarYUVLuminanceSource(data, width, height, left, top, right - left, bottom - top, false));
    }

    /**
     * This method begins an asynchronous decode the barcode image from the bitmap pixels.
     * @param pixels The array of colors representing the pixels of the bitmap.
     * @param width The width of the bitmap in pixels.
     * @param height The height of the bitmap in pixels.
     * @param executor The <tt>Executor</tt> to executing decode.
     * @param listener The {@link OnDecodeListener} used for being notified
     * when the barcode image was decoded.
     * @see #startDecode(byte[], int, int, Rect, Executor, OnDecodeListener)
     * @see #startDecode(byte[], int, int, int, int, int, int, Executor, OnDecodeListener)
     */
    public final void startDecode(int[] pixels, int width, int height, Executor executor, OnDecodeListener listener) {
        new DecodeTask(listener).executeOnExecutor(executor, new RGBLuminanceSource(width, height, pixels));
    }

    /**
     * This method begins an asynchronous decode the barcode image from the camera device.
     * @param data The array of YUV data returned from the camera device.
     * @param width The <em>data</em> width in pixels.
     * @param height The <em>data</em> height in pixels.
     * @param clipBounds The <tt>Rect</tt> expressing the barcode clip area.
     * @param executor The <tt>Executor</tt> to executing decode.
     * @param listener The {@link OnDecodeListener} used for being notified when
     * the barcode image was decoded.
     * @see #startDecode(int[], int, int, Executor, OnDecodeListener)
     * @see #startDecode(byte[], int, int, int, int, int, int, Executor, OnDecodeListener)
     */
    public final void startDecode(byte[] data, int width, int height, Rect clipBounds, Executor executor, OnDecodeListener listener) {
        new DecodeTask(listener).executeOnExecutor(executor, new PlanarYUVLuminanceSource(data, width, height, clipBounds.left, clipBounds.top, clipBounds.width(), clipBounds.height(), false));
    }

    /**
     * This method begins an asynchronous decode the barcode image from the camera device.
     * @param data The array of YUV data returned from the camera device.
     * @param width The <em>data</em> width in pixels.
     * @param height The <em>data</em> height in pixels.
     * @param left The left side of the rectangle expressing the barcode clip area.
     * @param top The top side of the rectangle expressing the barcode clip area.
     * @param right The right side of the rectangle expressing the barcode clip area.
     * @param bottom The bottom side of the rectangle expressing the barcode clip area.
     * @param executor The <tt>Executor</tt> to executing decode.
     * @param listener The {@link OnDecodeListener} used for being notified when the
     * barcode image was decoded.
     * @see #startDecode(int[], int, int, Executor, OnDecodeListener)
     * @see #startDecode(byte[], int, int, Rect, Executor, OnDecodeListener)
     */
    public final void startDecode(byte[] data, int width, int height, int left, int top, int right, int bottom, Executor executor, OnDecodeListener listener) {
        new DecodeTask(listener).executeOnExecutor(executor, new PlanarYUVLuminanceSource(data, width, height, left, top, right - left, bottom - top, false));
    }

    /**
     * Adds state to this decoder. By setting the hints once, subsequent calls
     * to {@link #decode} can reuse the same set of readers without reallocating
     * memory. This is important for performance in continuous scan clients.
     * @param hints The set of hints to use for subsequent calls to <em>decode</em>.
     * @see Builder
     */
    public final void setHints(Map<DecodeHintType, ?> hints) {
        mReader.setHints(hints);
    }

    /**
     * Returns the raw text from the decoded {@link Result}. The returned text removing the
     * white space characters from the beginning and end of the raw text.
     * @param result The decoded <tt>Result</tt>.
     * @return The raw text encoded by the barcode, or <tt>null</tt> if the <em>result</em>
     * is <tt>null</tt> or has no raw text.
     */
    public static String resultToString(Result result) {
        if (result != null) {
            final String text = result.getText();
            if (text != null) {
                return text.trim();
            }
        }

        return null;
    }

    /**
     * Returns a grey-scale luminance bitmap from the specified <em>source</em>.
     * @param source The {@link LuminanceSource} to create.
     * @return A grey-scale bitmap, or <tt>null</tt> if the <em>source</em> could't to create.
     */
    public static Bitmap createLuminanceBitmap(LuminanceSource source) {
        if (source instanceof RGBLuminanceSource) {
            return createRGBLuminanceBitmap((RGBLuminanceSource)source);
        } else if (source instanceof PlanarYUVLuminanceSource) {
            final PlanarYUVLuminanceSource yuvSource = (PlanarYUVLuminanceSource)source;
            return Bitmap.createBitmap(yuvSource.renderThumbnail(), yuvSource.getThumbnailWidth(), yuvSource.getThumbnailHeight(), Config.ARGB_8888);
        } else {
            return null;
        }
    }

    /**
     * Decodes the barcode image using the <em>source</em> provided.
     * @param source The {@link LuminanceSource} to decode.
     * @return The contents of the image if succeeded, <tt>null</tt> otherwise.
     */
    protected synchronized Result decode(LuminanceSource source) {
        try {
            return mReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
        } catch (Exception e) {
            return null;
        } finally {
            mReader.reset();
        }
    }

    /**
     * Returns a grey-scale luminance bitmap from the specified <em>source</em>.
     */
    private static Bitmap createRGBLuminanceBitmap(RGBLuminanceSource source) {
        final int width = source.getWidth(), height = source.getHeight();
        final byte[] matrix = source.getMatrix();
        final Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        final int[] rowPixels = new int[width];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                final int gray = (matrix[y * width + x] & 0xff);
                rowPixels[x] = Color.rgb(gray, gray, gray);
            }

            result.setPixels(rowPixels, 0, width, 0, y, width, 1);
        }

        return result;
    }

    /**
     * Class <tt>DecodeTask</tt> is an implementation of an {@link AsyncTask}.
     */
    private final class DecodeTask extends AsyncTask<LuminanceSource, Object, Pair<LuminanceSource, Result>> {
        private OnDecodeListener mListener;

        public DecodeTask(OnDecodeListener listener) {
            DebugUtils.__checkError(listener == null, "listener == null");
            mListener = listener;
        }

        @Override
        protected Pair<LuminanceSource, Result> doInBackground(LuminanceSource... params) {
            final LuminanceSource source = params[0];
            return new Pair<LuminanceSource, Result>(source, decode(source));
        }

        @Override
        protected void onPostExecute(Pair<LuminanceSource, Result> result) {
            mListener.onDecodeComplete(result.first, result.second);
            mListener = null;   // Prevent memory leak.
        }
    }

    /**
     * Class <tt>Builder</tt> to creates the barcode decoder hints.
     * <h3>Usage</h3>
     * <p>Here is an example:</p><pre>
     * final Map&lt;DecodeHintType, Object&gt; hints = new BarcodeDecoder.Builder()
     *     .charset("UTF-8")
     *     .formats(BarcodeFormat.QR_CODE)
     *     .build();</pre>
     */
    public static final class Builder {
        private final Map<DecodeHintType, Object> mHints;

        /**
         * Constructor
         */
        public Builder() {
            mHints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        }

        /**
         * Sets spend more time to try to find a barcode.
         * @return This builder.
         * @see DecodeHintType#TRY_HARDER
         */
        public final Builder tryHarder() {
            mHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            return this;
        }

        /**
         * Sets image is a pure monochrome image of a barcode.
         * @return This builder.
         * @see DecodeHintType#PURE_BARCODE
         */
        public final Builder pureBarcode() {
            mHints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
            return this;
        }

        /**
         * Sets the character encoding to use when decoding.
         * @param charsetName The charset name to set.
         * @return This builder.
         * @see DecodeHintType#CHARACTER_SET
         */
        public final Builder charset(String charsetName) {
            mHints.put(DecodeHintType.CHARACTER_SET, charsetName);
            return this;
        }

        /**
         * Sets allowed lengths of encoded data.
         * @param lengths An array of lengths to set.
         * @return This builder.
         * @see DecodeHintType#ALLOWED_LENGTHS
         */
        public final Builder allowedLengths(int... lengths) {
            mHints.put(DecodeHintType.ALLOWED_LENGTHS, lengths);
            return this;
        }

        /**
         * Sets the {@link ResultPointCallback} when the <tt>ResultPoint</tt> is found.
         * @param callback The <tt>ResultPointCallback</tt> to set.
         * @return This builder.
         * @see DecodeHintType#NEED_RESULT_POINT_CALLBACK
         */
        public final Builder callback(ResultPointCallback callback) {
            mHints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, callback);
            return this;
        }

        /**
         * Sets an array of possible {@link BarcodeFormat}s.
         * @param formats An array of <tt>BarcodeFormats</tt>.
         * @return This builder.
         * @see DecodeHintType#POSSIBLE_FORMATS
         * @see #formats(List)
         */
        public final Builder formats(BarcodeFormat... formats) {
            mHints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(formats));
            return this;
        }

        /**
         * Sets a <tt>List</tt> of possible {@link BarcodeFormat}s.
         * @param formats A <tt>List</tt> of <tt>BarcodeFormats</tt>.
         * @return This builder.
         * @see DecodeHintType#POSSIBLE_FORMATS
         * @see #formats(BarcodeFormat[])
         */
        public final Builder formats(List<BarcodeFormat> formats) {
            mHints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
            return this;
        }

        /**
         * Sets allowed extension lengths for EAN or UPC barcodes.
         * @param lengths An array of extension lengths to set.
         * @return This builder.
         * @see DecodeHintType#ALLOWED_EAN_EXTENSIONS
         */
        public final Builder extensions(int... lengths) {
            mHints.put(DecodeHintType.ALLOWED_EAN_EXTENSIONS, lengths);
            return this;
        }

        /**
         * Creates a barcode decoder hints with the arguments supplied to this builder.
         * @return The barcode decoder hints.
         */
        public final Map<DecodeHintType, Object> build() {
            return mHints;
        }
    }

    /**
     * Used for being notified when the barcode image was decoded.
     */
    public static interface OnDecodeListener {
        /**
         * Called on the UI thread when the barcode image was decoded.
         * @param source The {@link LuminanceSource} to decode.
         * @param result The contents of the image if succeeded,
         * <tt>null</tt> otherwise.
         */
        void onDecodeComplete(LuminanceSource source, Result result);
    }
}
