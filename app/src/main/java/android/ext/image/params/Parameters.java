package android.ext.image.params;

import android.annotation.WorkerThread;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.graphics.BitmapUtils;
import android.ext.util.DebugUtils;
import android.ext.util.ReflectUtils;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Printer;

/**
 * Class <tt>Parameters</tt> can be used to decode bitmap.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;[ Parameters | parameters ]
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      class="classFullName"
 *      app:sampleSize="2" /&gt;</pre>
 * @author Garfield
 */
public class Parameters {
    /**
     * The Object by user-defined to decode.
     */
    protected Object value;

    /**
     * Constructor
     * @param sampleSize The sample size to decode.
     * @see #Parameters(Context, AttributeSet)
     */
    public Parameters(int sampleSize) {
        this.value = fixSampleSize(sampleSize);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #Parameters(int)
     */
    public Parameters(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, ReflectUtils.getAttributes(context.getPackageName(), "Parameters"));
        this.value = fixSampleSize(a.getInt(0 /* R.styleable.Parameters_sampleSize */, 1));
        a.recycle();
    }

    /**
     * The default constructor used by subclasses.
     */
    protected Parameters() {
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
            .append(" { sampleSize = ").append(value)
            .append(" }").toString());
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
}
