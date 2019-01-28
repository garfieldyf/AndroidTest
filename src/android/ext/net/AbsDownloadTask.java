package android.ext.net;

import java.lang.ref.WeakReference;
import android.app.Activity;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.os.AsyncTask;

/**
 * Class AbsDownloadTask
 * @author Garfield
 */
@SuppressWarnings("unchecked")
public abstract class AbsDownloadTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements Cancelable {
    private WeakReference<Object> mOwner;

    /**
     * Constructor
     * @see #AbsDownloadTask(Object)
     */
    public AbsDownloadTask() {
        DebugUtils.__checkMemoryLeaks(getClass());
    }

    /**
     * Constructor
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AbsDownloadTask()
     */
    public AbsDownloadTask(Object owner) {
        DebugUtils.__checkMemoryLeaks(getClass());
        mOwner = new WeakReference<Object>(owner);
    }

    /**
     * Returns the object that owns this task.
     * @return The owner object or <tt>null</tt> if the owner released by the GC.
     * @see #setOwner(Object)
     */
    public final <T> T getOwner() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        return (T)mOwner.get();
    }

    /**
     * Alias of {@link #getOwner()}.
     * @return The <tt>Activity</tt> that owns this task or <tt>null</tt> if
     * the owner activity has been finished or destroyed or release by the GC.
     * @see #setOwner(Object)
     */
    public final <T extends Activity> T getOwnerActivity() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        final T activity = (T)mOwner.get();
        return (activity != null && !activity.isFinishing() && !activity.isDestroyed() ? activity : null);
    }

    /**
     * Sets the object that owns this task.
     * @param owner The owner object.
     * @return This task.
     * @see #getOwner()
     * @see #getOwnerActivity()
     */
    public final AbsDownloadTask<Params, Progress, Result> setOwner(Object owner) {
        mOwner = new WeakReference<Object>(owner);
        return this;
    }

    /**
     * Returns a new download request with the specified <em>params</em>.
     * @param params The parameters of this task, passed earlier by {@link #execute(Params[])}.
     * @return The instance of {@link DownloadRequest}.
     * @throws Exception if an error occurs while opening the connection.
     */
    protected abstract DownloadRequest newDownloadRequest(Params[] params) throws Exception;
}
