package android.ext.content;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.concurrent.Executor;
import android.content.Context;
import android.ext.content.AsyncCacheLoader.LoadParams;
import android.ext.content.AsyncCacheLoader.LoadResult;
import android.ext.net.DownloadRequest;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.JsonUtils;
import android.util.Log;

/**
 * Class <tt>AsyncCacheLoader</tt> allows to load the data on a background thread
 * and publish results on the UI thread. This class can be support the cache file.
 * <h3>AsyncCacheLoader's generic types</h3>
 * <p>The two types used by a loader are the following:</p>
 * <ol><li><tt>Key</tt>, The loader's key type.</li>
 * <li><tt>Result</tt>, The load result type.</li></ol>
 * <h3>Usage</h3>
 * <p>Here is an example of subclassing:</p><pre>
 * public static class JsonLoadParams extends LoadParams&lt;String, JSONObject&gt; {
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
 *     public JSONObject parseResult(String url, File cacheFile, Cancelable cancelable) throws Exception {
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
 * private static class JsonLoader extends AsyncCacheLoader&lt;String, JSONObject&gt; {
 *     public JsonLoader(Executor executor, Activity ownerActivity) {
 *         super(executor, ownerActivity);
 *     }
 *
 *     {@code @Override}
 *     protected void onStartLoading(String url, LoadParams&lt;String, JSONObject&gt;[] params) {
 *         final File cacheFile = params[0].getCacheFile(url);
 *         if (cacheFile == null || !cacheFile.exists()) {
 *             // If the cache file not exists, show loading UI.
 *         }
 *     }
 *
 *     {@code @Override}
 *     protected void onLoadComplete(String url, LoadParams&lt;String, JSONObject&gt;[] params, LoadResult&lt;JSONObject&gt result) {
 *         final Activity activity = getOwnerActivity();
 *         if (activity == null) {
 *             // The owner activity has been destroyed or release by the GC.
 *             return;
 *         }
 *
 *         // Hide loading UI, if need.
 *         if (result.result != null) {
 *             // Loading succeeded (may be file cache hit or download succeeded), update UI.
 *         } else {
 *             if (result.hitCache) {
 *                 // Loading succeeded, but the cache file's contents are equal the loaded file's contents, do nothing.
 *             } else {
 *                 // Loading failed and file cache not hit, show error or empty UI.
 *             }
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
public abstract class AsyncCacheLoader<Key, Result> extends AsyncTaskLoader<Key, LoadParams<Key, Result>, LoadResult<Result>> {
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

    /**
     * Called on a background thread to download the data.
     * @param task The {@link Task} whose executing this method.
     * @param key The key, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @param cacheFile The cache file to store the download data, or <tt>null</tt> if no cache file.
     * @return If the <em>cacheFile</em> is <tt>null</tt> returns the HTTP response code (<tt>Integer</tt>),
     * Otherwise returns the result, defined by the subclass.
     * @throws Exception if an error occurs while downloading to the resource.
     */
    protected Object onDownload(Task<?, ?, ?> task, Key key, LoadParams<Key, Result> params, String cacheFile) throws Exception {
        final DownloadRequest request = params.newDownloadRequest(key);
        return (cacheFile != null ? request.download(cacheFile, task, null) : request.download(task));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onProgressUpdate(Key key, LoadParams<Key, Result>[] params, Object[] values) {
        onLoadComplete(key, params, new LoadResult<Result>((Result)values[0], false));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected LoadResult<Result> loadInBackground(Task<?, ?, ?> task, Key key, LoadParams<Key, Result>[] loadParams) {
        boolean hitCache = false;
        Result result = null;
        try {
            final LoadParams<Key, Result> params = loadParams[0];
            final File cacheFile = params.getCacheFile(key);
            if (cacheFile == null) {
                result = (Result)onDownload(task, key, params, null);
            } else {
                hitCache = loadFromCache(task, key, params, cacheFile);
                if (!isTaskCancelled(task)) {
                    result = download(task, key, params, cacheFile.getPath(), hitCache);
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load JSON data - key = " + key + "\n" + e);
        }

        return new LoadResult<Result>(result, hitCache);
    }

    private boolean loadFromCache(Task<?, ?, ?> task, Key key, LoadParams<Key, Result> params, File cacheFile) {
        try {
            DebugUtils.__checkStartMethodTracing();
            final Result result = params.parseResult(key, cacheFile, task);
            DebugUtils.__checkStopMethodTracing(getClass().getSimpleName(), "loadFromCache");
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

    private Result download(Task<?, ?, ?> task, Key key, LoadParams<Key, Result> params, String cacheFile, boolean hitCache) throws Exception {
        final String tempFile = cacheFile + "." + Thread.currentThread().hashCode();
        final int statusCode  = (int)onDownload(task, key, params, tempFile);
        if (statusCode == HttpURLConnection.HTTP_OK && !isTaskCancelled(task)) {
            // If the cache file is hit and the cache file's contents are equal the temp
            // file's contents. Deletes the temp file and returns null, do not update UI.
            if (hitCache && FileUtils.compareFile(cacheFile, tempFile)) {
                FileUtils.deleteFiles(tempFile, false);
                return null;
            }

            // Parse the temp file and save it to the cache file.
            DebugUtils.__checkStartMethodTracing();
            final Result result = params.parseResult(key, new File(tempFile), task);
            DebugUtils.__checkStopMethodTracing(getClass().getSimpleName(), "download - parseResult");
            if (!isTaskCancelled(task)) {
                // Saves the cache file.
                FileUtils.moveFile(tempFile, cacheFile);
                return result;
            }
        }

        return null;
    }

    /**
     * Class <tt>LoadResult</tt> used to store the load result.
     */
    public static final class LoadResult<Result> {
        /**
         * The result of the load. If the result is <tt>null</tt> indicates the load failed
         * or the cache file is hit and it's contents are equal the loaded file's contents.
         */
        public final Result result;

        /**
         * If <tt>true</tt> indicates the cache file has successfully loaded, <tt>false</tt> otherwise.
         */
        public final boolean hitCache;

        /**
         * Constructor
         */
        public LoadResult(Result result, boolean hitCache) {
            this.result = result;
            this.hitCache = hitCache;
        }
    }

    /**
     * Class <tt>LoadParams</tt> used to {@link AsyncCacheLoader} to load data.
     */
    public static abstract class LoadParams<Key, Result> {
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
         * should override this method to parse their results. <p>The default implementation
         * parse the cache file to a JSON data (<tt>JSONObject</tt> or <tt>JSONArray</tt>).</p>
         * @param key The key, passed earlier by {@link #load}.
         * @param cacheFile The cache file to parse.
         * @param cancelable A {@link Cancelable} can be check the parse was cancelled.
         * @return A result, defined by the subclass.
         * @throws Exception if the data can not be parse.
         * @see JsonUtils#parse(Context, Object, Cancelable)
         */
        public Result parseResult(Key key, File cacheFile, Cancelable cancelable) throws Exception {
            return JsonUtils.parse(null, cacheFile, cancelable);
        }
    }
}
