package android.ext.image.params;

import static android.util.DisplayMetrics.DENSITY_DEVICE;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.util.ClassUtils;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.AttributeSet;
import android.util.Printer;

/**
 * Class <tt>ScaleParameters</tt> is an implementation of a {@link Parameters}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;ScaleParameters
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      app:config="[ argb_8888 | rgb_565 ]"
 *      app:mutable="true"
 *      app:scale="70%" /&gt;</pre>
 * @author Garfield
 */
public class ScaleParameters extends Parameters {
    /**
     * The scale amount of the image's size to decode.
     */
    public final float scale;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #ScaleParameters(Config, float, boolean)
     */
    public ScaleParameters(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, ClassUtils.getFieldValue(context.getPackageName(), "ScaleParameters"));
        this.value = context.getResources().getDisplayMetrics().densityDpi;
        this.scale = a.getFraction(0 /* R.styleable.ScaleParameters_scale */, 1, 1, 0);
        DebugUtils.__checkError(Float.compare(scale, +0.0f) < 0 || Float.compare(scale, +1.0f) > 0, "The scale " + scale + " out of range [0 - 1.0]");
        a.recycle();
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param config The {@link Config} to decode.
     * @param scale The scale amount of the image's size to decode.
     * @param mutable Whether to decode a mutable bitmap.
     * @see #ScaleParameters(Context, AttributeSet)
     */
    @SuppressWarnings("deprecation")
    public ScaleParameters(Config config, float scale, boolean mutable) {
        super(DENSITY_DEVICE, config, mutable);
        this.scale = scale;
        DebugUtils.__checkError(Float.compare(scale, +0.0f) < 0 || Float.compare(scale, +1.0f) > 0, "The scale " + scale + " out of range [0 - 1.0]");
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
        opts.inSampleSize = 1;
        if (Float.compare(scale, +0.0f) > 0 && Float.compare(scale, +1.0f) < 0) {
            final int screenDensity = (int)value;
            opts.inTargetDensity = screenDensity;
            opts.inDensity = (int)(screenDensity * (opts.outWidth / (opts.outWidth * scale)));
        } else {
            opts.inDensity = opts.inTargetDensity = 0;
        }
    }

    @Override
    public void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { config = ").append(config.name())
            .append(", scale = ").append(scale)
            .append(", screenDensity = ").append(DeviceUtils.toDensity((int)value))
            .append(", mutable = ").append(mutable)
            .append(" }").toString());
    }
}
