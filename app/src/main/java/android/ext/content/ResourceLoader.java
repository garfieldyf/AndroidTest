package android.ext.content;

import static java.net.HttpURLConnection.HTTP_OK;
import android.app.Activity;
import android.content.Context;
import android.ext.net.DownloadRequest;
import android.ext.net.DownloadRequest.DownloadCallback;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.FileUtils;
import android.os.Process;
import android.util.Log;
import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URLConnection;
import java.util.concurrent.Executor;

/**
 * Class <tt>ResourceLoader</tt> allows to load the resource from the web on a background
 * thread and publish results on the UI thread. This class can be support the cache file.
 * <h3>ResourceLoader's generic types</h3>
 * <p>The two types used by a loader are the following:</p>
 * <ol><li><tt>Key</tt>, The loader's key type.</li>
 * <li><tt>Result</tt>, The load result type.</li></ol>
 * <h3>Usage</h3>
 * <p>Here is an example:</p><pre>
 * private static class JSONLoadParams implements LoadParams&lt;String, JSONObject&gt; {
 *    {@code @Override}
 *     public File getCacheFile(Context context, String url) {
 *         // Builds the cache file, For example:
 *         return new File(context.getFilesDir(), "xxx/cacheFile.json");
 *     }
 *
 *    {@code @Override}
 *     public DownloadRequest newDownloadRequest(Context context, String url) throws Exception {
 *         return new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
 *     }
 *
 *    {@code @Override}
 *     public JSONObject parseResult(Context context, String url, File cacheFile, Cancelable cancelable) throws Exception {
 *         final JSONObject result;
 *         if (cacheFile == null) {
 *             // If no cache file, parse the JSON data from the network.
 *             result = newDownloadRequest(context, url).download(cancelable);
 *         } else if (cacheFile.exists()) {
 *             // Parse the JSON data from the cache file.
 *             result = JSONUtils.parse(context, cacheFile, cancelable);
 *         } else {
 *             // Parse the JSON data from the assets.
 *             result = JSONUtils.parse(context, UriUtils.getAssetUri("xxx/cacheFile.json"), cancelable);
 *             // or
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
 * private static class LoadCompleteListener implements OnLoadCompleteListener&lt;String, JSONObject&gt; {
 *    {@code @Override}
 *     public void onLoadComplete(String key, Object cookie, JSONObject result) {
 *         if (result != null) {
 *             // Loading succeeded, update UI.
 *         } else {
 *             // Loading failed, show error or empty UI.
 *         }
 *     }
 * }
 *
 * private ResourceLoader&lt;String, JSONObject&gt; mLoader;
 *
 * mLoader = new ResourceLoader&lt;String, JSONObject&gt;(context, executor);
 * mLoader.load(url, new JSONLoadParams(), new LoadCompleteListener(), cookie);</pre>
 * @author Garfield
 */
