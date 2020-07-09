package android.ext.image.params;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.support.AppCompat;
import android.ext.util.ClassUtils;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Printer;

/**
 * Class <tt>Parameters</tt> can be used to decode bitmap.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;[ Parameters | parameters ]
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      class="classFullName"
 *      app:config="[ argb_8888 | rgb_565 | hardware | rgba_f16 ]"
 *      app:sampleSize="2"
 *      app:attribute1="value1"
 *      app:attribute2="value2"
 *      ... ... /&gt;</pre>
 * @author Garfield
 */
public class Parameters {
    private static final int ARGB_8888 = 0;
    private static final int RGB_565   = 1;
    private static final int HARDWARE  = 2;
    private static final int RGBA_F16  = 3;

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
        this.config = parseConfig(a.getInt(ClassUtils.getFieldValue(packageName, "Parameters_config"), ARGB_8888));
        a.recycle();
    }

    /**
     * Constructor
     * @param value The Object by user-defined to decode.
     * @param config The {@link Config} to decode.
     */
    protected Parameters(Object value, Config config) {
        DebugUtils.__checkError(config == null, "config == null");
        this.value  = value;
        this.config = config;
    }

    /**
     * Return whether the decoded <tt>Bitmap</tt> will be mutable.
     */
    public boolean isMutable() {
        return (Build.VERSION.SDK_INT < 26 || config != Config.HARDWARE);
    }

    /**
     * Computes a sample size used to decode image.
     * @param target May be <tt>null</tt>. The target to compute.
     * @param opts The {@link Options} to compute. The <em>opts's</em>
     * <tt>out...</tt> fields are set.
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
        DebugUtils.__checkError(opts.inSampleSize <= 0, "opts.inSampleSize <= 0");
        DebugUtils.__checkError(opts.outWidth <= 0 || opts.outHeight <= 0, "opts.outWidth <= 0 || opts.outHeight <= 0");
        return (int)((float)opts.outWidth / opts.inSampleSize + 0.5f) * (int)((float)opts.outHeight / opts.inSampleSize + 0.5f) * AppCompat.getBytesPerPixel(opts);
    }

    public void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { config = ").append(config.name())
            .append(", sampleSize = ").append(value)
            .append(" }").toString());
    }

    /**
     * Computes the number of bytes that can be used to
     * store the image's pixels when decoding the image.
     */
    /* package */ final int computeByteCountImpl(Options opts) {
        DebugUtils.__checkError(opts.outWidth <= 0 || opts.outHeight <= 0, "opts.outWidth <= 0 || opts.outHeight <= 0");
        final int byteCount = AppCompat.getBytesPerPixel(opts);
        if (opts.inTargetDensity == 0) {
            return (opts.outWidth * opts.outHeight * byteCount);
        } else {
            final float scale = (float)opts.inTargetDensity / opts.inDensity;
            return (int)(opts.outWidth * scale + 0.5f) * (int)(opts.outHeight * scale + 0.5f) * byteCount;
        }
    }

    private static Config parseConfig(int config) {
        if (Build.VERSION.SDK_INT >= 26) {
            switch (config) {
            case RGB_565:
                return Config.RGB_565;

            case HARDWARE:
                return Config.HARDWARE;

            case RGBA_F16:
                return Config.RGBA_F16;

            default:
                return Config.ARGB_8888;
            }
        } else {
            return (config == RGB_565 ? Config.RGB_565 : Config.ARGB_8888);
        }
    }

    private static int fixSampleSize(int sampleSize) {
        return (sampleSize <= 1 ? 1 : (sampleSize <= 8 ? Integer.highestOneBit(sampleSize) : (sampleSize / 8 * 8)));
    }
}
