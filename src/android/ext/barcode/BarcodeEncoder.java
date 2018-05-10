package android.ext.barcode;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Executor;
import android.content.Context;
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
     */
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height) {
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
        private final WeakReference<OnEncodeListener> mListener;

        public EncodeTask(OnEncodeListener listener) {
            mListener = new WeakReference<OnEncodeListener>(listener);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Pair<BitMatrix, Bitmap> doInBackground(Object... params) {
            final OnEncodeListener listener = mListener.get();
            Pair<BitMatrix, Bitmap> result  = null;
            if (listener != null) {
                final Map<EncodeHintType, ?> hints = (Map<EncodeHintType, ?>)params[4];
                final BitMatrix bitMatrix = encode((String)params[0], (BarcodeFormat)params[1], (Integer)params[2], (Integer)params[3], hints);
                result = new Pair<BitMatrix, Bitmap>(bitMatrix, (bitMatrix != null ? listener.convertToBitmap(bitMatrix, hints) : null));
            }

            return result;
        }

        @Override
        protected void onPostExecute(Pair<BitMatrix, Bitmap> result) {
            final OnEncodeListener listener = mListener.get();
            if (listener != null) {
                listener.onEncodeComplete(result.first, result.second);
            }
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
     *     .padding(30, 30, 30, 30)
     *     .build();</pre>
     */
    public static final class BarcodeBuilder {
        private Config config;
        private int white;
        private int black;
        private int gravity;
        private Drawable logo;
        private int paddingLeft;
        private int paddingTop;
        private int paddingRight;
        private int paddingBottom;
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
         * Sets the padding of the barcode image.
         * @param left The left padding in pixels.
         * @param top The top padding in pixels.
         * @param right The right padding in pixels.
         * @param bottom The bottom padding in pixels.
         * @return This builder.
         */
        public final BarcodeBuilder padding(int left, int top, int right, int bottom) {
            this.paddingLeft   = left;
            this.paddingTop    = top;
            this.paddingRight  = right;
            this.paddingBottom = bottom;
            return this;
        }

        /**
         * Creates a barcode image with the arguments supplied to this builder.
         * @return The instance of barcode image.
         */
        public final Bitmap build() {
            int left, top, right, bottom, inputX, inputY, width, height;
            final int[] bounds = bitMatrix.getEnclosingRectangle();
            if (bounds != null) {
                left   = bounds[0];
                top    = bounds[1];
                right  = left + bounds[2];
                bottom = top  + bounds[3];
                inputX = paddingLeft;
                inputY = paddingTop;
                width  = bounds[2] + paddingLeft + paddingRight;
                height = bounds[3] + paddingTop + paddingBottom;
            } else {
                inputX = inputY = left = top = 0;
                width  = right  = bitMatrix.getWidth();
                height = bottom = bitMatrix.getHeight();
            }

            final int[] rowPixels = new int[width];
            final Bitmap result = Bitmap.createBitmap(width, height, config);
            result.eraseColor(white);

            for (int y = top, start = inputX + right - left; y < bottom; ++y, ++inputY) {
                // Fills the padding area ([0 - inputX], [inputY - width]) to the 'white' color.
                Arrays.fill(rowPixels, 0, inputX, white);
                Arrays.fill(rowPixels, start, width, white);

                // Converts the barcode image current row bits to pixels.
                for (int x = left, outputX = inputX; x < right; ++x, ++outputX) {
                    rowPixels[outputX] = (bitMatrix.get(x, y) ? black : white);
                }

                // Copy the result current row pixels from rowPixels array.
                result.setPixels(rowPixels, 0, width, 0, inputY, width, 1);
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
         * Called on a background thread when the {@link BitMatrix} converts to {@link Bitmap}.
         * @param bitMatrix The <tt>BitMatrix</tt> to convert, or <tt>null</tt> if encode failed.
         * @param hints The additional parameters, passed earlier by {@link BarcodeEncoder#startEncode}.
         * @return The <tt>Bitmap</tt>, or <tt>null</tt> if convert failed.
         * @see BarcodeBuilder
         * @see #onEncodeComplete(BitMatrix, Bitmap)
         */
        Bitmap convertToBitmap(BitMatrix bitMatrix, Map<EncodeHintType, ?> hints);
    }
}
