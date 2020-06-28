package android.ext.image.params;

import static android.util.DisplayMetrics.DENSITY_DEVICE;
import static android.util.DisplayMetrics.DENSITY_DEVICE_STABLE;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Printer;
import android.view.View;

/**
 * Class <tt>SizeParameters</tt> is an implementation of a {@link Parameters}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;SizeParameters
 *      xmlns:android="http://schemas.android.com/apk/res/android"
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      android:minWidth="200dp"
 *      android:minHeight="300dp"
 *      app:config="[ argb_8888 | rgb_565 ]" /&gt;</pre>
 * @author Garfield
 */
@SuppressWarnings("deprecation")
public class SizeParameters extends Parameters {
    private static final int[] SIZE_PARAMETERS_ATTRS = {
        android.R.attr.minWidth,
        android.R.attr.minHeight,
    };

    private static final int MIN_WIDTH  = 0;
    private static final int MIN_HEIGHT = 1;

    /**
     * The default {@link Parameters} (sample size = 1,
     * config = RGB_565) used to decode the bitmap.
     */
    public static final Parameters defaultParameters;

    /**
     * The minimum width to decode, in pixels.
     */
    private final int minWidth;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #SizeParameters(Config, int, int)
     */
    public SizeParameters(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, SIZE_PARAMETERS_ATTRS);
        this.minWidth = a.getDimensionPixelOffset(MIN_WIDTH /* android.R.attr.minWidth */, 0);
        this.value = a.getDimensionPixelOffset(MIN_HEIGHT /* android.R.attr.minHeight */, 0);
        a.recycle();
    }

    /**
     * Constructor
     * @param config The {@link Config} to decode.
     * @param minWidth The minimum width to decode, in pixels.
     * @param minHeight The minimum height to decode, in pixels.
     * @see #SizeParameters(Context, AttributeSet)
     */
    public SizeParameters(Config config, int minWidth, int minHeight) {
        super(minHeight, config);
        this.minWidth = minWidth;
        DebugUtils.__checkDebug(true, "SizeParameters", "deviceDensity = " + DeviceUtils.toDensity(DENSITY_DEVICE) + (Build.VERSION.SDK_INT >= 24 ? ", deviceDensityStable = " + DeviceUtils.toDensity(DENSITY_DEVICE_STABLE) : ""));
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
        DebugUtils.__checkError(opts.outWidth <= 0 || opts.outHeight <= 0, "outWidth = " + opts.outWidth + ", outHeight = " + opts.outHeight);
        final int width, height;
        if (target instanceof View) {
            final View view = (View)target;
            width  = Math.max(view.getWidth(), minWidth);
            height = Math.max(view.getHeight(), (int)value);
        } else {
            width  = minWidth;
            height = (int)value;
        }

        DebugUtils.__checkWarning(width <= 0 || height <= 0, "SizeParameters", "The image will be decode original size (width = " + width + ", height = " + height + ").");
        if (width > 0 && height > 0 && opts.outWidth > width && opts.outHeight > height) {
            final float scale = Math.max((float)opts.outWidth / width, (float)opts.outHeight / height);
            opts.inTargetDensity = DENSITY_DEVICE;
            opts.inDensity = (int)(DENSITY_DEVICE * scale + 0.5f);
        } else {
            opts.inDensity = opts.inTargetDensity = 0;
        }
    }

    @Override
    public void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { config = ").append(config.name())
            .append(", minWidth = ").append(minWidth)
            .append(", minHeight = ").append(value)
            .append(", deviceDensity = ").append(DeviceUtils.toDensity(DENSITY_DEVICE))
            .append(" }").toString());
    }

    static {
        defaultParameters = new SizeParameters(Config.RGB_565, 0, 0);
    }
}
