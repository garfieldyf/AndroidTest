package android.ext.content.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.content.XmlResources;
import android.ext.graphics.BitmapUtils;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.AttributeSet;
import android.util.Printer;

/**
 * Class BitmapDecoder
 * @author Garfield
 * @version 1.0
 */
public class BitmapDecoder extends AbsImageDecoder<Bitmap> {
    /**
     * The {@link Parameters} to decode bitmap.
     */
    protected final Parameters mParameters;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param id The resource id of the {@link Parameters} to load.
     * @param maxPoolSize The maximum number of {@link Options} in the internal pool.
     * @see #BitmapDecoder(Context, Parameters, int)
     */
    public BitmapDecoder(Context context, int id, int maxPoolSize) {
        super(context, maxPoolSize);
        mParameters = XmlResources.loadParameters(mContext, id);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @param maxPoolSize The maximum number of {@link Options} in the internal pool.
     * @see #BitmapDecoder(Context, int, int)
     */
    public BitmapDecoder(Context context, Parameters parameters, int maxPoolSize) {
        super(context, maxPoolSize);
        mParameters = parameters;
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link BitmapDecoder} from the specified <em>decoder</em>. The
     * returned decoder will be share the internal cache with the <em>decoder</em>.</p>
     * @param decoder The <tt>BitmapDecoder</tt> to copy.
     * @param id The resource id of the {@link Parameters} to load.
     * @see #BitmapDecoder(BitmapDecoder, Parameters)
     */
    public BitmapDecoder(BitmapDecoder decoder, int id) {
        super(decoder);
        mParameters = XmlResources.loadParameters(mContext, id);
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link BitmapDecoder} from the specified <em>decoder</em>. The
     * returned decoder will be share the internal cache with the <em>decoder</em>.</p>
     * @param decoder The <tt>BitmapDecoder</tt> to copy.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @see #BitmapDecoder(BitmapDecoder, int)
     */
    public BitmapDecoder(BitmapDecoder decoder, Parameters parameters) {
        super(decoder);
        mParameters = parameters;
    }

    @Override
    public void dump(Printer printer) {
        super.dump(printer);
        DebugUtils.dumpSummary(printer, new StringBuilder(80), 80, " Dumping Parameters ", (Object[])null);
        printer.println("  " + mParameters.toString());
    }

    @Override
    protected Bitmap decodeImage(Object uri, Object[] params, int flags, Options opts) throws Exception {
        // Computes the sample size.
        opts.inPreferredConfig = mParameters.config;
        mParameters.computeSampleSize(mContext, opts);

        // Decodes the bitmap pixels.
        return BitmapUtils.decodeBitmap(mContext, uri, opts);
    }

    @Override
    protected void decodeImageBounds(Object uri, Object[] params, int flags, Options opts) throws Exception {
        if (mParameters.requestDecodeBounds()) {
            super.decodeImageBounds(uri, params, flags, opts);
        }
    }

    /**
     * Class <tt>Parameters</tt> can be used to decode bitmap.
     * <h2>Usage</h2>
     * <p>Here is a xml resource example:</p><pre>
     * &lt;[ Parameters | SizeParameters | ScaleParameters | parameters ]
     *      xmlns:android="http://schemas.android.com/apk/res/android"
     *      xmlns:namespace="http://schemas.android.com/apk/res/<em>packageName</em>"
     *      class="classFullName"
     *      android:width="200dp"
     *      android:height="300dp"
     *      namespace:config="[ argb_8888 | rgb_565 | alpha_8 ]"
     *      namespace:sampleSize="2"
     *      namespace:attributes1="value1"
     *      namespace:attributes2="value2" /&gt;</pre>
     */
    public static class Parameters {
        private static final int ALPHA_8 = 0;
        private static final int RGB_565 = 1;

        /**
         * The parameters attributes.
         */
        private static int[] PARAMETERS_ATTRS;

        /**
         * The Object by user-defined to decode.
         */
        public Object value;

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
            this.value  = BitmapUtils.fixSampleSize(sampleSize);
            this.config = (config != null ? config : Config.ARGB_8888);
        }

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         * @param attrs The attributes of the XML tag that is inflating the data.
         * @see #Parameters(Config, int)
         */
        public Parameters(Context context, AttributeSet attrs) {
            DebugUtils.__checkError(PARAMETERS_ATTRS == null, "The " + getClass().getName() + " did not call Parameters.initAttrs()");
            final TypedArray a = context.obtainStyledAttributes(attrs, PARAMETERS_ATTRS);
            this.config = parseConfig(a.getInt(1 /* R.styleable.Parameters_config */, 2));
            this.value  = BitmapUtils.fixSampleSize(a.getInt(0 /* R.styleable.Parameters_sampleSize */, 1));
            a.recycle();
        }

        /**
         * Enables to decode bitmap bounds when the decoder decoding bitmap.
         * @return <tt>true</tt> if enable to decode bitmap bounds, <tt>false</tt> otherwise.
         */
        public boolean requestDecodeBounds() {
            return false;
        }

        /**
         * Computes the number of bytes that can be used to store the bitmap's
         * pixels when decoding the bitmap.
         * @param context The <tt>Context</tt>.
         * @param opts The {@link Options} to compute byte count, passed earlier
         * by <em>ImageDecoder.decodeImage</em>.
         */
        public int computeByteCount(Context context, Options opts) {
            return (int)((float)opts.outWidth / opts.inSampleSize + 0.5f) * (int)((float)opts.outHeight / opts.inSampleSize + 0.5f) * BitmapUtils.getBytesPerPixel(opts.inPreferredConfig);
        }

        /**
         * Computes a sample size for used to decode bitmap.
         * @param context The <tt>Context</tt>.
         * @param opts The {@link Options} to store the sample
         * size, passed earlier by <em>ImageDecoder.decodeImage</em>.
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
         * Initialize the {@link Parameters} styleable. <p>Note: This method recommended
         * call in the <tt>Application's</tt> static constructor.</p>
         * <p>Includes the following attributes:
         * <table><colgroup align="left" /><colgroup align="left" /><colgroup align="center" />
         * <tr><th>Attribute</th><th>Type</th><th>Index</th></tr>
         * <tr><td><tt>sampleSize</tt></td><td>integer</td><td>0</td></tr>
         * <tr><td><tt>config</tt></td><td>enum</td><td>1</td></tr>
         * </table></p>
         * @param attrs The <tt>R.styleable.Parameters</tt> styleable, as generated by the aapt tool.
         */
        public static void initAttrs(int[] attrs) {
            PARAMETERS_ATTRS = attrs;
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
    }

    /**
     * Class <tt>SizeParameters</tt> is an implementation of a {@link Parameters}.
     */
    public static class SizeParameters extends Parameters {
        private static final int[] SIZE_PARAMETERS_ATTRS = {
            android.R.attr.height,
            android.R.attr.width,
        };

        /**
         * The desired height to decode.
         */
        public final int desiredHeight;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         * @param attrs The attributes of the XML tag that is inflating the data.
         * @see #SizeParameters(Config, int, int)
         */
        public SizeParameters(Context context, AttributeSet attrs) {
            super(context, attrs);

            final TypedArray a = context.obtainStyledAttributes(attrs, SIZE_PARAMETERS_ATTRS);
            this.value = a.getDimensionPixelOffset(1 /* android.R.attr.width */, 0);
            this.desiredHeight = a.getDimensionPixelOffset(0 /* android.R.attr.height */, 0);
            a.recycle();

            DebugUtils.__checkError((int)value <= 0, "The tag requires a valid 'width' attribute");
            DebugUtils.__checkError(desiredHeight <= 0, "The tag requires a valid 'height' attribute");
        }

        /**
         * Constructor
         * @param config The {@link Config} to decode.
         * @param desiredWidth The desired width to decode.
         * @param desiredHeight The desired height to decode.
         * @see #SizeParameters(Context, AttributeSet)
         */
        public SizeParameters(Config config, int desiredWidth, int desiredHeight) {
            super(desiredWidth, config);
            this.desiredHeight = desiredHeight;
            DebugUtils.__checkError(desiredWidth <= 0 || desiredHeight <= 0, "desiredWidth <= 0 || desiredHeight <= 0");
        }

        @Override
        public boolean requestDecodeBounds() {
            return true;
        }

        @Override
        public void computeSampleSize(Context context, Options opts) {
            opts.inSampleSize = BitmapUtils.computeSampleSize(opts.outWidth, opts.outHeight, (int)value, desiredHeight);
        }

        @Override
        public String toString() {
            return new StringBuilder(128).append(getClass().getSimpleName())
                .append(" { config = ").append(config.name())
                .append(", desiredWidth = ").append(value)
                .append(", desiredHeight = ").append(desiredHeight)
                .append(" }").toString();
        }
    }

    /**
     * Class <tt>ScaleParameters</tt> is an implementation of a {@link Parameters}.
     */
    public static class ScaleParameters extends SizeParameters {
        private final int targetDensity;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         * @param attrs The attributes of the XML tag that is inflating the data.
         * @see #ScaleParameters(Context, Config, int, int)
         */
        public ScaleParameters(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.targetDensity = context.getResources().getDisplayMetrics().densityDpi;
        }

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         * @param config The {@link Config} to decode.
         * @param desiredWidth The desired width to decode.
         * @param desiredHeight The desired height to decode.
         * @see #ScaleParameters(Context, AttributeSet)
         */
        public ScaleParameters(Context context, Config config, int desiredWidth, int desiredHeight) {
            super(config, desiredWidth, desiredHeight);
            this.targetDensity = context.getResources().getDisplayMetrics().densityDpi;
        }

        @Override
        public int computeByteCount(Context context, Options opts) {
            final int byteCount = BitmapUtils.getBytesPerPixel(opts.inPreferredConfig);
            if (opts.outWidth <= (int)value || opts.outHeight <= desiredHeight) {
                return (opts.outWidth * opts.outHeight * byteCount);
            } else {
                final float densityRatio = (float)opts.inTargetDensity / opts.inDensity;
                return (int)(densityRatio * opts.outWidth + 0.5f) * (int)(densityRatio * opts.outHeight + 0.5f) * byteCount;
            }
        }

        @Override
        public void computeSampleSize(Context context, Options opts) {
            /*
             * Scale width and height.
             *      scaleX = opts.outWidth  / desiredWidth;
             *      scaleY = opts.outHeight / desiredHeight;
             *      scale  = max(scaleX, scaleY);
             *
             * Scale width, expressed as a percentage of the bitmap's width.
             *      scale = opts.outWidth / (opts.outWidth * 0.7f);     // scale 70%
             */
            opts.inSampleSize = 1;
            if (opts.outWidth <= (int)value || opts.outHeight <= desiredHeight) {
                opts.inDensity = opts.inTargetDensity = 0;
            } else {
                final float scale = Math.max((float)opts.outWidth / (int)value, (float)opts.outHeight / desiredHeight);
                opts.inTargetDensity = targetDensity;
                opts.inDensity = (int)(targetDensity * scale);
            }
        }
    }

    /**
     * Class <tt>DefaultParameters</tt> (The default parameters sample size = 1, config = ARGB_8888).
     */
    private static final class DefaultParameters {
        public static final Parameters sInstance = new Parameters(1, Config.ARGB_8888);
    }
}
