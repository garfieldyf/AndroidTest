package android.ext.content;

import static java.net.HttpURLConnection.HTTP_OK;
import java.io.File;
import java.lang.ref.WeakReference;
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
 * Class <tt>CachedTaskLoader</tt> allows to load the resource on a background thread
 * and publish results on the UI thread. This class can be support the cache file.
 * <h3>CachedTaskLoader's generic types</h3>
 * <p>The two types used by a loader are the following:</p>
 * <ol><li><tt>Key</tt>, The loader's key type.</li>
 * <li><tt>Result</tt>, The load result type.</li></ol>
 * <h3>Usage</h3>
 * <p>Here is an example of subclassing:</p><pre>
 * public static class JSONLoadParams implements LoadParams&lt;String, JSONObject&gt; {
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
 *     public JSONObject parseResult(Context context, String url, File cacheFile, Cancelable cancelable) throws Exception {
 *         final JSONObject result;
 *         if (cacheFile == null) {
 *             // If no cache file, parse the JSON data from the network.
 *             result = newDownloadRequest(context, url).download(cancelable);
 *         } else if (cacheFile.exists()) {
 *             // Parse the JSON data from the cache file.
 *             result = JsonUtils.parse(context, cacheFile, cancelable);
 *         } else {
 *             // If the cache file not exists, parse the JSON data from the "assets" file.
 *             result = JsonUtils.parse(context, UriUtils.getAssetUri("cacheFile.json"), cancelable);
 *             // or return null
 *             return null;
 *         }
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
 * private CachedTaskLoader&lt;String, JSONObject&gt; mLoader;
 *
 * mLoader = new CachedTaskLoader&lt;String, JSONObject&gt;(activity, executor);
 * mLoader.load(url, new JSONLoadParams(), listener, cookie);</pre>
 * @author Garfield
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CachedTaskLoader<Key, Result> extends Loader<Key> {
    private static final int MAX_POOL_SIZE = 8;

    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * The owner of this loader.
     */
    private WeakReference<Object> mOwner;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @see #CachedTaskLoader(Activity, Executor)
     */
    public CachedTaskLoader(Context context, Executor executor) {
        super(executor, MAX_POOL_SIZE);
        mContext = context.getApplicationContext();
    }

    /**
     * Constructor
     * @param ownerActivity The owner <tt>Activity</tt>.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @see #CachedTaskLoader(Context, Executor)
     */
    public CachedTaskLoader(Activity ownerActivity, Executor executor) {
        super(executor, MAX_POOL_SIZE);
        mOwner = new WeakReference<Object>(ownerActivity);
        mContext = ownerActivity.getApplicationContext();
    }

    /**
     * Executes the load task on a background thread. If the <em>key</em> is mapped to the task is running then
     * invoking this method has no effect.<p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param key The identifier of the load task.
     * @param loadParams The {@link LoadParams} to passed to load task.
     * @param listener An {@link OnLoadCompleteListener} to receive callbacks when a load is complete.
     * @param cookie An object by user-defined that gets passed into {@link OnLoadCompleteListener#onLoadComplete}.
     */
    public final void load(Key key, LoadParams<Key, Result> loadParams, OnLoadCompleteListener<Key, Result> listener, Object cookie) {
        DebugUtils.__checkUIThread("load");
        DebugUtils.__checkError(key == null || loadParams == null || listener == null, "key == null || loadParams == null || listener == null");
        if (mState != SHUTDOWN) {
            final Task task = mRunningTasks.get(key);
            if (task == null || task.isCancelled()) {
                final LoadTask newTask = obtain(key, loadParams, cookie, listener);
                mRunningTasks.put(key, newTask);
                mExecutor.execute(newTask);
            }
        }
    }

    /**
     * Sets the object that owns this loader.
     * @param owner The owner object.
     * @see #getOwner()
     */
    public final void setOwner(Object owner) {
        mOwner = new WeakReference<Object>(owner);
    }

    /**
     * Returns the object that owns this loader.
     * @return The owner object or <tt>null</tt>
     * if the owner released by the GC.
     * @see #setOwner(Object)
     */
    public final <T> T getOwner() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        return (T)mOwner.get();
    }

    @Override
    public final Task newInstance() {
        return new LoadTask();
    }

    /**
     * Called on a background thread to load the resource.
     * @param task The current {@link Task} whose executing this method.
     * @param key The key, passed earlier by {@link #load}.
     * @param loadParams The parameters, passed earlier by {@link #load}.
     * @param cookie An object, passed earlier by {@link #load}.
     * @return A result or <tt>null</tt> of the load.
     */
    protected Result loadInBackground(Task<?, ?> task, Key key, LoadParams<Key, Result> loadParams, Object cookie) {
        Object result = null;
        try {
            final File cacheFile = loadParams.getCacheFile(mContext, key);
            if (cacheFile == null) {
                DebugUtils.__checkStartMethodTracing();
                result = loadParams.parseResult(mContext, key, null, task);
                DebugUtils.__checkStopMethodTracing("CachedTaskLoader", "parseResult");
            } else {
                final boolean hitCache = loadFromCache(task, key, loadParams, cacheFile);
                if (!isTaskCancelled(task)) {
                    result = download(task, key, loadParams, cacheFile.getPath(), hitCache);
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load resource - key = " + key + "\n" + e);
        }

        return (Result)result;
    }

    /* package */ final boolean validateOwner() {
        if (mOwner != null) {
            final Object owner = mOwner.get();
            if (owner == null) {
                return false;
            } else if (owner instanceof Activity) {
                final Activity activity = (Activity)owner;
                return (!activity.isFinishing() && !activity.isDestroyed());
            }
        }

        return true;
    }

    private LoadTask obtain(Key key, LoadParams loadParams, Object cookie, OnLoadCompleteListener listener) {
        final LoadTask task = (LoadTask)mTaskPool.obtain();
        task.mKey = key;
        task.mParams = cookie;
        task.mListener = listener;
        task.mLoadParams = loadParams;
        return task;
    }

    private boolean loadFromCache(Task task, Object key, LoadParams loadParams, File cacheFile) {
        final int priority = Process.getThreadPriority(Process.myTid());
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
            DebugUtils.__checkStartMethodTracing();
            final Object result = loadParams.parseResult(mContext, key, cacheFile, task);
            DebugUtils.__checkStopMethodTracing("CachedTaskLoader", "loadFromCache");
            if (result != null) {
                // If the task was cancelled then invoking setProgress has no effect.
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

    private Object download(Task task, Object key, LoadParams loadParams, String cacheFile, boolean hitCache) throws Exception {
        final String tempFile = cacheFile + "." + Thread.currentThread().hashCode();
        final int statusCode  = loadParams.newDownloadRequest(mContext, key).download(tempFile, task, null);
        if (statusCode == HTTP_OK && !isTaskCancelled(task)) {
            // If the cache file is hit and the cache file's contents are equal the temp
            // file's contents. Deletes the temp file and cancel the task, do not update UI.
            if (hitCache && FileUtils.compareFile(cacheFile, tempFile)) {
                DebugUtils.__checkDebug(true, "CachedTaskLoader", "The cache file's contents are equal the downloaded file's contents, do not update UI.");
                FileUtils.deleteFiles(tempFile, false);
                task.cancel(false);
                return null;
            }

            // Parse the temp file and save it to the cache file.
            DebugUtils.__checkStartMethodTracing();
            final Object result = loadParams.parseResult(mContext, key, new File(tempFile), task);
            DebugUtils.__checkStopMethodTracing("CachedTaskLoader", "download");
            if (result != null) {
                FileUtils.moveFile(tempFile, cacheFile);
                return result;
            }
        }

        return null;
    }

    /**
     * Class <tt>LoadTask</tt> is an implementation of a {@link Task}.
     */
    /* package */ final class LoadTask extends Task {
        /* package */ Key mKey;
        /* package */ LoadParams mLoadParams;
        /* package */ OnLoadCompleteListener mListener;

        @Override
        public void onProgress(Object value) {
            if (validateOwner()) {
                mListener.onLoadComplete(mKey, mLoadParams, mParams, value);
            }
        }

        @Override
        public Object doInBackground(Object params) {
            waitResumeIfPaused();
            return (mState != SHUTDOWN && !isCancelled() ? loadInBackground(this, mKey, mLoadParams, mParams) : null);
        }

        @Override
        public void onPostExecute(Object result) {
            if (mState != SHUTDOWN) {
                // Removes the finished task from running
                // tasks, excluding the cancelled task.
                if (mRunningTasks.get(mKey) == this) {
                    mRunningTasks.remove(mKey);
                }

                if (!isCancelled() && validateOwner()) {
                    mListener.onLoadComplete(mKey, mLoadParams, mParams, result);
                }
            }

            // Recycles this task to avoid potential memory
            // leaks, Even the loader has been shut down.
            clearForRecycle();
            mKey = null;
            mListener   = null;
            mLoadParams = null;
            mTaskPool.recycle(this);
        }
    }

    /**
     * Class <tt>LoadParams</tt> used to {@link CachedTaskLoader} to load resource.
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
         * @param cacheFile The cache file's content to parse, or <tt>null</tt> if no cache file.
         * @param cancelable A {@link Cancelable} can be check the parse is cancelled.
         * @return A result or <tt>null</tt>, defined by the subclass.
         * @throws Exception if the data can not be parse.
         */
        Result parseResult(Context context, Key key, File cacheFile, Cancelable cancelable) throws Exception;
    }

    /**
     * Callback interface when a {@link CachedTaskLoader} has finished loading its data.
     */
    public static interface OnLoadCompleteListener<Key, Result> {
        /**
         * Called on the UI thread when the load is complete.
         * @param key The key, passed earlier by {@link CachedTaskLoader#load}.
         * @param loadParams The parameters, passed earlier by {@link CachedTaskLoader#load}.
         * @param cookie An object, passed earlier by {@link CachedTaskLoader#load}.
         * @param result A result or <tt>null</tt> of the load.
         */
        void onLoadComplete(Key key, LoadParams<Key, Result> loadParams, Object cookie, Result result);
    }
}
