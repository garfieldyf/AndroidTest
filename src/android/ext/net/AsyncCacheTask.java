package android.ext.net;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Arrays;
import android.content.Context;
import android.ext.content.AbsAsyncTask;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.JsonUtils;
import android.util.Log;
import android.util.Pair;

/**
 * Class <tt>AsyncCacheTask</tt> allows to load the data on a background thread
 * and publish results on the UI thread. This class can be support the cache file.
 * <h3>AsyncCacheTask's generic types</h3>
 * <p>The two types used by a task are the following:</p>
 * <ol><li><tt>Params</tt>, The parameters type of the task.</li>
 * <li><tt>Result</tt>, The load result type.</li></ol>
 * <h3>Usage</h3>
 * <p>Here is an example of subclassing:</p><pre>
 * private static class JsonTask extends AsyncCacheTask&lt;String, JSONObject&gt; {
 *     public JsonTask(Activity ownerActivity) {
 *         super(ownerActivity);
 *     }
 *
 *     {@code @Override}
 *     protected File getCacheFile(String[] urls) {
 *         // Builds the cache file, For example:
 *         return new File(context.getFilesDir(), "xxx/cacheFile.json");
 *     }
 *
 *     {@code @Override}
 *     protected DownloadRequest newDownloadRequest(String[] urls) throws Exception {
 *         return new DownloadRequest(urls[0]).connectTimeout(30000).readTimeout(30000);
 *     }
 *
 *     {@code @Override}
 *     protected JSONObject parseResult(String[] urls, File cacheFile) throws Exception {
 *         if (!cacheFile.exists()) {
 *             // If the cache file not exists, return null or parse the JSON data from the "assets" file.
 *             return null;
 *         }
 *
 *         // Parse the JSON data ... ...
 *         final JSONObject result = JsonUtils.parse(context, cacheFile, this);
 *
 *         // Check the result is valid.
 *         if (result != null && result.optInt("retCode") == 200) {
 *             return result;
 *         }
 *
 *         return null;
 *     }
 *
 *     {@code @Override}
 *     protected void onExecuteComplete(Result result) {
 *         final Activity activity = getOwnerActivity();
 *         if (activity == null) {
 *             // The owner activity has been destroyed or release by the GC.
 *             return;
 *         }
 *
 *         if (result != null) {
 *             // Loading succeeded, update UI.
 *         } else {
 *             // Loading failed, show error or empty UI.
 *         }
 *     }
 * }
 *
 * new JsonTask(activity).execute(url);</pre>
 * @author Garfield
 */
public abstract class AsyncCacheTask<Params, Result> extends AbsAsyncTask<Params, Object, Pair<Result, Boolean>> {
    /**
     * Constructor
     * @see #AsyncCacheTask(Object)
     */
    public AsyncCacheTask() {
    }

    /**
     * Constructor
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncCacheTask()
     */
    public AsyncCacheTask(Object owner) {
        super(owner);
    }

    /**
     * Returns the absolute path of the cache file on the filesystem.
     * Subclasses should override this method to returns the cache file
     * path. <p>The default implementation returns <tt>null</tt>.</p>
     * @param params The parameters, passed earlier by {@link #execute(Params[])}.
     * @return The path of the cache file, or <tt>null</tt> if no cache file.
     */
    protected File getCacheFile(Params[] params) {
        return null;
    }

    /**
     * Called on the UI thread after {@link #doInBackground}. <p>This method
     * won't be invoked if this task was cancelled.</p>
     * @param result The result, returned earlier by {@link #doInBackground}.
     */
    protected void onExecuteComplete(Result result) {
        throw new RuntimeException("No Implementation, Subclass must be implementation!");
    }

    /**
     * Called on a background thread to parse the data from the cache file. Subclasses
     * should override this method to parse their results. <p>The default implementation
     * parse the cache file to a JSON data (<tt>JSONObject</tt> or <tt>JSONArray</tt>).</p>
     * @param params The parameters, passed earlier by {@link #execute(Params[])}.
     * @param cacheFile The cache file to parse.
     * @return A result or <tt>null</tt>, defined by the subclass of this task.
     * @throws Exception if the data can not be parse.
     * @see JsonUtils#parse(Context, Object, Cancelable)
     */
    protected Result parseResult(Params[] params, File cacheFile) throws Exception {
        return JsonUtils.parse(null, cacheFile, this);
    }

    /**
     * Returns a new download request with the specified <em>params</em>.
     * @param params The parameters, passed earlier by {@link #execute(Params[])}.
     * @return The instance of {@link DownloadRequest}.
     * @throws Exception if an error occurs while opening the connection.
     */
    protected abstract DownloadRequest newDownloadRequest(Params[] params) throws Exception;

    @Override
    @SuppressWarnings("unchecked")
    protected void onProgressUpdate(Object... values) {
        onExecuteComplete((Result)values[0]);
    }

    @Override
    protected void onPostExecute(Pair<Result, Boolean> result) {
        if (result.first != null) {
            // Loading succeeded.
            onExecuteComplete(result.first);
        } else if (!result.second) {
            // Loading failed and the file cache not hit.
            onExecuteComplete(null);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Pair<Result, Boolean> doInBackground(Params... params) {
        boolean hitCache = false;
        Result result = null;
        try {
            final File cacheFile = getCacheFile(params);
            if (cacheFile == null) {
                result = newDownloadRequest(params).download(this);
            } else {
                hitCache = loadFromCache(params, cacheFile);
                if (!isCancelled()) {
                    result = download(params, cacheFile.getPath(), hitCache);
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load JSON data - params = " + Arrays.toString(params) + "\n" + e);
        }

        return new Pair<Result, Boolean>(result, hitCache);
    }

    private boolean loadFromCache(Params[] params, File cacheFile) {
        try {
            DebugUtils.__checkStartMethodTracing();
            final Result result = parseResult(params, cacheFile);
            DebugUtils.__checkStopMethodTracing(getClass().getSimpleName(), "loadFromCache");
            if (result != null) {
                // If this task was cancelled then invoking publishProgress has no effect.
                publishProgress(result);
                return true;
            }
        } catch (Exception e) {
            Log.w(getClass().getName(), "Couldn't load JSON data from the cache - " + cacheFile.getPath());
        }

        return false;
    }

    private Result download(Params[] params, String cacheFile, boolean hitCache) throws Exception {
        final String tempFile = cacheFile + "." + Thread.currentThread().hashCode();
        final int statusCode  = newDownloadRequest(params).download(tempFile, this, null);
        if (statusCode == HttpURLConnection.HTTP_OK && !isCancelled()) {
            // If the cache file is hit and the cache file's contents are equal the temp
            // file's contents. Deletes the temp file and returns null, do not update UI.
            if (hitCache && FileUtils.compareFile(cacheFile, tempFile)) {
                FileUtils.deleteFiles(tempFile, false);
                return null;
            }

            // Parse the temp file and save it to the cache file.
            DebugUtils.__checkStartMethodTracing();
            final Result result = parseResult(params, new File(tempFile));
            DebugUtils.__checkStopMethodTracing(getClass().getSimpleName(), "parseResult");
            if (result != null && !isCancelled()) {
                FileUtils.moveFile(tempFile, cacheFile);
                return result;
            }
        }

        return null;
    }
}
