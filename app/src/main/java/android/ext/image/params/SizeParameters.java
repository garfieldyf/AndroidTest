package android.ext.image.params;

import static android.ext.util.DeviceUtils.DEVICE_DENSITY;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.graphics.BitmapFactory.Options;
import android.util.AttributeSet;
import android.util.Printer;
import android.view.View;

/**
 * Class <tt>SizeParameters</tt> is an implementation of a {@link Parameters}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;SizeParameters xmlns:android="http://schemas.android.com/apk/res/android"
 *      android:minWidth="200dp"
 *      android:minHeight="300dp" /&gt;</pre>
 * @author Garfield
 */
public class SizeParameters extends Parameters {
    private static final int[] SIZE_PARAMETERS_ATTRS = {
        android.R.attr.minWidth,
        android.R.attr.minHeight,
    };

    private static final int MIN_WIDTH_INDEX  = 0;
    private static final int MIN_HEIGHT_INDEX = 1;

    /**
     * The minimum width to decode, in pixels.
     */
    private final int minWidth;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #SizeParameters(int, int)
     */
    public SizeParameters(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, SIZE_PARAMETERS_ATTRS);
        this.minWidth = a.getDimensionPixelOffset(MIN_WIDTH_INDEX /* android.R.attr.minWidth */, 0);
        this.value = a.getDimensionPixelOffset(MIN_HEIGHT_INDEX /* android.R.attr.minHeight */, 0);
        a.recycle();
    }

    /**
     * Constructor
     * @param minWidth The minimum width to decode, in pixels.
     * @param minHeight The minimum height to decode, in pixels.
     * @see #SizeParameters(Context, AttributeSet)
     */
    public SizeParameters(int minWidth, int minHeight) {
        super(Integer.valueOf(minHeight));
        this.minWidth = minWidth;
    }

    @Override
    public int computeByteCount(Options opts) {
        return computeByteCountImpl(opts);
    }

    @Override
    public void computeSampleSize(Object target, Options opts) {
        /*
         * Scale width and height.
         *      scaleX = opts.outWidth  / width;
         *      scaleY = opts.outHeight / height;
         *      scale  = max(scaleX, scaleY);
         */
        DebugUtils.__checkError(opts.outWidth <= 0 || opts.outHeight <= 0, "opts.outWidth(" + opts.outWidth + ") and opts.outHeight(" + opts.outHeight + ") must be > 0");
        DebugUtils.__checkError(opts.inDensity != 0 || opts.inTargetDensity != 0, "opts.inDensity(" + opts.inDensity + ") and opts.inTargetDensity(" + opts.inTargetDensity + ") must be == 0");
        final int width, height, minHeight = (int)value;
        if (target instanceof View) {
            final View view = (View)target;
            width  = Math.max(view.getWidth(),  minWidth);
            height = Math.max(view.getHeight(), minHeight);
        } else {
            width  = minWidth;
            height = minHeight;
        }

        DebugUtils.__checkWarning(width <= 0 || height <= 0, "SizeParameters", "The image will be decode original size (width = " + width + ", height = " + height + ").");
        if (width > 0 && height > 0 && opts.outWidth > width && opts.outHeight > height) {
            final float scale = Math.max((float)opts.outWidth / width, (float)opts.outHeight / height);
            opts.inTargetDensity = DEVICE_DENSITY;
            opts.inDensity = (int)(DEVICE_DENSITY * scale + 0.5f);
        }
    }

    @Override
    public void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { minWidth = ").append(minWidth)
            .append(", minHeight = ").append(value)
            .append(", deviceDensity = ").append(DeviceUtils.toDensity(DEVICE_DENSITY))
            .append(" }").toString());
    }
}
