package android.ext.content;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.concurrent.Executor;
import android.ext.content.AsyncJsonLoader.LoadParams;
import android.ext.content.AsyncJsonLoader.LoadResult;
import android.ext.net.DownloadRequest;
import android.ext.util.FileUtils;
import android.ext.util.JsonUtils;
import android.util.Log;

/**
 * Class <tt>AsyncJsonLoader</tt> allows to load the JSON data on a background thread and publish
 * results on the UI thread. This class can be support the JSON cache file.
 * <h5>AsyncJsonLoader's generic types</h5>
 * <p>The two types used by a JSON loader are the following:</p>
 * <ol><li><tt>Key</tt>, The loader's key type.</li>
 * <li><tt>Result</tt>, The load result type, must be <tt>JSONObject</tt> or <tt>JSONArray</tt>.</li></ol>
 * <h2>Usage</h2>
 * <p>Here is an example of subclassing:</p><pre>
 * public static class JsonLoadParams extends LoadParams&lt;String, JSONObject&gt; {
 *     {@code @Override}
 *     public File getCacheFile(String url) {
 *         // Builds the cache file, For example:
 *         return new File(context.getFilesDir(), "xxx/cacheFile.json");
 *     }
 *
 *     {@code @Override}
 *     public boolean validateResult(String url, JSONObject result) {
 *         return (result != null && result.optInt("retCode") == 200);
 *     }
 *
 *     {@code @Override}
 *     public DownloadRequest newDownloadRequest(String url) throws Exception {
 *         return new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
 *     }
 * }
 *
 * private static class JsonLoader extends AsyncJsonLoader&lt;String, JSONObject&gt; {
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
 *         } else if (!result.hitCache) {
 *             // Loading failed and file cache not hit, show error or empty UI.
 *         }
 *     }
 * }
 *
 * final JsonLoader mLoader = new JsonLoader(executor, activity);
 * mLoader.load(url, new JsonLoadParams());</pre>
 * @author Garfield
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AsyncJsonLoader<Key, Result> extends AsyncTaskLoader<Key, LoadParams<Key, Result>, LoadResult<Result>> {
    /**
     * Constructor
     * @param executor The <tt>Executor</tt> to executing load task.
     * @see #AsyncJsonLoader(Executor, Object)
     */
    public AsyncJsonLoader(Executor executor) {
        super(executor);
    }

    /**
     * Constructor
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncJsonLoader(Executor)
     */
    public AsyncJsonLoader(Executor executor, Object owner) {
        super(executor, owner);
    }

    /**
     * Called on a background thread to download the JSON data.
     * @param task The {@link Task} whose executing this method.
     * @param key The key, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @param cacheFile The JSON cache file to store the JSON data, or <tt>null</tt> if no cache file.
     * @return If the <em>cacheFile</em> is <tt>null</tt> returns the HTTP response code (<tt>Integer</tt>),
     * Otherwise returns the JSON data (<tt>JSONObject</tt> or <tt>JSONArray</tt>).
     * @throws Exception if an error occurs while downloading to the resource.
     */
    protected Object onDownload(Task<?, ?> task, Key key, LoadParams<Key, Result> params, String cacheFile) throws Exception {
        final DownloadRequest request = params.newDownloadRequest(key);
        return (cacheFile != null ? request.download(cacheFile, task, null) : request.download(task));
    }

    @Override
    protected void onProgressUpdate(Key key, LoadParams<Key, Result>[] params, Object[] values) {
        onLoadComplete(key, params, new LoadResult<Result>((Result)values[0], false));
    }

    @Override
    protected LoadResult<Result> loadInBackground(Task<?, ?> task, Key key, LoadParams<Key, Result>[] loadParams) {
        boolean hitCache = false;
        Result result = null;
        try {
            final LoadParams params = loadParams[0];
            final File cacheFile = params.getCacheFile(key);
            if (cacheFile == null) {
                final Result value = (Result)onDownload(task, key, params, null);
                if (params.validateResult(key, value)) {
                    result = value;
                }
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

    private boolean loadFromCache(Task task, Key key, LoadParams params, File cacheFile) {
        boolean hitCache = false;
        try {
            final Result result = JsonUtils.parse(null, cacheFile, task);
            if (hitCache = params.validateResult(key, result)) {
                // If the task was cancelled then invoking setProgress has no effect.
                task.setProgress(result);
            }
        } catch (Exception e) {
            Log.w(getClass().getName(), "Couldn't load JSON data from the cache - " + cacheFile);
        }

        return hitCache;
    }

    private Result download(Task task, Key key, LoadParams params, String cacheFile, boolean hitCache) throws Exception {
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
            final Result result = JsonUtils.parse(null, tempFile, task);
            if (!isTaskCancelled(task) && params.validateResult(key, result)) {
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
     * Class <tt>LoadParams</tt> used to {@link AsyncJsonLoader} to load JSON data.
     */
    public static abstract class LoadParams<Key, Result> {
        /**
         * Returns the absolute path of the JSON cache file on the filesystem.
         * <p>Subclasses should override this method to returns the cache file
         * path. The default implementation returns <tt>null</tt>.</p>
         * @param key The key, passed earlier by {@link #load}.
         * @return The path of the JSON cache file, or <tt>null</tt> if no cache file.
         */
        public File getCacheFile(Key key) {
            return null;
        }

        /**
         * Tests if the <em>result</em> is valid. Subclasses should override this method
         * to validate the <em>result</em>.
         * @param key The key, passed earlier by {@link #load}.
         * @param result The result or <tt>null</tt>.
         * @return <tt>true</tt> if the <em>result</em> is valid, <tt>false</tt> otherwise.
         */
        public boolean validateResult(Key key, Result result) {
            return (result != null);
        }

        /**
         * Returns a new download request with the specified <em>key</em>.
         * @param key The key, passed earlier by {@link #load}.
         * @return The {@link DownloadRequest} object.
         * @throws Exception if an error occurs while opening the connection.
         */
        public abstract DownloadRequest newDownloadRequest(Key key) throws Exception;
    }
}
