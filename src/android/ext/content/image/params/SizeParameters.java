package android.ext.content.image.params;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.graphics.BitmapUtils;
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
