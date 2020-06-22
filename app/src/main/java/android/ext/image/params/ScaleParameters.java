package android.ext.image.params;

import static android.util.DisplayMetrics.DENSITY_DEVICE;
import static android.util.DisplayMetrics.DENSITY_DEVICE_STABLE;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.util.ClassUtils;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Printer;

/**
 * Class <tt>ScaleParameters</tt> is an implementation of a {@link Parameters}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;ScaleParameters
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      app:config="[ argb_8888 | rgb_565 ]"
 *      app:scale="70%" /&gt;</pre>
 * @author Garfield
 */
@SuppressWarnings("deprecation")
public class ScaleParameters extends Parameters {
    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #ScaleParameters(Config, float)
     */
    public ScaleParameters(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, ClassUtils.getFieldValue(context.getPackageName(), "ScaleParameters"));
        this.value = a.getFraction(0 /* R.styleable.ScaleParameters_scale */, 1, 1, 0);
        DebugUtils.__checkError(Float.compare((float)value, +0.0f) < 0 || Float.compare((float)value, +1.0f) > 0, "The scale " + value + " out of range [0 - 1.0]");
        a.recycle();
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param config The {@link Config} to decode.
     * @param scale The scale amount of the image's size to decode.
     * @see #ScaleParameters(Context, AttributeSet)
     */
    public ScaleParameters(Config config, float scale) {
        super(scale, config);
        DebugUtils.__checkError(Float.compare(scale, +0.0f) < 0 || Float.compare(scale, +1.0f) > 0, "The scale " + scale + " out of range [0 - 1.0]");
        DebugUtils.__checkDebug(true, "ScaleParameters", "deviceDensity = " + DeviceUtils.toDensity(DENSITY_DEVICE) + (Build.VERSION.SDK_INT >= 24 ? ", deviceDensityStable = " + DeviceUtils.toDensity(DENSITY_DEVICE_STABLE) : ""));
    }

    @Override
    public int computeByteCount(Options opts) {
        return computeByteCountImpl(opts);
    }

    @Override
    public void computeSampleSize(Object target, Options opts) {
        /*
         * Scale width, expressed as a percentage of the image's width.
         *      scale = opts.outWidth / (opts.outWidth * 0.7f); // scale 70%
         */
        DebugUtils.__checkError(opts.outWidth <= 0 || opts.outHeight <= 0, "outWidth = " + opts.outWidth + ", outHeight = " + opts.outHeight);
        final float scale = (float)value;
        if (Float.compare(scale, +0.0f) > 0 && Float.compare(scale, +1.0f) < 0) {
            opts.inTargetDensity = DENSITY_DEVICE;
            opts.inDensity = (int)(DENSITY_DEVICE * (opts.outWidth / (opts.outWidth * scale)));
        } else {
            opts.inDensity = opts.inTargetDensity = 0;
        }
    }

    @Override
    public void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { config = ").append(config.name())
            .append(", scale = ").append(value)
            .append(", deviceDensity = ").append(DeviceUtils.toDensity(DENSITY_DEVICE))
            .append(" }").toString());
    }
}
