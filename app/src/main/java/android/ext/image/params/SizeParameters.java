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
 *      android:width="200dp"
 *      android:height="300dp"
 *      app:config="[ argb_8888 | rgb_565 ]" /&gt;</pre>
 * @author Garfield
 */
@SuppressWarnings("deprecation")
public class SizeParameters extends Parameters {
    private static final int[] SIZE_PARAMETERS_ATTRS = {
        android.R.attr.height,
        android.R.attr.width,
    };

    /**
     * The default {@link Parameters} (sample size = 1,
     * config = RGB_565) used to decode the bitmap.
     */
    public static final Parameters defaultParameters;

    /**
     * The desired width to decode, in pixels.
     */
    private final int width;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #SizeParameters(Config, int, int)
     */
    @SuppressLint("ResourceType")
    public SizeParameters(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, SIZE_PARAMETERS_ATTRS);
        this.width = a.getDimensionPixelOffset(1 /* android.R.attr.width */, 0);
        this.value = a.getDimensionPixelOffset(0 /* android.R.attr.height */, 0);
        a.recycle();
    }

    /**
     * Constructor
     * @param config The {@link Config} to decode.
     * @param width The desired width to decode, in pixels.
     * @param height The desired height to decode, in pixels.
     * @see #SizeParameters(Context, AttributeSet)
     */
    public SizeParameters(Config config, int width, int height) {
        super(height, config);
        this.width = width;
        DebugUtils.__checkDebug(true, "SizeParameters", "deviceDensity = " + DeviceUtils.toDensity(DENSITY_DEVICE) + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? ", deviceDensityStable = " + DeviceUtils.toDensity(DENSITY_DEVICE_STABLE) : ""));
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
        final int width, height;
        if (target instanceof View) {
            final View view = (View)target;
            width  = Math.max(view.getWidth(),  this.width);
            height = Math.max(view.getHeight(), (int)value);
        } else {
            width  = this.width;
            height = (int)value;
        }

        DebugUtils.__checkWarning(width <= 0 || height <= 0, "SizeParameters", "The image will be decode original size (width = " + width + ", height = " + height + ").");
        opts.inSampleSize = 1;
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
            .append(", width = ").append(width)
            .append(", height = ").append(value)
            .append(", deviceDensity = ").append(DeviceUtils.toDensity(DENSITY_DEVICE))
            .append(" }").toString());
    }

    static {
        defaultParameters = new SizeParameters(Config.RGB_565, 0, 0);
    }
}
