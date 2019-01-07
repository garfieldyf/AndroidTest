package android.ext.content.image.params;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.util.ClassUtils;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.AttributeSet;

/**
 * Class <tt>PercentParameters</tt> is an implementation of a {@link Parameters}.
 * <h2>Usage</h2>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;PercentParameters
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      app:config="[ argb_8888 | rgb_565 | alpha_8 ]"
 *      app:percent="0.7" /&gt;</pre>
 * @author Garfield
 */
public class PercentParameters extends Parameters {
    private final int mTargetDensity;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #PercentParameters(Context, Config, float)
     */
    public PercentParameters(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, (int[])ClassUtils.getAttributeValue(context, "PercentParameters"));
        value = a.getFloat(0 /* R.styleable.PercentParameters_percent */, 0);
        mTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
        a.recycle();
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param config The {@link Config} to decode.
     * @param percent The percent to decode, expressed as a percentage of the image's size.
     * @see #PercentParameters(Context, AttributeSet)
     */
    public PercentParameters(Context context, Config config, float percent) {
        super(percent, config);
        mTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
    }

    @Override
    public boolean requestDecodeBounds() {
        return true;
    }

    @Override
    public int computeByteCount(Context context, Options opts) {
        return computeByteCountImpl(context, opts);
    }

    @Override
    public void computeSampleSize(Context context, Options opts) {
        /*
         * Scale width, expressed as a percentage of the image's width.
         *      scale = opts.outWidth / (opts.outWidth * 0.7f); // scale 70%
         */
        opts.inSampleSize = 1;
        if ((float)value <= 0 || (float)value >= 1.0f) {
            opts.inDensity = opts.inTargetDensity = 0;
        } else {
            final float scale = opts.outWidth / (opts.outWidth * (float)value);
            opts.inTargetDensity = mTargetDensity;
            opts.inDensity = (int)(mTargetDensity * scale);
        }
    }
}
