package android.ext.barcode;

import static android.ext.util.DeviceUtils.DEVICE_DENSITY;
import static android.util.DisplayMetrics.DENSITY_DEFAULT;
import android.content.Context;
import android.ext.content.AsyncTask;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.DisplayMetrics;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * Class <tt>BarcodeEncoder</tt> used to encodes the contents to a barcode image.
 * <h3>Usage</h3>
 * <p>Here is an example:</p><pre>
 * final BarcodeEncoder encoder = new BarcodeEncoder()
 *     .setMargin(0)
 *     .setCharset("UTF-8")
 *     .setErrorCorrection(ErrorCorrectionLevel.H);
 *
 * encoder.startEncode(executor, contents, format, width, height, listener);</pre>
 * @author Garfield
 */
public class BarcodeEncoder {
    /* package */ final MultiFormatWriter mWriter;
    /* package */ final Map<EncodeHintType, Object> mHints;

    /**
     * Constructor
     */
    public BarcodeEncoder() {
        mWriter = new MultiFormatWriter();
        mHints  = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
    }

    /**
     * Returns the additional parameters with this encoder.
     * @return The additional parameters.
     */
    public final Map<EncodeHintType, Object> getHints() {
        return mHints;
    }

    /**
     * Sets the margin, in pixels, to use when generating the barcode.
     * @param margin The margin, in pixels.
     * @return This encoder.
     * @see EncodeHintType#MARGIN
     */
    public final BarcodeEncoder setMargin(int margin) {
        mHints.put(EncodeHintType.MARGIN, Integer.toString(margin));
        return this;
    }

    /**
     * Sets the exact version of QR code to be encoded.
     * @param version The version to set.
     * @return This encoder.
     * @see EncodeHintType#QR_VERSION
     */
    public final BarcodeEncoder setVersion(int version) {
        mHints.put(EncodeHintType.QR_VERSION, Integer.toString(version));
        return this;
    }

    /**
     * Sets the character encoding to use when encoding.
     * @param charsetName The charset name to set.
     * @return This encoder.
     * @see EncodeHintType#CHARACTER_SET
     */
    public final BarcodeEncoder setCharset(String charsetName) {
        mHints.put(EncodeHintType.CHARACTER_SET, charsetName);
        return this;
    }

    /**
     * Sets the required number of layers for an Aztec code.
     * @param layers The number of layers to set.
     * @return This encoder.
     * @see EncodeHintType#AZTEC_LAYERS
     */
    public final BarcodeEncoder setAztecLayers(int layers) {
        mHints.put(EncodeHintType.AZTEC_LAYERS, Integer.toString(layers));
        return this;
    }

    /**
     * Sets the matrix shape for Data Matrix.
     * @param hint The {@link SymbolShapeHint} to set.
     * @return This encoder.
     * @see EncodeHintType#DATA_MATRIX_SHAPE
     */
    public final BarcodeEncoder setDataMatrixShape(SymbolShapeHint hint) {
        mHints.put(EncodeHintType.DATA_MATRIX_SHAPE, hint);
        return this;
    }

    /**
     * Sets the error correction level.
     * @param level The error correction level to set.
     * @return This encoder.
     * @see EncodeHintType#ERROR_CORRECTION
     * @see #setErrorCorrection(ErrorCorrectionLevel)
     */
    public final BarcodeEncoder setErrorCorrection(int level) {
        mHints.put(EncodeHintType.ERROR_CORRECTION, Integer.toString(level));
        return this;
    }

    /**
     * Sets the error correction level.
     * @param level The {@link ErrorCorrectionLevel} to set.
     * @return This encoder.
     * @see EncodeHintType#ERROR_CORRECTION
     * @see #setErrorCorrection(int)
     */
    public final BarcodeEncoder setErrorCorrection(ErrorCorrectionLevel level) {
        mHints.put(EncodeHintType.ERROR_CORRECTION, level);
        return this;
    }

