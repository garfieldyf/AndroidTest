package android.ext.renderscript;

import android.content.Context;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicColorMatrix;
import android.renderscript.ScriptIntrinsicYuvToRGB;

/**
 * Class <tt>RenderScriptYuvToRGB</tt> used to converting a YUV data to the <tt>Bitmap</tt>.
 * @author Garfield
 */
public final class RenderScriptYuvToRGB extends AbsRenderScript<ScriptIntrinsicYuvToRGB> {
    private ScriptIntrinsicColorMatrix mColorMatrix;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @see #RenderScriptYuvToRGB(RenderScript)
     */
    public RenderScriptYuvToRGB(Context context) {
        super(RenderScript.create(context), null, true);
        mScript = ScriptIntrinsicYuvToRGB.create(mRS, Element.YUV(mRS));
    }

    /**
     * Constructor
     * @param rs The {@link RenderScript}.
     * @see #RenderScriptYuvToRGB(Context)
     */
    public RenderScriptYuvToRGB(RenderScript rs) {
        super(rs, ScriptIntrinsicYuvToRGB.create(rs, Element.YUV(rs)), false);
    }

    @Override
    public synchronized void close() {
        super.close();
        if (mColorMatrix != null) {
            mColorMatrix.destroy();
            mColorMatrix = null;
        }
    }

    /**
     * Converts a YUV data to the {@link Bitmap}.
     * @param yuvData The array of YUV data.
     * @param width The <em>yuvData</em> width in pixels.
     * @param height The <em>yuvData</em> height in pixels.
     * @param grayscale Whether to convert to grey-scale bitmap.
     * @return The converted <tt>Bitmap</tt> or <tt>null</tt>.
     * @see #convert(byte[], Bitmap, boolean)
     */
    public synchronized Bitmap convert(byte[] yuvData, int width, int height, boolean grayscale) {
        DebugUtils.__checkError(yuvData == null || width <= 0 || height <= 0, "yuvData == null || width <= 0 || height <= 0");
        if (mScript != null) {
            final Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            convertImpl(yuvData, result, grayscale);
            return result;
        }

        return null;
    }

    /**
     * Converts a YUV data into the <em>outBitmap</em>. The bitmap must match the dimensions of the YUV data.
     * @param yuvData The array of YUV data.
     * @param outBitmap The {@link Bitmap} to store the result, must be {@link Config#ARGB_8888} pixel format.
     * @param grayscale Whether to convert to grey-scale bitmap.
     * @see #convert(byte[], int, int, boolean)
     */
    public synchronized void convert(byte[] yuvData, Bitmap outBitmap, boolean grayscale) {
        DebugUtils.__checkError(yuvData == null || outBitmap == null, "yuvData == null || outBitmap == null");
        DebugUtils.__checkError(outBitmap.getConfig() != Config.ARGB_8888, "The outBitmap must be ARGB_8888 pixel format.");
        if (mScript != null) {
            convertImpl(yuvData, outBitmap, grayscale);
        }
    }

    private void gray(Allocation input, Bitmap outBitmap) {
        if (mColorMatrix == null) {
            mColorMatrix = ScriptIntrinsicColorMatrix.create(mRS);
            mColorMatrix.setGreyscale();
        }

        final Allocation output = Allocation.createFromBitmap(mRS, outBitmap);
        try {
            mColorMatrix.forEach(input, output);
            output.copyTo(outBitmap);
        } finally {
            output.destroy();
        }
    }

    private void convertImpl(byte[] yuvData, Bitmap outBitmap, boolean grayscale) {
        final Allocation input  = Allocation.createSized(mRS, Element.U8(mRS), yuvData.length);
        final Allocation output = Allocation.createFromBitmap(mRS, outBitmap);

        try {
            input.copyFrom(yuvData);
            mScript.setInput(input);
            mScript.forEach(output);
            if (grayscale) {
                gray(output, outBitmap);
            } else {
                output.copyTo(outBitmap);
            }
        } finally {
            input.destroy();
            output.destroy();
        }
    }
}
