package android.ext.barcode;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import android.graphics.Rect;
import android.os.AsyncTask;
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

/**
 * Class BarcodeDecoder
 * @author Garfield
 * @version 2.0
 */
public class BarcodeDecoder {
    private final MultiFormatReader mReader;
    private PlanarYUVLuminanceSource mSource;

    /**
     * Constructor
     */
    public BarcodeDecoder() {
        mReader = new MultiFormatReader();
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
    public Result decode(int[] pixels, int width, int height) {
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
    public Result decode(byte[] data, int width, int height, int left, int top, int right, int bottom) {
        return decode(mSource = new PlanarYUVLuminanceSource(data, width, height, left, top, right - left, bottom - top, false));
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
        new RGBDecodeTask(listener).executeOnExecutor(executor, pixels, width, height);
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
        new YUVDecodeTask(listener).executeOnExecutor(executor, data, width, height, clipBounds.left, clipBounds.top, clipBounds.right, clipBounds.bottom);
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
        new YUVDecodeTask(listener).executeOnExecutor(executor, data, width, height, left, top, right, bottom);
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
     * Returns the last decode the barcode image {@link PlanarYUVLuminanceSource}.
     * @return The <tt>PlanarYUVLuminanceSource</tt> or <tt>null</tt> if the decoded
     * the data not from the camera device.
     */
    public final PlanarYUVLuminanceSource getLuminanceSource() {
        return mSource;
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

    private synchronized Result decode(LuminanceSource source) {
        try {
            return mReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
        } catch (Exception e) {
            return null;
        } finally {
            mReader.reset();
        }
    }

    /**
     * Class <tt>YUVDecodeTask</tt> is an implementation of an {@link AsyncTask}.
     */
    private class YUVDecodeTask extends AsyncTask<Object, Object, Result> {
        private final WeakReference<OnDecodeListener> mListener;

        public YUVDecodeTask(OnDecodeListener listener) {
            mListener = new WeakReference<OnDecodeListener>(listener);
        }

        @Override
        protected Result doInBackground(Object... params) {
            return decode((byte[])params[0], (Integer)params[1], (Integer)params[2], (Integer)params[3], (Integer)params[4], (Integer)params[5], (Integer)params[6]);
        }

        @Override
        protected void onPostExecute(Result result) {
            final OnDecodeListener listener = mListener.get();
            if (listener != null) {
                listener.onDecodeComplete(result);
            }
        }
    }

    /**
     * Class <tt>RGBDecodeTask</tt> is an implementation of an {@link AsyncTask}.
     */
    private final class RGBDecodeTask extends YUVDecodeTask {
        public RGBDecodeTask(OnDecodeListener listener) {
            super(listener);
        }

        @Override
        protected Result doInBackground(Object... params) {
            return decode((int[])params[0], (Integer)params[1], (Integer)params[2]);
        }
    }

    /**
     * Class <tt>Builder</tt> to creates the barcode decoder hints.
     * <h2>Usage</h2>
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
         * Callback method to be invoked when the barcode image was decoded.
         * @param result The contents of the image, or <tt>null</tt> if decode failed.
         */
        void onDecodeComplete(Result result);
    }
}
