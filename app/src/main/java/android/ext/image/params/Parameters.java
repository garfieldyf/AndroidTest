package android.ext.image.params;

import static android.ext.util.DeviceUtils.DEVICE_DENSITY;
import android.annotation.WorkerThread;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.graphics.BitmapUtils;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.ReflectUtils;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Printer;
import android.view.View;

/**
 * Class <tt>Parameters</tt> can be used to decode bitmap.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;[ Parameters | parameters ]
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      class="classFullName"
 *      app:config="[ argb_8888 | rgb_565 | hardware | rgba_f16 ]"
 *      app:sampleSize="2" /&gt;</pre>
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
        final TypedArray a = context.obtainStyledAttributes(attrs, ReflectUtils.getAttributes(packageName, "Parameters"));
        this.value  = fixSampleSize(a.getInt(ReflectUtils.getAttributeIndex(packageName, "Parameters_sampleSize"), 1));
        this.config = parseConfig(a.getInt(ReflectUtils.getAttributeIndex(packageName, "Parameters_config"), ARGB_8888));
        a.recycle();
    }

    /**
     * Constructor
     * @param value The Object by user-defined to decode.
     * @param config The {@link Config} to decode.
     */
    protected Parameters(Object value, Config config) {
        DebugUtils.__checkError(config == null, "Invalid parameter - config == null");
        this.value  = value;
        this.config = config;
    }

    /**
     * Return whether the decoded <tt>Bitmap</tt> will be mutable.
     */
    @WorkerThread
    public boolean isMutable() {
        // Bitmaps with Config.HARWARE are always immutable.
        return (Build.VERSION.SDK_INT < 26 || config != Config.HARDWARE);
    }

    /**
     * Computes a sample size used to decode image.
     * @param target May be <tt>null</tt>. The target to compute.
     * @param opts The {@link Options} to compute. The <em>opts's</em>
     * <tt>out...</tt> fields are set.
     */
    @WorkerThread
    public void computeSampleSize(Object target, Options opts) {
        opts.inSampleSize = (int)value;
    }

    /**
     * Computes the number of bytes that can be used to store the image's
     * pixels when decoding the image.
     * @param opts The {@link Options} to compute. The <em>opts's</em>
     * <tt>out...</tt> fields are set.
     * @return The number of bytes.
     */
    @WorkerThread
    public int computeByteCount(Options opts) {
        DebugUtils.__checkError(opts.inSampleSize <= 0 || opts.outWidth <= 0 || opts.outHeight <= 0, "opts.inSampleSize(" + opts.inSampleSize + ") and opts.outWidth(" + opts.outWidth + ") and opts.outHeight(" + opts.outHeight + ") must be > 0");
        return (int)((float)opts.outWidth / opts.inSampleSize + 0.5f) * (int)((float)opts.outHeight / opts.inSampleSize + 0.5f) * getBytesPerPixel(opts);
    }

    public void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { config = ").append(config.name())
            .append(", sampleSize = ").append(value)
            .append(" }").toString());
    }

    /**
     * Returns the default {@link Parameters} used to decode the bitmap.
     * The default <tt>Parameters</tt> sampleSize = 1, config = RGB_565.
     */
    public static Parameters defaultParameters() {
        return DefaultParameters.sInstance;
    }

    /**
     * Computes the number of bytes that can be used to
     * store the image's pixels when decoding the image.
     */
    /* package */ static int computeByteCountImpl(Options opts) {
        DebugUtils.__checkError(opts.outWidth <= 0 || opts.outHeight <= 0, "opts.outWidth(" + opts.outWidth + ") and opts.outHeight(" + opts.outHeight + ") must be > 0");
        final int byteCount = getBytesPerPixel(opts);
        if (opts.inTargetDensity == 0) {
            return (opts.outWidth * opts.outHeight * byteCount);
        } else {
            final float scale = (float)opts.inTargetDensity / opts.inDensity;
            return (int)(opts.outWidth * scale + 0.5f) * (int)(opts.outHeight * scale + 0.5f) * byteCount;
        }
    }

    /**
     * Computes the {@link Options#inDensity} to decode image.
     */
    /* package */ static void computeDecodeDensity(int width, int height, Options opts) {
        /*
         * Scale width and height.
         *      scaleX = opts.outWidth  / width;
         *      scaleY = opts.outHeight / height;
         *      scale  = max(scaleX, scaleY);
         */
        DebugUtils.__checkError(opts.inDensity != 0 || opts.inTargetDensity != 0, "opts.inDensity(" + opts.inDensity + ") and opts.inTargetDensity(" + opts.inTargetDensity + ") must be == 0");
        if (width > 0 && height > 0 && opts.outWidth > width && opts.outHeight > height) {
            final float scale = Math.max((float)opts.outWidth / width, (float)opts.outHeight / height);
            opts.inTargetDensity = DEVICE_DENSITY;
            opts.inDensity = (int)(DEVICE_DENSITY * scale + 0.5f);
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
        }

        return (config == RGB_565 ? Config.RGB_565 : Config.ARGB_8888);
    }

    private static int fixSampleSize(int sampleSize) {
        return (sampleSize <= 1 ? 1 : (sampleSize <= 8 ? Integer.highestOneBit(sampleSize) : (sampleSize / 8 * 8)));
    }

    private static int getBytesPerPixel(Options opts) {
        Parameters.__checkOptions(opts);
        return BitmapUtils.getBytesPerPixel(Build.VERSION.SDK_INT >= 26 && opts.outConfig != null ? opts.outConfig : opts.inPreferredConfig);
    }

    private static void __checkOptions(Options opts) {
        if (Build.VERSION.SDK_INT >= 26) {
            Log.d("Parameters", "getBytesPerPixel - outConfig = " + opts.outConfig);
            if (opts.outConfig == null && opts.inPreferredConfig == null) {
                throw new AssertionError("opts.outConfig == null && opts.inPreferredConfig == null");
            }
        } else if (opts.inPreferredConfig == null) {
            throw new AssertionError("opts.inPreferredConfig == null");
        }
    }

    /**
     * Class <tt>DefaultParameters</tt> is an implementation of a {@link Parameters}.
     */
    private static final class DefaultParameters extends Parameters {
        public static final Parameters sInstance = new DefaultParameters();

        /**
         * This class cannot be instantiated.
         */
        private DefaultParameters() {
            super(null, Config.RGB_565);
        }

        @Override
        public boolean isMutable() {
            return true;
        }

        @Override
        public int computeByteCount(Options opts) {
            return computeByteCountImpl(opts);
        }

        @Override
        public void computeSampleSize(Object target, Options opts) {
            int width = 0, height = 0;
            if (target instanceof View) {
                final View view = (View)target;
                width  = view.getWidth();
                height = view.getHeight();
            }

            computeDecodeDensity(width, height, opts);
        }

        @Override
        public void dump(Printer printer, StringBuilder result) {
            printer.println(result.append(getClass().getSimpleName())
                .append(" { config = ").append(config.name())
                .append(", sampleSize = 1")
                .append(", deviceDensity = ").append(DeviceUtils.toDensity(DEVICE_DENSITY))
                .append(" }").toString());
        }
    }
}
