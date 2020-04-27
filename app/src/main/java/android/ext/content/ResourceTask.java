package android.ext.content;

import static java.net.HttpURLConnection.HTTP_OK;
import android.app.Activity;
import android.content.Context;
import android.ext.content.ResourceLoader.LoadParams;
import android.ext.content.ResourceLoader.OnLoadCompleteListener;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.os.Process;
import android.util.Log;
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
    protected final Result doInBackground(LoadParams<Key, Result>[] loadParams) {
        mLoadParams = loadParams[0];
        final File cacheFile = mLoadParams.getCacheFile(mContext, mKey);
        if (cacheFile == null) {
            return parseResult();
        } else {
            final boolean hitCache = loadFromCache(cacheFile);
            return (isCancelled() ? null : download(cacheFile.getPath(), hitCache));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void onProgressUpdate(Object[] values) {
        onPostExecute((Result)values[0]);
    }

    @Override
    protected void onPostExecute(Result result) {
        if (mListener != null && validateOwner(mOwner)) {
            mListener.onLoadComplete(mKey, mLoadParams, mCookie, result);
        }
    }

    private Result parseResult() {
        Result result = null;
        try {
            DebugUtils.__checkStartMethodTracing();
            result = mLoadParams.parseResult(mContext, mKey, null, this);
            DebugUtils.__checkStopMethodTracing("ResourceTask", "parse result - key = " + mKey);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't parse result - key = " + mKey + "\n" + e);
        }

        return result;
    }

    private boolean loadFromCache(File cacheFile) {
        final int priority = Process.getThreadPriority(Process.myTid());
        try {
            DebugUtils.__checkStartMethodTracing();
            Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
            final Result result = mLoadParams.parseResult(mContext, mKey, cacheFile, this);
            DebugUtils.__checkStopMethodTracing("ResourceTask", "loadFromCache - key = " + mKey + ", cacheFile = " + cacheFile);
            if (result != null) {
                // If this task was cancelled then invoking publishProgress has no effect.
                publishProgress(result);
                return true;
            }
        } catch (Exception e) {
            Log.w(getClass().getName(), "Couldn't load resource from the cache - " + cacheFile);
        } finally {
            Process.setThreadPriority(priority);
        }

        return false;
    }

    private Result download(String cacheFile, boolean hitCache) {
        final String tempFile = cacheFile + "." + Thread.currentThread().hashCode();
        Result result = null;
        try {
            final int statusCode = mLoadParams.newDownloadRequest(mContext, mKey).download(tempFile, this, null);
            if (statusCode == HTTP_OK && !isCancelled() && !(hitCache && FileUtils.compareFile(cacheFile, tempFile))) {
                // If the cache file is not equals the temp file, parse the temp file.
                DebugUtils.__checkStartMethodTracing();
                result = mLoadParams.parseResult(mContext, mKey, new File(tempFile), this);
                DebugUtils.__checkStopMethodTracing("ResourceTask", DebugUtils.toString(result, new StringBuilder("downloads - result = ")).append(", key = ").append(mKey).toString());
                if (result != null) {
                    // Save the temp file to the cache file.
                    FileUtils.moveFile(tempFile, cacheFile);
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load resource - key = " + mKey + "\n" + e);
        }

        if (hitCache && result == null) {
            // If the cache file is hit and parse the temp file failed,
            // cancel this task and delete the temp file, do not update UI.
            DebugUtils.__checkDebug(true, "ResourceTask", "cancel task - key = " + mKey);
            cancel(false);
            FileUtils.deleteFiles(tempFile, false);
        }

        return result;
    }
}
