package android.ext.content.image.params;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.AttributeSet;
import android.util.Printer;

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
     * The desired width to decode.
     */
    public final int desiredWidth;

    /**
     * The desired height to decode.
     */
    public final int desiredHeight;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #SizeParameters(Context, Config, int, int, boolean)
     */
    public SizeParameters(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, SIZE_PARAMETERS_ATTRS);
        this.value = context.getResources().getDisplayMetrics().densityDpi;
        this.desiredWidth  = a.getDimensionPixelOffset(1 /* android.R.attr.width */, 0);
        this.desiredHeight = a.getDimensionPixelOffset(0 /* android.R.attr.height */, 0);
        a.recycle();

        DebugUtils.__checkError(desiredWidth <= 0, "The tag requires a valid 'width' attribute");
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
        super(context.getResources().getDisplayMetrics().densityDpi, config, mutable);
        this.desiredWidth  = desiredWidth;
        this.desiredHeight = desiredHeight;
        DebugUtils.__checkError(desiredWidth <= 0 || desiredHeight <= 0, "desiredWidth <= 0 || desiredHeight <= 0");
    }

    @Override
    public int computeByteCount(Context context, Options opts) {
        return computeByteCount(opts);
    }

    @Override
    public void computeSampleSize(Context context, Options opts) {
        /*
         * Scale width and height.
         *      scaleX = opts.outWidth  / desiredWidth;
         *      scaleY = opts.outHeight / desiredHeight;
         *      scale  = max(scaleX, scaleY);
         */
        DebugUtils.__checkError(opts.inDensity != 0 || opts.inTargetDensity != 0, "opts.inDensity and opts.inTargetDensity uninitialized");
        opts.inSampleSize = 1;
        if (opts.outWidth > desiredWidth && opts.outHeight > desiredHeight) {
            final float scale = Math.max((float)opts.outWidth / desiredWidth, (float)opts.outHeight / desiredHeight);
            final int targetDensity = (int)value;
            opts.inTargetDensity = targetDensity;
            opts.inDensity = (int)(targetDensity * scale);
        }
    }

    @Override
    public void dump(Printer printer, String indent) {
        printer.println(new StringBuilder(128).append(indent)
            .append(getClass().getSimpleName())
            .append(" { config = ").append(config.name())
            .append(", desiredWidth = ").append(desiredWidth)
            .append(", desiredHeight = ").append(desiredHeight)
            .append(", mutable = ").append(mutable)
            .append(" }").toString());
    }
}
