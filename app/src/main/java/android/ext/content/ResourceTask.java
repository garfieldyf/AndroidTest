package android.ext.content;

import static android.ext.content.ResourceLoader.download;
import static android.ext.content.ResourceLoader.loadFromCache;
import static android.ext.content.ResourceLoader.parseResult;
import android.content.Context;
import android.ext.content.ResourceLoader.LoadParams;
import android.ext.content.ResourceLoader.OnLoadCompleteListener;
import android.ext.net.DownloadRequest.DownloadCallback;
import android.ext.util.DebugUtils;
import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URLConnection;

/**
 * Class <tt>ResourceTask</tt> allows to load the resource from the web on a background
 * thread and publish results on the UI thread. This class can be support the cache file.
 * <h3>ResourceTask's generic types</h3>
 * <p>The two types used by a task are the following:</p>
 * <ol><li><tt>Key</tt>, The type of the key sent to the task.</li>
 * <li><tt>Result</tt>, The type of the result of the task.</li></ol>
 * <h3>Usage</h3>
 * <p>Here is an example:</p><pre>
 * new ResourceTask&lt;String, JSONObject&gt;(context, url)
 *    .setCookie(cookie)
 *    .setWeakOnLoadCompleteListener(listener)
 *    .setOwner(activity)   // May be an <tt>Activity, LifecycleOwner, Lifecycle</tt> or <tt>Fragment</tt> etc.
 *    .execute(executor, loadParams);</pre>
 * @author Garfield
 */
public class ResourceTask<Key, Result> extends AsyncTask<LoadParams<Key, Result>, Object, Result> implements DownloadCallback<Object, Integer> {
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
     * The {@link OnLoadCompleteListener} to receive callbacks when a load is complete.
     */
    private WeakReference<OnLoadCompleteListener<Key, Result>> mListener;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param key The key of this task.
     */
    public ResourceTask(Context context, Key key) {
        mKey = key;
        mContext = context.getApplicationContext();
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
     * The listener is internally held as {@link WeakReference weak reference}.
     * @param listener The <tt>OnLoadCompleteListener</tt> to set.
     * @return This <em>task</em>.
     */
    public final ResourceTask<Key, Result> setWeakOnLoadCompleteListener(OnLoadCompleteListener<Key, Result> listener) {
        mListener = new WeakReference<OnLoadCompleteListener<Key, Result>>(listener);
        return this;
    }

    @Override
    public Integer onDownload(URLConnection conn, int statusCode, Object[] params) throws Exception {
        return download(conn, statusCode, params);
    }

    @Override
    protected void onPostExecute(LoadParams<Key, Result>[] params, Result result) {
        DebugUtils.__checkError(mListener == null, "The " + getClass().getName() + " did not call setWeakOnLoadCompleteListener()");
        final OnLoadCompleteListener<Key, Result> listener = mListener.get();
        if (listener != null) {
            listener.onLoadComplete(mKey, mCookie, result);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void onProgressUpdate(Object[] values) {
        onPostExecute((LoadParams<Key, Result>[])mWorker.mParams, (Result)values[0]);
    }

    @Override
    protected final Result doInBackground(LoadParams<Key, Result>[] params) {
        final LoadParams<Key, Result> loadParams = params[0];
        final File cacheFile = loadParams.getCacheFile(mContext, mKey);
        if (cacheFile == null) {
            return parseResult(mContext, mWorker, mKey, loadParams);
        }

        final Result result = loadFromCache(mContext, mKey, loadParams, cacheFile);
        if (result != null) {
            // Loads from the cache file succeeded, update UI.
            setProgress(result);
        }

        return (isCancelled() ? null : download(mContext, mWorker, mKey, loadParams, cacheFile.getPath(), (result != null), this));
    }
}
