package android.ext.renderscript;

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
     */
    /* package */ AbsRenderScript(RenderScript rs, T script, boolean shouldDestroy) {
        mRS = rs;
        mScript = script;
        mShouldDestroy = shouldDestroy;
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
