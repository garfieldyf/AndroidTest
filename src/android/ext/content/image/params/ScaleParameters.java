package android.ext.content.image.params;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.util.ClassUtils;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.AttributeSet;

/**
 * Class <tt>ScaleParameters</tt> is an implementation of a {@link Parameters}.
 * <h2>Usage</h2>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;ScaleParameters
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      app:config="[ argb_8888 | rgb_565 | alpha_8 ]"
 *      app:mutable="true"
 *      app:scale="0.7" /&gt;</pre>
 * @author Garfield
 */
public class ScaleParameters extends Parameters {
    private final int mTargetDensity;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #ScaleParameters(Context, Config, float, boolean)
     */
    public ScaleParameters(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, (int[])ClassUtils.getAttributeValue(context, "ScaleParameters"));
        value = a.getFloat(0 /* R.styleable.ScaleParameters_scale */, 0);
        mTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
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
        super(scale, config, mutable);
        mTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
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
        final float scale = (float)value;
        opts.inSampleSize = 1;
        if (scale <= 0 || scale >= 1.0f) {
            opts.inDensity = opts.inTargetDensity = 0;
        } else {
            opts.inTargetDensity = mTargetDensity;
            opts.inDensity = (int)(mTargetDensity * (opts.outWidth / (opts.outWidth * scale)));
        }
    }

    @Override
    public String toString() {
        return new StringBuilder(128).append(getClass().getSimpleName())
            .append(" { config = ").append(config.name())
            .append(", scale = ").append(value)
            .append(", mutable = ").append(mutable)
            .append(" }").toString();
    }
}
