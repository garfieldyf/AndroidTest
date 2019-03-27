package android.ext.image.params;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.util.ClassUtils;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.AttributeSet;
import android.util.Printer;

/**
 * Class <tt>ScaleParameters</tt> is an implementation of a {@link Parameters}.
 * <h2>Usage</h2>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;ScaleParameters
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      app:config="[ argb_8888 | rgb_565 ]"
 *      app:mutable="true"
 *      app:scale="0.7" /&gt;</pre>
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
     * @see #ScaleParameters(Context, Config, float, boolean)
     */
    public ScaleParameters(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, (int[])ClassUtils.getAttributeValue(context, "ScaleParameters"));
        this.value = context.getResources().getDisplayMetrics().densityDpi;
        this.scale = a.getFloat(0 /* R.styleable.ScaleParameters_scale */, 0);
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
    public ScaleParameters(Context context, Config config, float scale, boolean mutable) {
        super(context.getResources().getDisplayMetrics().densityDpi, config, mutable);
        this.scale = scale;
        DebugUtils.__checkError(Float.compare(scale, +0.0f) < 0 || Float.compare(scale, +1.0f) > 0, "The scale " + scale + " out of range [0 - 1.0]");
    }

    @Override
    public int computeByteCount(Context context, Options opts) {
        return computeByteCount(opts);
    }

    @Override
    public void computeSampleSize(Context context, Options opts) {
        /*
         * Scale width, expressed as a percentage of the image's width.
         *      scale = opts.outWidth / (opts.outWidth * 0.7f); // scale 70%
         */
        DebugUtils.__checkError(opts.inDensity != 0 || opts.inTargetDensity != 0, "opts.inDensity and opts.inTargetDensity uninitialized");
        opts.inSampleSize = 1;
        if (Float.compare(scale, +0.0f) > 0 && Float.compare(scale, +1.0f) < 0) {
            final int targetDensity = (int)value;
            opts.inTargetDensity = targetDensity;
            opts.inDensity = (int)(targetDensity * (opts.outWidth / (opts.outWidth * scale)));
        }
    }

    @Override
    public void dump(Printer printer, String indent) {
        printer.println(new StringBuilder(128).append(indent)
            .append(getClass().getSimpleName())
            .append(" { config = ").append(config.name())
            .append(", scale = ").append(scale)
            .append(", mutable = ").append(mutable)
            .append(" }").toString());
    }
}
