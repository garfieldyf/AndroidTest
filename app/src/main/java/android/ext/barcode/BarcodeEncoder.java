package android.ext.barcode;

import android.content.Context;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Class BarcodeEncoder
 * @author Garfield
 */
public class BarcodeEncoder {
    private Map<EncodeHintType, ?> mHints;
    private final MultiFormatWriter mWriter;

    /**
     * Constructor
     * @see #BarcodeEncoder(Map)
     */
    public BarcodeEncoder() {
        mWriter = new MultiFormatWriter();
    }

    /**
     * Constructor
     * @param hints The additional parameters to supply to this encoder.
     * @see #BarcodeEncoder()
     * @see Builder
     */
    public BarcodeEncoder(Map<EncodeHintType, ?> hints) {
        mHints  = hints;
        mWriter = new MultiFormatWriter();
    }

    /**
     * Encodes an barcode image with the specified <em>contents</em> synchronously.
     * <p><b>Note: This method will block the calling thread until it was returned.</b></p>
     * @param contents The contents to encode.
     * @param format The {@link BarcodeFormat} to encode.
     * @param width The preferred width in pixels.
     * @param height The preferred height in pixels.
     * @return The {@link BitMatrix} representing encoded barcode image if succeeded,
     * <tt>null</tt> otherwise.
     * @see #encode(String, BarcodeFormat, int, int, Map)
     * @see BarcodeBuilder
     */
    public final BitMatrix encode(String contents, BarcodeFormat format, int width, int height) {
        return encode(contents, format, width, height, mHints);
    }

