package android.ext.barcode;

import android.content.res.Resources;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import com.google.zxing.common.BitMatrix;

/**
 * Class <tt>BarcodeBuilder</tt> used to converts the {@link BitMatrix} to a barcode image.
 * <h2>Usage</h2>
 * <p>Here is an example:</p><pre>
 * final Bitmap bitmap = new BarcodeBuilder(bitMatrix)
 *     .logo(logo)
 *     .gravity(Gravity.FILL)
 *     .margins(30, 30, 30, 30)
 *     .build();</pre>
 * @author Garfield
 * @version 2.0
 */
public final class BarcodeBuilder {
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

        final int[] rowPixels = new int[right - left];
        final Bitmap result = Bitmap.createBitmap(width, height, config);
        result.eraseColor(white);

        for (int y = top; y < bottom; ++y, ++offsetY) {
            // Converts the bitMatrix current row bits to pixels.
            for (int x = left, i = 0; x < right; ++x, ++i) {
                rowPixels[i] = (bitMatrix.get(x, y) ? black : white);
            }

            // Sets the result current row pixels from [offsetX, offsetY] to row width.
            result.setPixels(rowPixels, 0, width, offsetX, offsetY, rowPixels.length, 1);
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
