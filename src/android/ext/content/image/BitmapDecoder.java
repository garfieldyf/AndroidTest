package android.ext.content.image;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.ext.content.image.ImageLoader.ImageDecoder;
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
public class BitmapDecoder<Params> extends AbsImageDecoder<Params, Bitmap> {
    /**
     * The {@link Parameters} to decode bitmap.
     */
    protected final Parameters mParameters;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @param maxPoolSize The maximum number of {@link Options} in the internal pool.
     */
    public BitmapDecoder(Context context, Parameters parameters, int maxPoolSize) {
        super(context, maxPoolSize);
        mParameters = parameters;
    }

    @Override
    public void dump(Printer printer) {
        super.dump(printer);
        DebugUtils.dumpSummary(printer, new StringBuilder(80), 80, " Dumping Parameters ", (Object[])null);
        printer.println("  " + mParameters.toString());
    }

    @Override
    protected Bitmap decodeImage(Object uri, Params[] params, int flags, Options opts) throws Exception {
        // Computes the sample size.
        opts.inPreferredConfig = mParameters.config;
        mParameters.computeSampleSize(mContext, uri, opts);

        // Decodes the bitmap pixels.
        return decodeBitmap(uri, params, flags, opts);
    }

    @Override
    protected void decodeImageBounds(Object uri, Params[] params, int flags, Options opts) throws Exception {
        if (mParameters instanceof SizeParameters || this instanceof CacheBitmapDecoder) {
            super.decodeImageBounds(uri, params, flags, opts);
        }
    }

    /**
     * Class <tt>Parameters</tt> can be used to decode bitmap.
     * <h2>Usage</h2>
     * <p>Here is a xml resource example:</p><pre>
     * &lt;[ Parameters | SizeParameters | parameters ]
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
            this.config = config;
            this.value  = BitmapUtils.fixSampleSize(sampleSize);
        }

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         * @param attrs The attributes of the XML tag that is inflating the data.
         * @see #Parameters(Config, int)
         */
        public Parameters(Context context, AttributeSet attrs) {
            final TypedArray a = context.obtainStyledAttributes(attrs, PARAMETERS_ATTRS);
            this.config = parseConfig(a.getInt(1 /* R.styleable.Parameters_config */, -1));
            this.value  = BitmapUtils.fixSampleSize(a.getInt(0 /* R.styleable.Parameters_sampleSize */, 1));
            a.recycle();
        }

        /**
         * Returns a sample size for used to decode bitmap.
         * @param context The <tt>Context</tt>.
         * @param uri The uri, passed earlier by {@link ImageDecoder#decodeImage}.
         * @param opts The {@link Options}, passed earlier by {@link ImageDecoder#decodeImage}.
         */
        public void computeSampleSize(Context context, Object uri, Options opts) {
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
            DebugUtils.__checkError(PARAMETERS_ATTRS != null, "The attributes has already initialized.");
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
            final int desiredWidth = a.getDimensionPixelOffset(1 /* android.R.attr.width */, 0);
            if (desiredWidth <= 0) {
                throw new NotFoundException("The tag requires a valid 'width' attribute");
            }

            final int desiredHeight = a.getDimensionPixelOffset(0 /* android.R.attr.height */, 0);
            if (desiredHeight <= 0) {
                throw new NotFoundException("The tag requires a valid 'height' attribute");
            }

            a.recycle();
            this.value = desiredWidth;
            this.desiredHeight = desiredHeight;
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
        public void computeSampleSize(Context context, Object uri, Options opts) {
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
     * Class <tt>DefaultParameters</tt> (The default parameters sample size = 1, config = ARGB_8888).
     */
    private static final class DefaultParameters {
        public static final Parameters sInstance = new Parameters(1, Config.ARGB_8888);
    }
}