    /**
     * Encodes an barcode image with the specified <em>contents</em> synchronously.
     * <p><b>Note: This method will block the calling thread until it was returned.</b></p>
     * @param contents The contents to encode.
     * @param format The {@link BarcodeFormat} to encode.
     * @param width The preferred width in pixels.
     * @param height The preferred height in pixels.
     * @param hints The additional parameters to supply to this encoder.
     * @return The {@link BitMatrix} representing encoded barcode image if succeeded,
     * <tt>null</tt> otherwise.
     * @see #encode(String, BarcodeFormat, int, int)
     * @see BarcodeBuilder
     */
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) {
        try {
            return mWriter.encode(contents, format, width, height, hints);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't encode '" + contents + "' to a barcode image.", e);
            return null;
        }
    }

    /**
     * This method begins an asynchronous encode an barcode image with the specified <em>contents</em>.
     * @param contents The contents to encode.
     * @param format The {@link BarcodeFormat} to encode.
     * @param width The preferred width in pixels.
     * @param height The preferred height in pixels.
     * @param executor The <tt>Executor</tt> to executing encode.
     * @param listener The {@link OnEncodeListener} used for being
     * notified when the contents was encoded an barcode image.
     * @see #startEncode(String, BarcodeFormat, int, int, Map, Executor, OnEncodeListener)
     */
    public final void startEncode(String contents, BarcodeFormat format, int width, int height, Executor executor, OnEncodeListener listener) {
        DebugUtils.__checkError(executor == null, "executor == null");
        new EncodeTask().executeOnExecutor(executor, new Encoder(this, contents, format, width, height, mHints, listener));
    }

    /**
     * This method begins an asynchronous encode an barcode image with the specified <em>contents</em>.
     * @param contents The contents to encode.
     * @param format The {@link BarcodeFormat} to encode.
     * @param width The preferred width in pixels.
     * @param height The preferred height in pixels.
     * @param hints The additional parameters to supply to this encoder.
     * @param executor The <tt>Executor</tt> to executing encode.
     * @param listener The {@link OnEncodeListener} used for being
     * notified when the contents was encoded an barcode image.
     * @see #startEncode(String, BarcodeFormat, int, int, Executor, OnEncodeListener)
     */
    public final void startEncode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints, Executor executor, OnEncodeListener listener) {
        DebugUtils.__checkError(executor == null, "executor == null");
        new EncodeTask().executeOnExecutor(executor, new Encoder(this, contents, format, width, height, hints, listener));
    }

    /**
     * Sets the additional parameters to supply to this encoder.
     * @param hints The parameters to set.
     * @see Builder
     */
    public final void setHints(Map<EncodeHintType, ?> hints) {
        mHints = hints;
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
     * Class <tt>Encoder</tt> used to encode the contents to a barcode image.
     */
    private static final class Encoder {
        /* package */ Bitmap result;
        /* package */ BitMatrix bitMatrix;
        /* package */ final int width;
        /* package */ final int height;
        /* package */ final String contents;
        /* package */ final BarcodeFormat format;
        /* package */ final BarcodeEncoder encoder;
        /* package */ final OnEncodeListener listener;
        /* package */ final Map<EncodeHintType, ?> hints;

        /* package */ Encoder(BarcodeEncoder encoder, String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints, OnEncodeListener listener) {
            this.hints    = hints;
            this.width    = width;
            this.height   = height;
            this.format   = format;
            this.encoder  = encoder;
            this.contents = contents;
            this.listener = listener;
            DebugUtils.__checkError(listener == null, "listener == null");
        }

        /* package */ final Encoder encode() {
            bitMatrix = encoder.encode(contents, format, width, height, hints);
            if (bitMatrix != null) {
                result = listener.convertToBitmap(bitMatrix, hints);
            }

            return this;
        }
    }

    /**
     * Class <tt>EncodeTask</tt> is an implementation of an {@link AsyncTask}.
     */
    /* package */ static final class EncodeTask extends AsyncTask<Encoder, Object, Encoder> {
        @Override
        protected Encoder doInBackground(Encoder[] encoders) {
            return encoders[0].encode();
        }

        @Override
        protected void onPostExecute(Encoder encoder) {
            encoder.listener.onEncodeComplete(encoder.bitMatrix, encoder.result);
        }
    }

    /**
     * Class <tt>Builder</tt> to creates the barcode encoder hints.
     * <h3>Usage</h3>
     * <p>Here is an example:</p><pre>
     * final Map&lt;EncodeHintType, Object&gt; hints = new BarcodeEncoder.Builder()
     *     .charset("UTF-8")
     *     .margin(0)
     *     .errorCorrection(ErrorCorrectionLevel.H)
     *     .build();</pre>
     */
    public static final class Builder {
        private final Map<EncodeHintType, Object> mHints;

        /**
         * Constructor
         */
        public Builder() {
            mHints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        }

        /**
         * Sets the margin, in pixels, to use when generating the barcode.
         * @param margin The margin, in pixels.
         * @return This builder.
         * @see EncodeHintType#MARGIN
         */
        public final Builder margin(int margin) {
            mHints.put(EncodeHintType.MARGIN, Integer.toString(margin));
            return this;
        }

        /**
         * Sets the exact version of QR code to be encoded.
         * @param version The version to set.
         * @return This builder.
         * @see EncodeHintType#QR_VERSION
         */
        public final Builder version(int version) {
            mHints.put(EncodeHintType.QR_VERSION, Integer.toString(version));
            return this;
        }

        /**
         * Sets the required number of layers for an Aztec code.
         * @param layers The number of layers to set.
         * @return This builder.
         * @see EncodeHintType#AZTEC_LAYERS
         */
        public final Builder aztecLayers(int layers) {
            mHints.put(EncodeHintType.AZTEC_LAYERS, Integer.toString(layers));
            return this;
        }

        /**
         * Sets the character encoding to use when encoding.
         * @param charsetName The charset name to set.
         * @return This builder.
         * @see EncodeHintType#CHARACTER_SET
         */
        public final Builder charset(String charsetName) {
            mHints.put(EncodeHintType.CHARACTER_SET, charsetName);
            return this;
        }

        /**
         * Sets the matrix shape for Data Matrix.
         * @param hint The {@link SymbolShapeHint} to set.
         * @return This builder.
         * @see EncodeHintType#DATA_MATRIX_SHAPE
         */
        public final Builder dataMatrixShape(SymbolShapeHint hint) {
            mHints.put(EncodeHintType.DATA_MATRIX_SHAPE, hint);
            return this;
        }

        /**
         * Sets the error correction level.
         * @param level The {@link ErrorCorrectionLevel} to set.
         * @return This builder.
         * @see EncodeHintType#ERROR_CORRECTION
         * @see #errorCorrection(int)
         */
        public final Builder errorCorrection(ErrorCorrectionLevel level) {
            mHints.put(EncodeHintType.ERROR_CORRECTION, level);
            return this;
        }

        /**
         * Sets the error correction level.
         * @param level The error correction level to set.
         * @return This builder.
         * @see EncodeHintType#ERROR_CORRECTION
         * @see #errorCorrection(ErrorCorrectionLevel)
         */
        public final Builder errorCorrection(int level) {
            mHints.put(EncodeHintType.ERROR_CORRECTION, Integer.toString(level));
            return this;
        }

        /**
         * Creates a barcode encoder hints with the arguments supplied to this builder.
         * @return The barcode encoder hints.
         */
        public final Map<EncodeHintType, Object> build() {
            return mHints;
        }
    }

    /**
     * Used for being notified when the contents was encoded an barcode image.
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
        Bitmap convertToBitmap(BitMatrix bitMatrix, Map<EncodeHintType, ?> hints);
    }
}
