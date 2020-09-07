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
 * new ResourceTask&lt;String, JSONObject&gt;(context, url)
 *    .setCookie(cookie)
 *    .setOnLoadCompleteListener(new LoadCompleteListener())
 *    .execute(loadParams);</pre>
 * @author Garfield
 */
public class ResourceTask<Key, Result> extends AbsAsyncTask<LoadParams<Key, Result>, Object, Result> implements DownloadCallback<Object, Integer> {
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
    protected OnLoadCompleteListener<Key, Result> mListener;

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
     * @param listener The <tt>OnLoadCompleteListener</tt>.
     * @return This <em>task</em>.
     */
    public final ResourceTask<Key, Result> setOnLoadCompleteListener(OnLoadCompleteListener<Key, Result> listener) {
        mListener = listener;
        return this;
    }

    @Override
    public Integer onDownload(URLConnection conn, int statusCode, Object[] params) throws Exception {
        return download(conn, statusCode, params);
    }

    @Override
    protected void onPostExecute(Result result) {
        DebugUtils.__checkError(mListener == null, "The " + getClass().getName() + " did not call setOnLoadCompleteListener()");
        mListener.onLoadComplete(mKey, mCookie, result);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void onProgressUpdate(Object[] values) {
        onPostExecute((Result)values[0]);
    }

    @Override
    protected final Result doInBackground(LoadParams<Key, Result>[] params) {
        final LoadParams<Key, Result> loadParams = params[0];
        final File cacheFile = loadParams.getCacheFile(mContext, mKey);
        if (cacheFile == null) {
            return parseResult(mContext, mKey, loadParams, this);
        }

        final Result result = loadFromCache(mContext, mKey, loadParams, cacheFile);
        if (result != null) {
            // Loads from the cache file succeeded, update UI.
            publishProgress(result);
        }

        return (isCancelled() ? null : download(mContext, mKey, loadParams, result, cacheFile.getPath(), this, this));
    }
}
