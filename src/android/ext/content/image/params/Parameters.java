package android.ext.content.image.params;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.graphics.BitmapUtils;
import android.ext.util.ClassUtils;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.AttributeSet;

/**
 * Class <tt>Parameters</tt> can be used to decode bitmap.
 * <h2>Usage</h2>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;[ Parameters | parameters ]
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      class="classFullName"
 *      app:config="[ argb_8888 | rgb_565 | alpha_8 ]"
 *      app:sampleSize="2"
 *      app:attribute1="value1"
 *      app:attribute2="value2"
 *      ... ... /&gt;</pre>
 * @author Garfield
 */
public class Parameters {
    private static final int ALPHA_8 = 0;
    private static final int RGB_565 = 1;

    /**
     * The Object by user-defined to decode.
     */
    protected Object value;

    /**
     * The desired {@link Config} to decode.
     */
    public final Config config;

    /**
     * Constructor
     * @param config The {@link Config} to decode.
     * @param sampleSize The sample size to decode.
     * @see #Parameters(Context, AttributeSet)
     */
    public Parameters(Config config, int sampleSize) {
        this.value  = fixSampleSize(sampleSize);
        this.config = (config != null ? config : Config.ARGB_8888);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #Parameters(Config, int)
     */
    public Parameters(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, (int[])ClassUtils.getAttributeValue(context, "Parameters"));
        this.config = parseConfig(a.getInt((int)ClassUtils.getAttributeValue(context, "Parameters_config"), 2));
        this.value  = fixSampleSize(a.getInt((int)ClassUtils.getAttributeValue(context, "Parameters_sampleSize"), 1));
        a.recycle();
    }

    /**
     * Computes the number of bytes that can be used to store the image's
     * pixels when decoding the image.
     * @param context The <tt>Context</tt>.
     * @param opts The {@link Options} to compute byte count.
     */
    public int computeByteCount(Context context, Options opts) {
        return (int)((float)opts.outWidth / opts.inSampleSize + 0.5f) * (int)((float)opts.outHeight / opts.inSampleSize + 0.5f) * BitmapUtils.getBytesPerPixel(config);
    }

    /**
     * Computes a sample size for used to decode image.
     * @param context The <tt>Context</tt>.
     * @param opts The {@link Options} to store the sample size.
     */
    public void computeSampleSize(Context context, Options opts) {
        opts.inSampleSize = (int)value;
    }

    @Override
    public String toString() {
        return new StringBuilder(128).append(getClass().getSimpleName())
            .append(" { config = ").append(config.name())
            .append(", sampleSize = ").append(value)
            .append(" }").toString();
    }

    /**
     * Constructor
     * @param value The Object by user-defined to decode.
     * @param config The {@link Config} to decode.
     */
    protected Parameters(Object value, Config config) {
        this.value  = value;
        this.config = config;
    }

    /**
     * Returns the default {@link Parameters} associated with this class
     * (The default parameters sample size = 1, config = ARGB_8888).
     */
    public static Parameters defaultParameters() {
        return DefaultParameters.sInstance;
    }

    private static Config parseConfig(int config) {
        switch (config) {
        case ALPHA_8:
            return Config.ALPHA_8;

        case RGB_565:
            return Config.RGB_565;

        default:
            return Config.ARGB_8888;
        }
    }

    private static int fixSampleSize(int sampleSize) {
        return (sampleSize <= 1 ? 1 : (sampleSize <= 8 ? Integer.highestOneBit(sampleSize) : (sampleSize / 8 * 8)));
    }

    /* package */ static int computeByteCountImpl(Context context, Options opts) {
        final int byteCount = BitmapUtils.getBytesPerPixel(opts.inPreferredConfig);
        if (opts.inTargetDensity == 0) {
            return (opts.outWidth * opts.outHeight * byteCount);
        } else {
            final float scale = (float)opts.inTargetDensity / opts.inDensity;
            return (int)(opts.outWidth * scale + 0.5f) * (int)(opts.outHeight * scale + 0.5f) * byteCount;
        }
    }

    /**
     * Class <tt>DefaultParameters</tt> (The default parameters sampleSize = 1, config = ARGB_8888).
     */
    private static final class DefaultParameters {
        public static final Parameters sInstance = new Parameters(1, Config.ARGB_8888);
    }
}
