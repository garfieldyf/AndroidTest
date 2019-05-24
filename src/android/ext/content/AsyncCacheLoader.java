package android.ext.content;

import static java.net.HttpURLConnection.HTTP_OK;
import java.io.File;
import java.util.concurrent.Executor;
import android.content.Context;
import android.ext.content.AsyncCacheLoader.LoadParams;
import android.ext.net.DownloadRequest;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.JsonUtils;
import android.util.Log;

/**
 * Class <tt>AsyncCacheLoader</tt> allows to load the data on a background thread
 * and publish results on the UI thread. This class can be support the cache file.
 * <h3>Usage</h3>
 * <p>Here is an example of subclassing:</p><pre>
 * public static class JsonLoadParams extends LoadParams&lt;String&gt; {
 *     {@code @Override}
 *     public File getCacheFile(String url) {
 *         // Builds the cache file, For example:
 *         return new File(context.getFilesDir(), "xxx/cacheFile.json");
 *     }
 *
 *     {@code @Override}
 *     public DownloadRequest newDownloadRequest(String url) throws Exception {
 *         return new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
 *     }
 *
 *     {@code @Override}
 *     public Object parseResult(String url, File cacheFile, Cancelable cancelable) throws Exception {
 *         if (!cacheFile.exists()) {
 *             // If the cache file not exists, return null or parse the JSON data from the "assets" file.
 *             return null;
 *         }
 *
 *         // Parse the JSON data ... ...
 *         final JSONObject result = JsonUtils.parse(context, cacheFile, cancelable);
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
 * private static class JsonLoader extends AsyncCacheLoader&lt;String&gt; {
 *     public JsonLoader(Executor executor, Activity ownerActivity) {
 *         super(executor, ownerActivity);
 *     }
 *
 *     // Optional override.
 *     {@code @Override}
 *     protected void onStartLoading(String url, LoadParams&lt;String&gt;[] params) {
 *         final File cacheFile = params[0].getCacheFile(url);
 *         if (cacheFile == null || !cacheFile.exists()) {
 *             // If the cache file not exists, show loading UI.
 *         }
 *     }
 *
 *     {@code @Override}
 *     protected void onLoadComplete(String url, LoadParams&lt;String&gt;[] params, Object result) {
 *         final Activity activity = getOwnerActivity();
 *         if (activity == null) {
 *             // The owner activity has been destroyed or release by the GC.
 *             return;
 *         }
 *
 *         if (result == EMPTY_RESULT) {
 *             // The cache file's contents are equal the downloaded file's contents, do not update UI.
 *             return;
 *         }
 *
 *         // Hide loading UI, if need.
 *         if (result != null) {
 *             // Loading succeeded, update UI.
 *         } else {
 *             // Loading failed, show error or empty UI.
 *         }
 *     }
 * }
 *
 * private JsonLoader mLoader;
 *
 * mLoader = new JsonLoader(executor, activity);
 * mLoader.load(url, new JsonLoadParams());</pre>
 * @author Garfield
 */
public abstract class AsyncCacheLoader<Key> extends AsyncTaskLoader<Key, LoadParams<Key>, Object> {
    public static final Object EMPTY_RESULT = new Object();

    /**
     * Constructor
     * @param executor The <tt>Executor</tt> to executing load task.
     * @see #AsyncCacheLoader(Executor, Object)
     */
    public AsyncCacheLoader(Executor executor) {
        super(executor);
    }

    /**
     * Constructor
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncCacheLoader(Executor)
     */
    public AsyncCacheLoader(Executor executor, Object owner) {
        super(executor, owner);
    }

    @Override
    protected void onProgressUpdate(Key key, LoadParams<Key>[] params, Object[] values) {
        onLoadComplete(key, params, values[0]);
    }

