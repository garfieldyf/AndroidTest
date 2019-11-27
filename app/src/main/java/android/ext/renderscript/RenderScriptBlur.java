package android.ext.renderscript;

import android.content.Context;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

/**
 * Class <tt>RenderScriptBlur</tt> used to blur the <tt>Bitmap</tt>.
 * @author Garfield
 */
public final class RenderScriptBlur extends AbsRenderScript<ScriptIntrinsicBlur> {
    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @see #RenderScriptBlur(RenderScript)
     */
    public RenderScriptBlur(Context context) {
        super(RenderScript.create(context), null, true);
        mScript = ScriptIntrinsicBlur.create(mRS, Element.U8_4(mRS));
    }

    /**
     * Constructor
     * @param rs The {@link RenderScript}.
     * @see #RenderScriptBlur(Context)
     */
    public RenderScriptBlur(RenderScript rs) {
        super(rs, ScriptIntrinsicBlur.create(rs, Element.U8_4(rs)), false);
    }

    /**
     * Blurs the given the <em>bitmap</em>.
     * @param bitmap The bitmap to blur, must be {@link Config#ARGB_8888} pixel format.
     * @param radius The radius of the blur, Supported range <tt>0 &lt; radius &lt;= 25</tt>.
     */
    public synchronized void blur(Bitmap bitmap, float radius) {
        DebugUtils.__checkError(bitmap == null, "bitmap == null");
        DebugUtils.__checkError(bitmap.getConfig() != Config.ARGB_8888, "The bitmap must be ARGB_8888 pixel format.");
        if (mScript != null) {
            final Allocation input  = Allocation.createFromBitmap(mRS, bitmap);
            final Allocation output = Allocation.createTyped(mRS, input.getType());

            try {
                mScript.setInput(input);
                mScript.setRadius(radius);
                mScript.forEach(output);
                output.copyTo(bitmap);
            } finally {
                input.destroy();
                output.destroy();
            }
        }
    }
}
