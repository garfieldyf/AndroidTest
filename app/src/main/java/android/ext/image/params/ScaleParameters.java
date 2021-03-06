package android.ext.image.params;

import static android.ext.util.DeviceUtils.DEVICE_DENSITY;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.ReflectUtils;
import android.graphics.BitmapFactory.Options;
import android.util.AttributeSet;
import android.util.Printer;

/**
 * Class <tt>ScaleParameters</tt> is an implementation of a {@link Parameters}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;ScaleParameters xmlns:app="http://schemas.android.com/apk/res-auto"
 *      app:scale="70%" /&gt;</pre>
 * @author Garfield
 */
public class ScaleParameters extends Parameters {
    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param scale The scale amount of the image's size to decode.
     * @see #ScaleParameters(Context, AttributeSet)
     */
    public ScaleParameters(float scale) {
        this.value = scale;
        DebugUtils.__checkError(scale < 0f || scale > 1.0f, "The scale " + scale + " out of range [0 - 1.0]");
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #ScaleParameters(float)
     */
    public ScaleParameters(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, ReflectUtils.getAttributes(context.getPackageName(), "ScaleParameters"));
        this.value = a.getFraction(0 /* R.styleable.ScaleParameters_scale */, 1, 1, 0);
        DebugUtils.__checkError((float)value < 0f || (float)value > 1.0f, "The scale " + value + " out of range [0 - 1.0]");
        a.recycle();
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
        DebugUtils.__checkError(opts.outWidth <= 0 || opts.outHeight <= 0, "opts.outWidth(" + opts.outWidth + ") and opts.outHeight(" + opts.outHeight + ") must be > 0");
        DebugUtils.__checkError(opts.inDensity != 0 || opts.inTargetDensity != 0, "opts.inDensity(" + opts.inDensity + ") and opts.inTargetDensity(" + opts.inTargetDensity + ") must be == 0");
        final float scale = (float)value;
        if (scale > 0f && scale < 1.0f) {
            opts.inTargetDensity = DEVICE_DENSITY;
            opts.inDensity = (int)(DEVICE_DENSITY * (opts.outWidth / (opts.outWidth * scale)));
        }
    }

    @Override
    public void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { scale = ").append(value)
            .append(", deviceDensity = ").append(DeviceUtils.toDensity(DEVICE_DENSITY))
            .append(" }").toString());
    }
}
