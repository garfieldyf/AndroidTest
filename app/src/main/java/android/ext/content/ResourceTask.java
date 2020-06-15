package android.ext.content;

import static android.ext.content.ResourceLoader.download;
import static android.ext.content.ResourceLoader.loadFromCache;
import static android.ext.content.ResourceLoader.parseResult;
import android.app.Activity;
import android.content.Context;
import android.ext.content.ResourceLoader.LoadParams;
import android.ext.content.ResourceLoader.OnLoadCompleteListener;
import android.ext.util.DebugUtils;
import java.io.File;

/**
 * Class <tt>ResourceTask</tt> allows to load the resource on a background thread
 * and publish results on the UI thread. This class can be support the cache file.
 * <h3>ResourceTask's generic types</h3>
 * <p>The two types used by a task are the following:</p>
 * <ol><li><tt>Key</tt>, The type of the key sent to the task.</li>
 * <li><tt>Result</tt>, The type of the result of the task.</li></ol>
 * <h3>Usage</h3>
 * <p>Here is an example:</p><pre>
 * private static class LoadCompleteListener implements OnLoadCompleteListener&lt;String, JSONObject&gt; {
 *    {@code @Override}
 *     public void onLoadComplete(String key, LoadParams&lt;String, JSONObject&gt; loadParams, Object cookie, JSONObject result) {
 *         if (result != null) {
 *             // Loading succeeded, update UI.
 *         } else {
 *             // Loading failed, show error or empty UI.
 *         }
 *     }
 * }
 *
 * new ResourceTask&lt;String, JSONObject&gt;(activity, url)
 *    .setCookie(cookie)
 *    .setOnLoadCompleteListener(new LoadCompleteListener())
 *    .execute(loadParams);</pre>
 * @author Garfield
 */
public class ResourceTask<Key, Result> extends AbsAsyncTask<LoadParams<Key, Result>, Object, Result> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * The key of this task.
     */
    protected final Key mKey;

    /**
     * The Object by user-defined.
     */
    protected Object mCookie;

    /**
     * The parameters of this task.
     */
    protected LoadParams<Key, Result> mLoadParams;

    /**
     * The {@link OnLoadCompleteListener} to receive callbacks when a load is complete.
     */
    protected OnLoadCompleteListener<Key, Result> mListener;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param key The key of this task.
     * @see #ResourceTask(Activity, Key)
     */
    public ResourceTask(Context context, Key key) {
        mKey = key;
        mContext = context.getApplicationContext();
    }

    /**
     * Constructor
     * @param ownerActivity The owner <tt>Activity</tt>.
     * @param key The key of this task.
     * @see #ResourceTask(Context, Key)
     */
    public ResourceTask(Activity ownerActivity, Key key) {
        super(ownerActivity);
        mKey = key;
        mContext = ownerActivity.getApplicationContext();
    }

    /**
     * Sets an object that gets passed into {@link OnLoadCompleteListener#onLoadComplete}.
     * @param cookie An object by user-defined.
     * @return This <em>task</em>.
     */
    public final ResourceTask<Key, Result> setCookie(Object cookie) {
        mCookie = cookie;
        return this;
    }

    /**
     * Sets An {@link OnLoadCompleteListener} to receive callbacks when a load is complete.
     * @param listener The <tt>OnLoadCompleteListener</tt>.
     * @return This <em>task</em>.
     */
    public final ResourceTask<Key, Result> setOnLoadCompleteListener(OnLoadCompleteListener<Key, Result> listener) {
        mListener = listener;
        return this;
    }

    @Override
    protected void onPostExecute(Result result) {
        DebugUtils.__checkError(mListener == null, "The " + getClass().getName() + " did not call setOnLoadCompleteListener()");
        if (validateOwner(mOwner)) {
            mListener.onLoadComplete(mKey, mLoadParams, mCookie, result);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void onProgressUpdate(Object[] values) {
        onPostExecute((Result)values[0]);
    }

    @Override
    protected final Result doInBackground(LoadParams<Key, Result>[] loadParams) {
        mLoadParams = loadParams[0];
        final File cacheFile = mLoadParams.getCacheFile(mContext, mKey);
        if (cacheFile == null) {
            return parseResult(mContext, mKey, mLoadParams, this);
        }

        final Result result = loadFromCache(mContext, mKey, mLoadParams, cacheFile);
        final boolean hitCache = (result != null);
        if (hitCache) {
            // Loads from the cache file succeeded, update UI.
            publishProgress(result);
        }

        return (isCancelled() ? null : download(mContext, mKey, mLoadParams, this, cacheFile.getPath(), hitCache));
    }
}
