package android.ext.content;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.concurrent.Executor;
import android.ext.content.AsyncJsonLoader.LoadParams;
import android.ext.net.DownloadRequest;
import android.ext.util.FileUtils;
import android.ext.util.JsonUtils;
import android.util.Log;
import android.util.Pair;

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
 *         return new File("mnt/sdcard/xxx/cacheFile.json");
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
 *     protected void onLoadComplete(String url, LoadParams&lt;String, JSONObject&gt;[] params, Pair&lt;JSONObject, Boolean&gt result) {
 *         final Activity activity = getOwnerActivity();
 *         if (activity == null) {
 *             // The owner activity has been destroyed or release by the GC.
 *             return;
 *         }
 *
 *         // Hide loading UI, if need.
 *         if (result.first != null) {
 *             // Loading succeeded, update UI.
 *         } else if (!result.second) {
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
public abstract class AsyncJsonLoader<Key, Result> extends AsyncTaskLoader<Key, LoadParams<Key, Result>, Pair<Result, Boolean>> {
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

    @Override
    protected void onProgressUpdate(Key key, LoadParams<Key, Result>[] params, Object[] values) {
        onLoadComplete(key, params, new Pair<Result, Boolean>((Result)values[0], false));
    }

    @Override
    protected Pair<Result, Boolean> loadInBackground(Task<?, ?> task, Key key, LoadParams<Key, Result>[] loadParams) {
        boolean hitCache = false;
        Result result = null;
        try {
            final LoadParams params = loadParams[0];
            final File cacheFile = params.getCacheFile(key);
            if (cacheFile == null) {
                result = params.newDownloadRequest(key).download(task);
                if (isTaskCancelled(task) || !params.validateResult(key, result)) {
                    result = null;
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

        return new Pair<Result, Boolean>(result, hitCache);
    }

    private boolean loadFromCache(Task task, Key key, LoadParams params, File cacheFile) {
        boolean hitCache = false;
        try {
            final Result result = JsonUtils.parse(null, cacheFile, task);
            if (hitCache = params.validateResult(key, result)) {
                task.setProgress(result);
            }
        } catch (Exception e) {
            Log.w(getClass().getName(), "Couldn't load JSON data from the cache - " + cacheFile);
        }

        return hitCache;
    }

    private Result download(Task task, Key key, LoadParams params, String cacheFile, boolean hitCache) throws Exception {
        final String tempFile = cacheFile + "." + Thread.currentThread().hashCode();
        final int statusCode  = params.newDownloadRequest(key).download(tempFile, task, null);
        if (statusCode == HttpURLConnection.HTTP_OK && !isTaskCancelled(task)) {
            // If cache file is hit and the cache file's contents are equal the temp
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
     * Class <tt>LoadParams</tt> used to {@link AsyncJsonLoader} to load JSON data.
     */
    public static abstract class LoadParams<Key, Result> {
        /**
         * Returns the absolute path of the JSON cache file on the filesystem.
         * <p>Subclasses should override this method to returns the cache file
         * path. The default implementation returns <tt>null</tt>.</p>
         * @param key The key, passed earlier by {@link AsyncJsonLoader#load}.
         * @return The path of the JSON cache file, or <tt>null</tt> if no cache file.
         */
        public File getCacheFile(Key key) {
            return null;
        }

        /**
         * Tests if the <em>result</em> is valid. Subclasses should override this method
         * to validate the <em>result</em>.
         * @param key The key, passed earlier by {@link AsyncJsonLoader#load}.
         * @param result The result or <tt>null</tt>.
         * @return <tt>true</tt> if the <em>result</em> is valid, <tt>false</tt> otherwise.
         */
        public boolean validateResult(Key key, Result result) {
            return (result != null);
        }

        /**
         * Returns a new download request with the specified <em>key</em>.
         * @param key The key, passed earlier by {@link AsyncJsonLoader#load}.
         * @return The {@link DownloadRequest} object.
         * @throws Exception if an error occurs while opening the connection.
         */
        public abstract DownloadRequest newDownloadRequest(Key key) throws Exception;
    }
}
