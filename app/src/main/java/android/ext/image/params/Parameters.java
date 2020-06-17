package android.ext.image.params;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.support.AppCompat;
import android.ext.util.ClassUtils;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.AttributeSet;
import android.util.Printer;

/**
 * Class <tt>Parameters</tt> can be used to decode bitmap.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;[ Parameters | parameters ]
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      class="classFullName"
 *      app:config="[ argb_8888 | rgb_565 ]"
 *      app:sampleSize="2"
 *      app:attribute1="value1"
 *      app:attribute2="value2"
 *      ... ... /&gt;</pre>
 * @author Garfield
 */
public class Parameters {
    private static final int RGB_565   = 0;
    private static final int ARGB_8888 = 1;

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
        this(fixSampleSize(sampleSize), config);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #Parameters(Config, int)
     */
    public Parameters(Context context, AttributeSet attrs) {
        final String packageName = context.getPackageName();
        final TypedArray a = context.obtainStyledAttributes(attrs, ClassUtils.getFieldValue(packageName, "Parameters"));
        this.value  = fixSampleSize(a.getInt(ClassUtils.getFieldValue(packageName, "Parameters_sampleSize"), 1));
        this.config = a.getInt(ClassUtils.getFieldValue(packageName, "Parameters_config"), ARGB_8888) == RGB_565 ? Config.RGB_565 : Config.ARGB_8888;
        a.recycle();
    }

    /**
     * Computes a sample size for used to decode image.
     * @param target May be <tt>null</tt>. The target to compute.
     * @param opts The {@link Options} to store the sample size.
     */
    public void computeSampleSize(Object target, Options opts) {
        opts.inSampleSize = (int)value;
    }

    /**
     * Computes the number of bytes that can be used to store the image's
     * pixels when decoding the image.
     * @param opts The {@link Options} to compute. The <em>opts's</em>
     * <tt>out...</tt> fields are set.
     */
    public int computeByteCount(Options opts) {
        return (int)((float)opts.outWidth / opts.inSampleSize + 0.5f) * (int)((float)opts.outHeight / opts.inSampleSize + 0.5f) * AppCompat.getBytesPerPixel(opts);
    }

    public void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { config = ").append(config.name())
            .append(", sampleSize = ").append(value)
            .append(" }").toString());
    }

    /**
     * Returns the default {@link Parameters} associated with this class
     * (The default parameters sample size = 1, config = RGB_565).
     */
    public static Parameters defaultParameters() {
        return DefaultParameters.sInstance;
    }

    /**
     * Constructor
     * @param value The Object by user-defined to decode.
     * @param config The {@link Config} to decode.
     */
    protected Parameters(Object value, Config config) {
        this.value  = value;
        this.config = (config != null ? config : Config.ARGB_8888);
    }

    /**
     * Computes the number of bytes that can be used to
     * store the image's pixels when decoding the image.
     */
    /* package */ final int computeByteCountImpl(Options opts) {
        final int byteCount = AppCompat.getBytesPerPixel(opts);
        if (opts.inTargetDensity == 0) {
            return (opts.outWidth * opts.outHeight * byteCount);
        } else {
            final float scale = (float)opts.inTargetDensity / opts.inDensity;
            return (int)(opts.outWidth * scale + 0.5f) * (int)(opts.outHeight * scale + 0.5f) * byteCount;
        }
    }

    private static int fixSampleSize(int sampleSize) {
        return (sampleSize <= 1 ? 1 : (sampleSize <= 8 ? Integer.highestOneBit(sampleSize) : (sampleSize / 8 * 8)));
    }

    /**
     * Class <tt>DefaultParameters</tt> (The default parameters sampleSize = 1, config = RGB_565).
     */
    private static final class DefaultParameters {
        public static final Parameters sInstance = new SizeParameters(Config.RGB_565, 0, 0);
    }
}