    @Override
    protected Object loadInBackground(Task<?, ?> task, Key key, LoadParams<Key>[] loadParams) {
        Object result = null;
        try {
            final LoadParams<Key> params = loadParams[0];
            final File cacheFile = params.getCacheFile(key);
            if (cacheFile == null) {
                result = download(task, key, params);
            } else {
                final boolean hitCache = loadFromCache(task, key, params, cacheFile);
                if (!isTaskCancelled(task)) {
                    result = download(task, key, params, cacheFile.getPath(), hitCache);
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load JSON data - key = " + key + "\n" + e);
        }

        return result;
    }

    private Object download(Task<?, ?> task, Key key, LoadParams<Key> params) throws Exception {
        final File tempFile = File.createTempFile("._acl-" + Thread.currentThread().hashCode(), null, null);
        DebugUtils.__checkDebug(true, getClass().getSimpleName(), tempFile.getPath());
        try {
            final int statusCode = params.newDownloadRequest(key).download(tempFile.getPath(), task, null);
            final Object result = (statusCode == HTTP_OK && !isTaskCancelled(task) ? params.parseResult(key, tempFile, task) : null);
            DebugUtils.__checkError(result == EMPTY_RESULT, "Invalid parse - result == EMPTY_RESULT");
            return result;
        } finally {
            tempFile.delete();
        }
    }

    private boolean loadFromCache(Task<?, ?> task, Key key, LoadParams<Key> params, File cacheFile) {
        try {
            DebugUtils.__checkStartMethodTracing();
            final Object result = params.parseResult(key, cacheFile, task);
            DebugUtils.__checkStopMethodTracing(getClass().getSimpleName(), "loadFromCache");
            DebugUtils.__checkError(result == EMPTY_RESULT, "Invalid parse - result == EMPTY_RESULT");
            if (result != null) {
                // If the task was cancelled then invoking setProgress has no effect.
                task.setProgress(result);
                return true;
            }
        } catch (Exception e) {
            Log.w(getClass().getName(), "Couldn't load JSON data from the cache - " + cacheFile.getPath());
        }

        return false;
    }

    private Object download(Task<?, ?> task, Key key, LoadParams<Key> params, String cacheFile, boolean hitCache) throws Exception {
        final String tempFile = cacheFile + "." + Thread.currentThread().hashCode();
        final int statusCode  = params.newDownloadRequest(key).download(tempFile, task, null);
        if (statusCode == HTTP_OK && !isTaskCancelled(task)) {
            // If the cache file is hit and the cache file's contents are equal the temp file's
            // contents. Deletes the temp file and returns EMPTY_RESULT, do not update UI.
            if (hitCache && FileUtils.compareFile(cacheFile, tempFile)) {
                FileUtils.deleteFiles(tempFile, false);
                return EMPTY_RESULT;
            }

            // Parse the temp file and save it to the cache file.
            DebugUtils.__checkStartMethodTracing();
            final Object result = params.parseResult(key, new File(tempFile), task);
            DebugUtils.__checkStopMethodTracing(getClass().getSimpleName(), "parseResult");
            DebugUtils.__checkError(result == EMPTY_RESULT, "Invalid parse - result == EMPTY_RESULT");
            if (result != null && !isTaskCancelled(task)) {
                FileUtils.moveFile(tempFile, cacheFile);
                return result;
            }
        }

        return null;
    }

    /**
     * Class <tt>LoadParams</tt> used to {@link AsyncCacheLoader} to load data.
     */
    public static abstract class LoadParams<Key> {
        /**
         * Returns the absolute path of the cache file on the filesystem.
         * <p>Subclasses should override this method to returns the cache file
         * path. The default implementation returns <tt>null</tt>.</p>
         * @param key The key, passed earlier by {@link #load}.
         * @return The path of the cache file, or <tt>null</tt> if no cache file.
         */
        public File getCacheFile(Key key) {
            return null;
        }

        /**
         * Returns a new download request with the specified <em>key</em>.
         * @param key The key, passed earlier by {@link #load}.
         * @return The {@link DownloadRequest} object.
         * @throws Exception if an error occurs while opening the connection.
         */
        public abstract DownloadRequest newDownloadRequest(Key key) throws Exception;

        /**
         * Called on a background thread to parse the data from the cache file. Subclasses
         * should override this method to parse their result. <p>The default implementation
         * parse the cache file's contents to a <tt>JSONObject</tt> or <tt>JSONArray</tt>.</p>
         * @param key The key, passed earlier by {@link #load}.
         * @param cacheFile The cache file to parse.
         * @param cancelable A {@link Cancelable} can be check the parse was cancelled.
         * @return A result or <tt>null</tt>, defined by the subclass.
         * @throws Exception if the data can not be parse.
         * @see JsonUtils#parse(Context, Object, Cancelable)
         */
        public Object parseResult(Key key, File cacheFile, Cancelable cancelable) throws Exception {
            return JsonUtils.parse(null, cacheFile, cancelable);
        }
    }
}
