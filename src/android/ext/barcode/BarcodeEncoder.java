package android.ext.barcode;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Executor;
import android.content.Context;
import android.content.res.Resources;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Class BarcodeEncoder
 * @author Garfield
 * @version 2.0
 */
public class BarcodeEncoder {
    private Map<EncodeHintType, ?> mHints;
    private final MultiFormatWriter mWriter;

    /**
     * Constructor
     */
    public BarcodeEncoder() {
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
            Log.e(getClass().getName(), new StringBuilder("Couldn't encode '").append(contents).append("' to barcode image.").toString(), e);
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
        new EncodeTask(listener).executeOnExecutor(executor, contents, format, width, height, mHints);
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
        new EncodeTask(listener).executeOnExecutor(executor, contents, format, width, height, hints);
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
     * Class <tt>EncodeTask</tt> is an implementation of an {@link AsyncTask}.
     */
    private final class EncodeTask extends AsyncTask<Object, Object, Pair<BitMatrix, Bitmap>> {
        private OnEncodeListener mListener;

        public EncodeTask(OnEncodeListener listener) {
            DebugUtils.__checkError(listener == null, "listener == null");
            mListener = listener;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Pair<BitMatrix, Bitmap> doInBackground(Object... params) {
            final Map<EncodeHintType, ?> hints = (Map<EncodeHintType, ?>)params[4];
            final BitMatrix bitMatrix = encode((String)params[0], (BarcodeFormat)params[1], (Integer)params[2], (Integer)params[3], hints);
            return new Pair<BitMatrix, Bitmap>(bitMatrix, (bitMatrix != null ? mListener.convertToBitmap(bitMatrix, hints) : null));
        }

        @Override
        protected void onPostExecute(Pair<BitMatrix, Bitmap> result) {
            mListener.onEncodeComplete(result.first, result.second);
            mListener = null;   // Clears the listener to avoid potential memory leaks.
        }
    }

    /**
     * Class <tt>Builder</tt> to creates the barcode encoder hints.
     * <h2>Usage</h2>
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
     * Class <tt>BarcodeBuilder</tt> used to converts the {@link BitMatrix}
     * to a barcode image.
     * <h2>Usage</h2>
     * <p>Here is an example:</p><pre>
     * final Bitmap bitmap = new BarcodeBuilder(bitMatrix)
     *     .logo(logo)
     *     .gravity(Gravity.FILL)
     *     .margins(30, 30, 30, 30)
     *     .build();</pre>
     */
    public static final class BarcodeBuilder {
        private Config config;
        private int white;
        private int black;
        private int gravity;
        private Drawable logo;
        private int leftMargin;
        private int topMargin;
        private int rightMargin;
        private int bottomMargin;
        private final BitMatrix bitMatrix;

        /**
         * Constructor
         * @param bitMatrix The <tt>BitMatrix</tt> to convert.
         */
        public BarcodeBuilder(BitMatrix bitMatrix) {
            DebugUtils.__checkError(bitMatrix == null, "bitMatrix == null");
            this.bitMatrix = bitMatrix;
            this.white  = Color.WHITE;
            this.black  = Color.BLACK;
            this.config = Config.RGB_565;
        }

        /**
         * Sets the config to used to build the barcode image.
         * @param config The {@link Config} to set.
         * @return This builder.
         */
        public final BarcodeBuilder config(Config config) {
            this.config = config;
            return this;
        }

        /**
         * Sets the "white" color will be draw the barcode image.
         * @param white The "white" color to set.
         * @return This builder.
         */
        public final BarcodeBuilder white(int white) {
            this.white = white;
            return this;
        }

        /**
         * Sets the "black" color will be draw the barcode image.
         * @param black The "black" color to set.
         * @return This builder.
         */
        public final BarcodeBuilder black(int black) {
            this.black = black;
            return this;
        }

        /**
         * Sets the logo will be draw into the barcode image.
         * @param logo The <tt>Drawable</tt> to set.
         * @param gravity The gravity used to position/stretch
         * the <em>logo</em> within its bounds.
         * @return This builder.
         * @see Gravity
         */
        public final BarcodeBuilder logo(Drawable logo, int gravity) {
            this.logo = logo;
            this.gravity = gravity;
            return this;
        }

        /**
         * Sets the margins of the barcode image.
         * @param margin The margin size in pixels.
         * @return This builder.
         * @see #margins(Resources, int)
         * @see #margins(int, int, int, int)
         */
        public final BarcodeBuilder margins(int margin) {
            leftMargin = topMargin = rightMargin = bottomMargin = margin;
            return this;
        }

        /**
         * Sets the margins of the barcode image.
         * @param left The left margin size in pixels.
         * @param top The top margin size in pixels.
         * @param right The right margin size in pixels.
         * @param bottom The bottom margin size in pixels.
         * @return This builder.
         * @see #margins(int)
         * @see #margins(Resources, int)
         */
        public final BarcodeBuilder margins(int left, int top, int right, int bottom) {
            this.leftMargin   = left;
            this.topMargin    = top;
            this.rightMargin  = right;
            this.bottomMargin = bottom;
            return this;
        }

        /**
         * Sets the margins of the barcode image.
         * @param res The <tt>Resources</tt>.
         * @param id The resource id of the margin dimension.
         * @return This builder.
         * @see #margins(int)
         * @see #margins(int, int, int, int)
         */
        public final BarcodeBuilder margins(Resources res, int id) {
            return margins(res.getDimensionPixelOffset(id));
        }

        /**
         * Creates a barcode image with the arguments supplied to this builder.
         * @return A mutable {@link Bitmap} of the barcode image.
         */
        public final Bitmap build() {
            int left, top, right, bottom, offsetX, offsetY, width, height;
            final int[] bounds = bitMatrix.getEnclosingRectangle();
            if (bounds != null) {
                left    = bounds[0];
                top     = bounds[1];
                right   = left + bounds[2];
                bottom  = top  + bounds[3];
                offsetX = leftMargin;
                offsetY = topMargin;
                width   = bounds[2] + leftMargin + rightMargin;
                height  = bounds[3] + topMargin  + bottomMargin;
            } else {
                offsetX = offsetY = left = top = 0;
                width   = right   = bitMatrix.getWidth();
                height  = bottom  = bitMatrix.getHeight();
            }

            final int[] bitPixels = new int[right - left];
            final Bitmap result = Bitmap.createBitmap(width, height, config);
            result.eraseColor(white);

            for (int y = top; y < bottom; ++y, ++offsetY) {
                // Converts the bitMatrix current row bits to pixels.
                for (int x = left, i = 0; x < right; ++x, ++i) {
                    bitPixels[i] = (bitMatrix.get(x, y) ? black : white);
                }

                // Sets the result current row pixels from [offsetX, offsetY] to bit width.
                result.setPixels(bitPixels, 0, width, offsetX, offsetY, bitPixels.length, 1);
            }

            if (logo != null) {
                final Canvas canvas = new Canvas(result);
                drawLogo(canvas, width, height);
                canvas.setBitmap(null);
            }

            return result;
        }

        private void drawLogo(Canvas canvas, int width, int height) {
            final int size = (int)(width * 0.25f);
            final int left = (width - size) / 2;
            final int top  = (height - size) / 2;

            final int logoWidth  = logo.getIntrinsicWidth();
            final int logoHeight = logo.getIntrinsicHeight();
            final Rect container = new Rect(left, top, left + size, top + size);

            if (logoWidth > 0 && logoHeight > 0) {
                final Rect bounds = new Rect();
                Gravity.apply(gravity, logoWidth, logoHeight, container, 0, 0, bounds);
                logo.setBounds(bounds);
            } else {
                logo.setBounds(container);
            }

            logo.draw(canvas);
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
