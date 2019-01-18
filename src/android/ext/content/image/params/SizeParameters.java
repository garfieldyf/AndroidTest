package android.ext.content.image.params;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.AttributeSet;

/**
 * Class <tt>SizeParameters</tt> is an implementation of a {@link Parameters}.
 * <h2>Usage</h2>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;SizeParameters
 *      xmlns:android="http://schemas.android.com/apk/res/android"
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      android:width="200dp"
 *      android:height="300dp"
 *      app:mutable="true"
 *      app:config="[ argb_8888 | rgb_565 | alpha_8 ]" /&gt;</pre>
 * @author Garfield
 */
public class SizeParameters extends Parameters {
    private static final int[] SIZE_PARAMETERS_ATTRS = {
        android.R.attr.height,
        android.R.attr.width,
    };

    /**
     * The desired height to decode.
     */
    public final int desiredHeight;

    /**
     * The screen density.
     */
    private final int targetDensity;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #SizeParameters(Context, Config, int, int, boolean)
     */
    public SizeParameters(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, SIZE_PARAMETERS_ATTRS);
        this.value = a.getDimensionPixelOffset(1 /* android.R.attr.width */, 0);
        this.desiredHeight = a.getDimensionPixelOffset(0 /* android.R.attr.height */, 0);
        this.targetDensity = context.getResources().getDisplayMetrics().densityDpi;
        a.recycle();

        DebugUtils.__checkError((int)value <= 0, "The tag requires a valid 'width' attribute");
        DebugUtils.__checkError(desiredHeight <= 0, "The tag requires a valid 'height' attribute");
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param config The {@link Config} to decode.
     * @param desiredWidth The desired width to decode.
     * @param desiredHeight The desired height to decode.
     * @param mutable Whether to decode a mutable bitmap.
     * @see #SizeParameters(Context, AttributeSet)
     */
    public SizeParameters(Context context, Config config, int desiredWidth, int desiredHeight, boolean mutable) {
        super(desiredWidth, config, mutable);

        DebugUtils.__checkError(desiredWidth <= 0 || desiredHeight <= 0, "desiredWidth <= 0 || desiredHeight <= 0");
        this.desiredHeight = desiredHeight;
        this.targetDensity = context.getResources().getDisplayMetrics().densityDpi;
    }

    @Override
    public int computeByteCount(Context context, Options opts) {
        return computeByteCountImpl(context, opts);
    }

    @Override
    public void computeSampleSize(Context context, Options opts) {
        /*
         * Scale width and height.
         *      scaleX = opts.outWidth  / desiredWidth;
         *      scaleY = opts.outHeight / desiredHeight;
         *      scale  = max(scaleX, scaleY);
         */
        final int desiredWidth = (int)value;
        opts.inSampleSize = 1;
        if (opts.outWidth <= desiredWidth || opts.outHeight <= desiredHeight) {
            opts.inDensity = opts.inTargetDensity = 0;
        } else {
            final float scale = Math.max((float)opts.outWidth / desiredWidth, (float)opts.outHeight / desiredHeight);
            opts.inTargetDensity = targetDensity;
            opts.inDensity = (int)(targetDensity * scale);
        }
    }

    @Override
    public String toString() {
        return new StringBuilder(128).append(getClass().getSimpleName())
            .append(" { config = ").append(config.name())
            .append(", desiredWidth = ").append(value)
            .append(", desiredHeight = ").append(desiredHeight)
            .append(", mutable = ").append(mutable)
            .append(" }").toString();
    }
}