    /**
     * Encodes a barcode image with the specified <em>contents</em> synchronously.
     * <p><b>Note: This method will block the calling thread until it was returned.</b></p>
     * @param contents The contents to encode.
     * @param format The {@link BarcodeFormat} to encode.
     * @param width The preferred width in pixels.
     * @param height The preferred height in pixels.
     * @return The {@link BitMatrix} representing encoded barcode image if succeeded,
     * <tt>null</tt> otherwise.
     */
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height) {
        try {
            return mWriter.encode(contents, format, width, height, mHints);
        } catch (Exception e) {
            DebugUtils.__checkLogError(true, getClass().getName(), "Couldn't encode '" + contents + "' to a barcode image.", e);
            return null;
        }
    }

    /**
     * This method begins an asynchronous encode a barcode image with the specified <em>contents</em>.
     * @param contents The contents to encode.
     * @param format The {@link BarcodeFormat} to encode.
     * @param width The preferred width in pixels.
     * @param height The preferred height in pixels.
     * @param listener The {@link OnEncodeListener} used for being notified when the contents was encoded a barcode image.
     */
    public void startEncode(String contents, BarcodeFormat format, int width, int height, OnEncodeListener listener) {
        DebugUtils.__checkError(listener == null, "Invalid parameter - listener == null");
        /*
         * params - { BarcodeEncoder, contents, format, width, height, OnEncodeListener, null }
         */
        new EncodeTask().execute(this, contents, format, width, height, listener, null);
    }

    /**
     * Computes the QR Code size according to the <tt>DisplayMetrics</tt>.
     * @param context The <tt>Context</tt>.
     * @param scale The scale of the display size, expressed as a percentage
     * of the display size of the current device.
     * @return The QR Code size in pixels.
     */
    public static int computeQRCodeSize(Context context, float scale) {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int)(Math.min(dm.widthPixels, dm.heightPixels) * scale + 0.5f);
    }

    /**
     * Class <tt>EncodeTask</tt> is an implementation of an {@link AsyncTask}.
     */
    /* package */ static final class EncodeTask extends AsyncTask<Object, Object, Bitmap> {
        @Override
        protected Bitmap doInBackground(Object[] params) {
            /*
             * params - { BarcodeEncoder, contents, format, width, height, OnEncodeListener, BitMatrix }
             */
            DebugUtils.__checkError(params.length != 7, "params.length must be == 7");
            final BarcodeEncoder encoder = (BarcodeEncoder)params[0];
            final BitMatrix bitMatrix = encoder.encode((String)params[1], (BarcodeFormat)params[2], (int)params[3], (int)params[4]);
            params[6] = bitMatrix;
            return (bitMatrix != null ? ((OnEncodeListener)params[5]).convertToBitmap(bitMatrix, encoder.mHints) : null);
        }

        @Override
        protected void onPostExecute(Object[] params, Bitmap result) {
            ((OnEncodeListener)params[5]).onEncodeComplete((BitMatrix)params[6], result);
            Arrays.fill(params, null);  // Prevent memory leak.
        }
    }

    /**
     * Used for being notified when the contents was encoded a barcode image.
     */
    public static interface OnEncodeListener {
        /**
         * Called on the UI thread when the contents was encoded barcode image.
         * @param bitMatrix The {@link BitMatrix} representing encoded barcode
         * image, or <tt>null</tt> if encode failed.
         * @param result The {@link Bitmap} representing encoded barcode image,
         * or <tt>null</tt> if encode failed.
         * @see #convertToBitmap(BitMatrix, Map)
         */
        void onEncodeComplete(BitMatrix bitMatrix, Bitmap result);

        /**
         * Called on a background thread when the {@link BitMatrix} converts to a barcode image.
         * @param bitMatrix The <tt>BitMatrix</tt> to convert. Never <tt>null</tt>.
         * @param hints The additional parameters, passed earlier by {@link BarcodeEncoder#startEncode}.
         * @return A {@link Bitmap} of the barcode image, or <tt>null</tt> if convert failed.
         * @see BarcodeBuilder
         * @see #onEncodeComplete(BitMatrix, Bitmap)
         */
        default Bitmap convertToBitmap(BitMatrix bitMatrix, Map<EncodeHintType, ?> hints) {
            final int margin = (int)(20.0f * DEVICE_DENSITY / DENSITY_DEFAULT);
            DebugUtils.__checkDebug(true, "BarcodeEncoder", "deviceDensity = " + DEVICE_DENSITY + ", defaultDensity = " + DENSITY_DEFAULT + ", margin = " + margin);
            return new BarcodeBuilder(bitMatrix).config(Config.RGB_565).margins(margin).build();
        }
    }
}
