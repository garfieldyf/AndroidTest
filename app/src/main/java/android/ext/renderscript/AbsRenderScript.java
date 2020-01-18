package android.ext.renderscript;

import android.content.Context;
import android.renderscript.RenderScript;
import android.renderscript.Script;
import java.io.Closeable;

/**
 * Class AbsRenderScript
 * @author Garfield
 */
/* package */ abstract class AbsRenderScript<T extends Script> implements Closeable {
    /* package */ T mScript;
    /* package */ RenderScript mRS;
    /* package */ final boolean mShouldDestroy;

    /**
     * Constructor
     * @see #AbsRenderScript(RenderScript, Script)
     */
    /* package */ AbsRenderScript(Context context) {
        mShouldDestroy = true;
        mRS = RenderScript.create(context.getApplicationContext());
    }

    /**
     * Constructor
     * @see #AbsRenderScript(Context)
     */
    /* package */ AbsRenderScript(RenderScript rs, T script) {
        mRS = rs;
        mScript = script;
        mShouldDestroy = false;
    }

    @Override
    public synchronized void close() {
        if (mScript != null) {
            mScript.destroy();
            mScript = null;
        }

        if (mShouldDestroy && mRS != null) {
            mRS.destroy();
            mRS = null;
        }
    }
}
