package android.ext.content;

import static java.net.HttpURLConnection.HTTP_OK;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import android.app.Activity;
import android.content.Context;
import android.ext.net.DownloadRequest;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.os.Process;
import android.util.Log;

/**
 * Class <tt>CacheLoader</tt> allows to load the resource on a background thread
 * and publish results on the UI thread. This class can be support the cache file.
 * <h3>CacheLoader's generic types</h3>
 * <p>The two types used by a loader are the following:</p>
 * <ol><li><tt>Key</tt>, The loader's key type.</li>
 * <li><tt>Result</tt>, The load result type.</li></ol>
 * <h3>Usage</h3>
 * <p>Here is an example of subclassing:</p><pre>
 * public static class JsonLoadParams implements LoadParams&lt;String&gt; {
 *     {@code @Override}
 *     public File getCacheFile(Context context, String url) {
 *         // Builds the cache file, For example:
 *         return new File(context.getFilesDir(), "xxx/cacheFile.json");
 *     }
 *
 *     {@code @Override}
 *     public DownloadRequest newDownloadRequest(Context context, String url) throws Exception {
 *         return new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
 *     }
 *
 *     {@code @Override}
 *     public Object parseResult(Context context, String url, File file, Cancelable cancelable) throws Exception {
 *         if (!file.exists()) {
 *             // If the file not exists, return null or parse the JSON data from the "assets" file.
 *             return null;
 *         }
 *
 *         // Parse the file's content to a JSON object.
 *         final JSONObject result = JsonUtils.parse(context, file, cancelable);
 *
 *         // Check the result is valid.
 *         if (result != null && result.optInt("retCode") == 200) {
 *             return result;
 *         }
 *
 *         return null;
 *     }
 * }
 *
 * private CacheLoader&lt;String&gt; mLoader;
 *
 * mLoader = new CacheLoader&lt;String&gt;(activity, executor);
 * mLoader.load(url, new JsonLoadParams(), listener);</pre>
 * @author Garfield
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CacheLoader<Key, Result> extends AsyncTaskLoader<Key, Object, Result> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;
    private final ConcurrentHashMap<Task, Boolean> __checkTasks = new ConcurrentHashMap<Task, Boolean>();

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @see #CacheLoader(Activity, Executor)
     */
    public CacheLoader(Context context, Executor executor) {
        super(executor);
        mContext = context.getApplicationContext();
    }

    /**
     * Constructor
     * @param ownerActivity The owner <tt>Activity</tt>.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @see #CacheLoader(Context, Executor)
     */
    public CacheLoader(Activity ownerActivity, Executor executor) {
        super(executor, ownerActivity);
        mContext = ownerActivity.getApplicationContext();
    }

    /**
     * Equivalent to calling <tt>load(key, new Object[] { params, listener })</tt>.
     * @param key The identifier of the load task.
     * @param params The parameters of the load task.
     * @param listener The {@link OnLoadCompleteListener} to receive the load is complete.
     */
    public final void load(Key key, LoadParams<Key, Result> params, OnLoadCompleteListener<Key, Result> listener) {
        load(key, new Object[] { params, listener });
    }

    @Override
    protected Result loadInBackground(Task<?, ?> task, Key key, Object[] loadParams) {
        Object result = null;
        try {
            final LoadParams params = (LoadParams)loadParams[0];
            final File cacheFile = params.getCacheFile(mContext, key);
            if (cacheFile == null) {
                DebugUtils.__checkStartMethodTracing();
                result = download(task, key, params);
                DebugUtils.__checkStopMethodTracing("CacheLoader", "download");
            } else {
                final boolean hitCache = loadFromCache(task, key, params, cacheFile);
                if (!isTaskCancelled(task)) {
                    result = download(task, key, params, cacheFile.getPath(), hitCache);
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load resource - key = " + key + "\n" + e);
        }

        this.__checkTasks.remove(task);
        return (Result)result;
    }

    @Override
    protected void onLoadComplete(Key key, Object[] params, Result result) {
        if (checkOwner()) {
            ((OnLoadCompleteListener)params[1]).onLoadComplete(key, params, result);
        }
    }

    @Override
    protected void onProgressUpdate(Key key, Object[] params, Object[] values) {
        if (checkOwner()) {
            ((OnLoadCompleteListener)params[1]).onLoadComplete(key, params, values[0]);
        }
    }

    private Object download(Task task, Object key, LoadParams params) throws Exception {
        final File tempFile = new File(FileUtils.getCacheDir(mContext, null), Integer.toString(Thread.currentThread().hashCode()));
        try {
            final int statusCode = params.newDownloadRequest(mContext, key).download(tempFile.getPath(), task, null);
            return (statusCode == HTTP_OK && !isTaskCancelled(task) ? params.parseResult(mContext, key, tempFile, task) : null);
        } finally {
            tempFile.delete();
        }
    }

    private boolean loadFromCache(Task task, Object key, LoadParams params, File cacheFile) {
        final int priority = Process.getThreadPriority(Process.myTid());
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
            DebugUtils.__checkStartMethodTracing();
            final Object result = params.parseResult(mContext, key, cacheFile, task);
            DebugUtils.__checkStopMethodTracing("CacheLoader", "loadFromCache");
            if (result != null) {
                // If the task was cancelled then invoking setProgress has no effect.
                this.__checkIsCancelled(task);
                task.setProgress(result);
                return true;
            }
        } catch (Exception e) {
            Log.w(getClass().getName(), "Couldn't load resource from the cache - " + cacheFile.getPath());
        } finally {
            Process.setThreadPriority(priority);
        }

        return false;
    }

    private Object download(Task task, Object key, LoadParams params, String cacheFile, boolean hitCache) throws Exception {
        final String tempFile = cacheFile + "." + Thread.currentThread().hashCode();
        final int statusCode  = params.newDownloadRequest(mContext, key).download(tempFile, task, null);
        if (statusCode == HTTP_OK && !isTaskCancelled(task)) {
            // If the cache file is hit and the cache file's contents are equal the temp
            // file's contents. Deletes the temp file and returns null, do not update UI.
            if (hitCache && FileUtils.compareFile(cacheFile, tempFile)) {
                DebugUtils.__checkDebug(true, "CacheLoader", "The cache file's contents are equal the downloaded file's contents, do not update UI.");
                FileUtils.deleteFiles(tempFile, false);
                task.cancel(false);
                this.__checkCancel(task);
                return null;
            }

            // Parse the temp file and save it to the cache file.
            DebugUtils.__checkStartMethodTracing();
            final Object result = params.parseResult(mContext, key, new File(tempFile), task);
            DebugUtils.__checkStopMethodTracing("CacheLoader", "parseResult");
            if (result != null) {
                FileUtils.moveFile(tempFile, cacheFile);
                return result;
            }
        }

        return null;
    }

    private void __checkCancel(Task task) {
        this.__checkTasks.put(task, true);
    }

    private void __checkIsCancelled(Task task) {
        Boolean cancelled = null;
        cancelled = this.__checkTasks.get(task);
        if (cancelled != null && cancelled.booleanValue()) {
            throw new AssertionError("The task was cancelled, call setProgress has no effect.");
        }
    }

    /**
     * Class <tt>LoadParams</tt> used to {@link CacheLoader} to load resource.
     */
    public static interface LoadParams<Key, Result> {
        /**
         * Called on a background thread to returns the absolute path of the
         * cache file on the filesystem.
         * @param context The <tt>Context</tt>.
         * @param key The key, passed earlier by {@link #load}.
         * @return The path of the cache file, or <tt>null</tt> if no cache file.
         */
        File getCacheFile(Context context, Key key);

        /**
         * Called on a background thread to returns a new download request
         * with the specified <em>key</em>.
         * @param context The <tt>Context</tt>.
         * @param key The key, passed earlier by {@link #load}.
         * @return The {@link DownloadRequest} object.
         * @throws Exception if an error occurs while opening the connection.
         */
        DownloadRequest newDownloadRequest(Context context, Key key) throws Exception;

        /**
         * Called on a background thread to parse the data from the cache file.
         * @param context The <tt>Context</tt>.
         * @param key The key, passed earlier by {@link #load}.
         * @param file The file's content to parse.
         * @param cancelable A {@link Cancelable} can be check the parse is cancelled.
         * @return A result or <tt>null</tt>, defined by the subclass.
         * @throws Exception if the data can not be parse.
         */
        Result parseResult(Context context, Key key, File file, Cancelable cancelable) throws Exception;
    }

    /**
     * Callback interface when a {@link CacheLoader} has finished loading its data.
     */
    public static interface OnLoadCompleteListener<Key, Result> {
        /**
         * Called on the thread when the load is complete.
         * @param key The key, passed earlier by {@link #load}.
         * @param params The parameters, passed earlier by {@link #load}.
         * @param result A result or <tt>null</tt> of the load.
         */
        void onLoadComplete(Key key, Object[] params, Result result);
    }
}
