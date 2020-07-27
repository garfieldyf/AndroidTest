package android.ext.content;

import android.app.Activity;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.os.AsyncTask;
import java.lang.ref.WeakReference;

/**
 * Like as {@link AsyncTask}, but this class has an owner <tt>Object</tt> to avoid potential memory
 * leaks. The owner object may be a <tt>Activity</tt>, <tt>Dialog</tt> or <tt>Fragment</tt> etc.
 * <h3>Usage</h3>
 * <p>Here is an example of subclassing:</p><pre>
 * private static class DownloadTask extends AbsAsyncTask&lt;String, Object, Long&gt; {
 *     public DownloadTask(Activity ownerActivity) {
 *         super(ownerActivity);
 *     }
 *
 *    {@code @Override}
 *     protected Long doInBackground(String... urls) {
 *         ... ...
 *     }
 *
 *    {@code @Override}
 *     protected void onPostExecute(Long result) {
 *         final Activity activity = getOwnerActivity();
 *         if (activity == null) {
 *              // The owner activity has been destroyed or release by the GC.
 *              return;
 *         }
 *
 *         Log.i(TAG, "Downloaded " + result + " bytes");
 *     }
 * }
 *
 * new DownloadTask(activity).execute(url);</pre>
 * @see AbsAsyncTask#setOwner(Object)
 * @see AbsAsyncTask#getOwner()
 * @see AbsAsyncTask#getOwnerActivity()
 * @author Garfield
 */
public abstract class AbsAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements Cancelable {
    private WeakReference<Object> mOwner;

    /**
     * Constructor
     * @see #AbsAsyncTask(Object)
     */
    public AbsAsyncTask() {
        DebugUtils.__checkMemoryLeaks(getClass());
    }

    /**
     * Constructor
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AbsAsyncTask()
     */
    public AbsAsyncTask(Object owner) {
        DebugUtils.__checkMemoryLeaks(getClass());
        mOwner = new WeakReference<Object>(owner);
    }

    /**
     * Sets the object that owns this task.
     * @param owner The owner object.
     * @see #getOwner()
     * @see #getOwnerActivity()
     */
    public final void setOwner(Object owner) {
        mOwner = new WeakReference<Object>(owner);
    }

    /**
     * Returns the object that owns this task.
     * @return The owner object or <tt>null</tt> if the owner released by the GC.
     * @see #setOwner(Object)
     * @see #getOwnerActivity()
     */
    @SuppressWarnings("unchecked")
    public final <T> T getOwner() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        return (T)mOwner.get();
    }

    /**
     * Alias of {@link #getOwner()}.
     * @return The <tt>Activity</tt> that owns this task or <tt>null</tt> if
     * the owner activity has been finished or destroyed or release by the GC.
     * @see #getOwner()
     * @see #setOwner(Object)
     */
    @SuppressWarnings("unchecked")
    public final <T extends Activity> T getOwnerActivity() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        final T activity = (T)mOwner.get();
        return (activity != null && !activity.isFinishing() && !activity.isDestroyed() ? activity : null);
    }
}