public class ResourceLoader<Key, Result> extends Loader<Key> implements DownloadCallback<Object, Integer> {
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
     */
    public ResourceLoader(Context context, Executor executor) {
        super(executor, 8);
        mContext = context.getApplicationContext();
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
        DebugUtils.__checkError(key == null || loadParams == null || listener == null, "Invalid parameters - key == null || loadParams == null || listener == null");
        if (mState != SHUTDOWN) {
            final Task task = mRunningTasks.get(key);
            DebugUtils.__checkDebug(task != null && !task.isCancelled(), "ResourceLoader", "The task is already running - key = " + key);
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
     * @see #getOwnerActivity()
     */
    public final void setOwner(Object owner) {
        mOwner = new WeakReference<Object>(owner);
    }

    /**
     * Returns the object that owns this loader.
     * @return The owner object or <tt>null</tt>
     * if the owner released by the GC.
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
     * @return The <tt>Activity</tt> that owns this loader or <tt>null</tt> if
     * the owner activity has been finished or destroyed or released by the GC.
     * @see #getOwner()
     * @see #setOwner(Object)
     */
    @SuppressWarnings("unchecked")
    public final <T extends Activity> T getOwnerActivity() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        final T activity = (T)mOwner.get();
        return (activity != null && !activity.isFinishing() && !activity.isDestroyed() ? activity : null);
    }

    @Override
    public final Task newInstance() {
        return new LoadTask();
    }

    @Override
    public Integer onDownload(URLConnection conn, int statusCode, Object[] params) throws Exception {
        return download(conn, statusCode, params);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private LoadTask obtain(Key key, LoadParams loadParams, Object cookie, OnLoadCompleteListener listener) {
        final LoadTask task = (LoadTask)mTaskPool.obtain();
        task.mKey = key;
        task.mParams = cookie;
        task.mListener = listener;
        task.mLoadParams = loadParams;
        return task;
    }

    /* package */ static int download(URLConnection conn, int statusCode, Object[] params) throws Exception {
        if (statusCode == HTTP_OK) {
            try (final InputStream is = conn.getInputStream()) {
                FileUtils.copyStream(is, (File)params[0], (Cancelable)params[1]);
            }
        }

        return statusCode;
    }

    /* package */ static <Key, Result> Result loadFromCache(Context context, Key key, LoadParams<Key, Result> loadParams, File cacheFile) {
        final int priority = Process.getThreadPriority(Process.myTid());
        Result result = null;
        try {
            DebugUtils.__checkStartMethodTracing();
            Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
            result = loadParams.parseResult(context, key, cacheFile, null);
            DebugUtils.__checkStopMethodTracing("ResourceLoader", "loadFromCache - key = " + key + ", cacheFile = " + cacheFile);
        } catch (Exception e) {
            Log.w(ResourceLoader.class.getName(), "Couldn't load resource from the cache - " + cacheFile);
        } finally {
            Process.setThreadPriority(priority);
        }

        return result;
    }

    /* package */ static <Key, Result> Result parseResult(Context context, Key key, LoadParams<Key, Result> loadParams, Cancelable cancelable) {
        Result result = null;
        try {
            DebugUtils.__checkStartMethodTracing();
            result = loadParams.parseResult(context, key, null, cancelable);
            DebugUtils.__checkStopMethodTracing("ResourceLoader", "parse result - key = " + key);
        } catch (Exception e) {
            Log.e(ResourceLoader.class.getName(), "Couldn't parse result - key = " + key + "\n" + e);
        }

        return result;
    }

    /* package */ static <Key, Result> Result download(Context context, Key key, LoadParams<Key, Result> loadParams, DownloadCallback<Object, Integer> callback, Cancelable cancelable, File cacheFile, boolean hitCache) {
        final String cachePath = cacheFile.getPath();
        final File tempFile = new File(cachePath + ".tmp");
        Result result = null;
        try {
            final int statusCode = loadParams.newDownloadRequest(context, key).download(callback, tempFile, cancelable);
            if (statusCode == HTTP_OK && !cancelable.isCancelled() && !(hitCache && FileUtils.compareFile(cachePath, tempFile.getPath()))) {
                // If the cache file is not equals the temp file, parse the temp file.
                DebugUtils.__checkStartMethodTracing();
                result = loadParams.parseResult(context, key, tempFile, null);
                DebugUtils.__checkStopMethodTracing("ResourceLoader", DeviceUtils.toString(result, new StringBuilder("downloads - result = ")).append(", key = ").append(key).toString());
                if (result != null) {
                    // Save the temp file to the cache file.
                    FileUtils.moveFile(tempFile.getPath(), cachePath);
                    DebugUtils.__checkDebug(true, "ResourceLoader", "save the cache file = " + cachePath + ", hitCache = " + hitCache);
                }
            }
        } catch (Exception e) {
            tempFile.delete();
            Log.e(ResourceLoader.class.getName(), "Couldn't load resource - key = " + key + "\n" + e);
        }

        /*
         * If the cache file is hit and result is null:
         *   1. If download failed or cancelled.
         *   2. The cache file's contents and the temp file' contents are equal.
         *   3. Parse the temp file failed.
         * Cancel the task and delete the temp file, do not update UI.
         */
        if (hitCache && result == null) {
            DebugUtils.__checkDebug(true, "ResourceLoader", "cancel task - key = " + key);
            tempFile.delete();
            cancelable.cancel(false);
        }

        return result;
    }

    /**
     * Class <tt>LoadTask</tt> is an implementation of a {@link Task}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    /* package */ final class LoadTask extends Task {
        /* package */ Key mKey;
        /* package */ LoadParams mLoadParams;
        /* package */ OnLoadCompleteListener mListener;

        @Override
        public Object doInBackground(Object params) {
            final File cacheFile = mLoadParams.getCacheFile(mContext, mKey);
            if (cacheFile == null) {
                return parseResult(mContext, mKey, mLoadParams, this);
            }

            final Object result = loadFromCache(mContext, mKey, mLoadParams, cacheFile);
            final boolean hitCache = (result != null);
            if (hitCache) {
                // Loads from the cache file succeeded, update UI.
                setProgress(result);
            }

            return (isTaskCancelled(this) ? null : download(mContext, mKey, mLoadParams, ResourceLoader.this, this, cacheFile, hitCache));
        }

        @Override
        public void onProgress(Object value) {
            if (mState != SHUTDOWN) {
                mListener.onLoadComplete(mKey, mParams, value);
            }
        }

        @Override
        public void onPostExecute(Object result) {
            if (!isTaskCancelled(mKey, this)) {
                mListener.onLoadComplete(mKey, mParams, result);
            }

            // Recycles this task to avoid potential memory leaks.
            clearForRecycle();
            mKey = null;
            mListener = null;
            mLoadParams = null;
            mTaskPool.recycle(this);
        }
    }

    /**
     * Class <tt>LoadParams</tt> used to {@link ResourceLoader} to load resource.
     */
    public static interface LoadParams<Key, Result> {
        /**
         * Called on a background thread to returns the absolute path of the cache
         * file on the filesystem. The default implementation returns <tt>null</tt>.
         * @param context The <tt>Context</tt>.
         * @param key The key, passed earlier by {@link #load}.
         * @return The path of the cache file, or <tt>null</tt> if no cache file.
         */
        default File getCacheFile(Context context, Key key) {
            return null;
        }

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
     * Callback interface when a {@link ResourceLoader} has finished loading its data.
     */
    public static interface OnLoadCompleteListener<Key, Result> {
        /**
         * Called on the UI thread when the load is complete.
         * @param key The key, passed earlier by {@link ResourceLoader#load}.
         * @param cookie An object, passed earlier by {@link ResourceLoader#load}.
         * @param result A result or <tt>null</tt> of the load.
         */
        void onLoadComplete(Key key, Object cookie, Result result);
    }
}
