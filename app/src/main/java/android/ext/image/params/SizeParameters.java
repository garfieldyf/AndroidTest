package android.ext.image.params;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.AttributeSet;
import android.util.Printer;

/**
 * Class <tt>SizeParameters</tt> is an implementation of a {@link Parameters}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;SizeParameters
 *      xmlns:android="http://schemas.android.com/apk/res/android"
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      android:width="200dp"
 *      android:height="300dp"
 *      app:mutable="true"
 *      app:config="[ argb_8888 | rgb_565 ]" /&gt;</pre>
 * @author Garfield
 */
public class SizeParameters extends Parameters {
    private static final int[] SIZE_PARAMETERS_ATTRS = {
        android.R.attr.height,
        android.R.attr.width,
    };

    /**
     * The desired width to decode, in pixels.
     */
    public final int width;

    /**
     * The desired height to decode, in pixels.
     */
    public final int height;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #SizeParameters(Context, Config, int, int, boolean)
     */
    @SuppressLint("ResourceType")
    public SizeParameters(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, SIZE_PARAMETERS_ATTRS);
        this.value  = context.getResources().getDisplayMetrics().densityDpi;
        this.width  = a.getDimensionPixelOffset(1 /* android.R.attr.width */, 0);
        this.height = a.getDimensionPixelOffset(0 /* android.R.attr.height */, 0);
        DebugUtils.__checkError(width <= 0 || height <= 0, "The tag requires a valid 'width' or 'height' attribute");
        a.recycle();
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param config The {@link Config} to decode.
     * @param width The desired width to decode, in pixels.
     * @param height The desired height to decode, in pixels.
     * @param mutable Whether to decode a mutable bitmap.
     * @see #SizeParameters(Context, AttributeSet)
     */
    public SizeParameters(Context context, Config config, int width, int height, boolean mutable) {
        super(context.getResources().getDisplayMetrics().densityDpi, config, mutable);
        this.width  = width;
        this.height = height;
        DebugUtils.__checkError(width <= 0 || height <= 0, "width <= 0 || height <= 0");
    }

    @Override
    public int computeByteCount(Options opts) {
        return computeByteCountImpl(opts);
    }

    @Override
    public void computeSampleSize(Options opts) {
        /*
         * Scale width and height.
         *      scaleX = opts.outWidth  / width;
         *      scaleY = opts.outHeight / height;
         *      scale  = max(scaleX, scaleY);
         */
        opts.inSampleSize = 1;
        if (opts.outWidth > width && opts.outHeight > height) {
            final float scale = Math.max((float)opts.outWidth / width, (float)opts.outHeight / height);
            final int screenDensity = (int)value;
            opts.inTargetDensity = screenDensity;
            opts.inDensity = (int)(screenDensity * scale + 0.5f);
        } else {
            opts.inDensity = opts.inTargetDensity = 0;
        }
    }

    @Override
    public void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { config = ").append(config.name())
            .append(", width = ").append(width)
            .append(", height = ").append(height)
            .append(", screenDensity = ").append(DeviceUtils.toDensity((int)value))
            .append(", mutable = ").append(mutable)
            .append(" }").toString());
    }
}
