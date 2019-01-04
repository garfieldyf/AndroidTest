package android.ext.content.image.params;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.AttributeSet;

/**
 * Class <tt>ScaleParameters</tt> is an implementation of a {@link Parameters}.
 * <h2>Usage</h2>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;ScaleParameters
 *      xmlns:android="http://schemas.android.com/apk/res/android"
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      android:width="200dp"
 *      android:height="300dp"
 *      app:config="[ argb_8888 | rgb_565 | alpha_8 ]" /&gt;</pre>
 * @author Garfield
 */
public class ScaleParameters extends SizeParameters {
    private final int mTargetDensity;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #ScaleParameters(Context, Config, int, int)
     */
    public ScaleParameters(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
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
        mTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
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
        opts.inSampleSize = 1;
        if (opts.outWidth <= (int)value || opts.outHeight <= desiredHeight) {
            opts.inDensity = opts.inTargetDensity = 0;
        } else {
            final float scale = Math.max((float)opts.outWidth / (int)value, (float)opts.outHeight / desiredHeight);
            opts.inTargetDensity = mTargetDensity;
            opts.inDensity = (int)(mTargetDensity * scale);
        }
    }
}
