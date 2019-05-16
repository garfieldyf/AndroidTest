package android.ext.net;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Arrays;
import android.content.Context;
import android.ext.content.AbsAsyncTask;
import android.ext.content.AsyncJsonLoader.LoadResult;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.JsonUtils;
import android.util.Log;

/**
 * Class <tt>AsyncJsonTask</tt> allows to load the JSON data on a background thread and publish
 * results on the UI thread. This class can be support the JSON cache file.
 * <h3>AsyncJsonTask's generic types</h3>
 * <p>The two types used by a JSON task are the following:</p>
 * <ol><li><tt>Params</tt>, The parameters type of the task.</li>
 * <li><tt>Result</tt>, The load result type, must be <tt>JSONObject</tt> or <tt>JSONArray</tt>.</li></ol>
 * <h3>Usage</h3>
 * <p>Here is an example of subclassing:</p><pre>
 * private static class JsonTask extends AsyncJsonTask&lt;String, JSONObject&gt; {
 *     public JsonTask(Activity ownerActivity) {
 *         super(ownerActivity);
 *     }
 *
 *     {@code @Override}
 *     protected File getCacheFile(String[] params) {
 *         // Builds the cache file, For example:
 *         return new File(context.getFilesDir(), "xxx/cacheFile.json");
 *     }
 *
 *     {@code @Override}
 *     protected boolean validateResult(String[] params, JSONObject result) {
 *         return (result != null && result.optInt("retCode") == 200);
 *     }
 *
 *     {@code @Override}
 *     protected DownloadRequest newDownloadRequest(String[] params) throws Exception {
 *         return new DownloadRequest(params[0]).connectTimeout(30000).readTimeout(30000);
 *     }
 *
 *     {@code @Override}
 *     protected void onPostExecute(LoadResult&lt;JSONObject&gt result) {
 *         final Activity activity = getOwnerActivity();
 *         if (activity == null) {
 *             // The owner activity has been destroyed or release by the GC.
 *             return;
 *         }
 *
 *         if (result.result != null) {
 *             // Loading succeeded (may be file cache hit or download succeeded), update UI.
 *         } else if (!result.hitCache) {
 *             // Loading failed and file cache not hit, show error or empty UI.
 *         }
 *     }
 * }
 *
 * new JsonTask(activity).execute(url);</pre>
 * @author Garfield
 */
public abstract class AsyncJsonTask<Params, Result> extends AbsAsyncTask<Params, Object, LoadResult<Result>> {
    /**
     * Constructor
     * @see #AsyncJsonTask(Object)
     */
    public AsyncJsonTask() {
    }

    /**
     * Constructor
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncJsonTask()
     */
    public AsyncJsonTask(Object owner) {
        super(owner);
    }

    /**
     * Returns the absolute path of the JSON cache file on the filesystem.
     * <p>Subclasses should override this method to returns the cache file path.
     * The default implementation returns <tt>null</tt>.</p>
     * @param params The parameters, passed earlier by {@link #execute(Params[])}.
     * @return The path of the JSON cache file, or <tt>null</tt> if no cache file.
     */
    protected File getCacheFile(Params[] params) {
        return null;
    }

    /**
     * Tests if the <em>result</em> is valid. Subclasses should override this method to
     * validate the <em>result</em>.
     * @param params The parameters, passed earlier by {@link #execute(Params[])}.
     * @param result The JSON value or <tt>null</tt>.
     * @return <tt>true</tt> if the <em>result</em> is valid, <tt>false</tt> otherwise.
     */
    protected boolean validateResult(Params[] params, Result result) {
        return (result != null);
    }

    /**
     * Called on a background thread to load the JSON data from the cache file.
     * @param params The parameters of this task, passed earlier by {@link #execute(Params[])}.
     * @param cacheFile The JSON cache file to load.
     * @return A result, defined by the subclass of this task.
     * @throws Exception if JSON data can not be load.
     * @see JsonUtils#parse(Context, Object, Cancelable)
     */
    protected Result loadFromCache(Params[] params, File cacheFile) throws Exception {
        return JsonUtils.parse(null, cacheFile, this);
    }

    /**
     * Called on a background thread to download the JSON data.
     * @param params The parameters of this task, passed earlier by {@link #execute(Params[])}.
     * @param cacheFile The JSON cache file to store the JSON data, or <tt>null</tt> if no cache file.
     * @return If the <em>cacheFile</em> is <tt>null</tt> returns the HTTP response code (<tt>Integer</tt>),
     * Otherwise returns the JSON data (<tt>JSONObject</tt> or <tt>JSONArray</tt>).
     * @throws Exception if an error occurs while downloading to the resource.
     */
    protected Object onDownload(Params[] params, String cacheFile) throws Exception {
        final DownloadRequest request = newDownloadRequest(params);
        return (cacheFile != null ? request.download(cacheFile, this, null) : request.download(this));
    }

    /**
     * Returns a new download request with the specified <em>params</em>.
     * @param params The parameters of this task, passed earlier by {@link #execute(Params[])}.
     * @return The instance of {@link DownloadRequest}.
     * @throws Exception if an error occurs while opening the connection.
     */
    protected abstract DownloadRequest newDownloadRequest(Params[] params) throws Exception;

    @Override
    @SuppressWarnings("unchecked")
    protected void onProgressUpdate(Object... values) {
        onPostExecute(new LoadResult<Result>((Result)values[0], false));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected LoadResult<Result> doInBackground(Params... params) {
        boolean hitCache = false;
        Result result = null;
        try {
            final File cacheFile = getCacheFile(params);
            if (cacheFile == null) {
                final Result value = (Result)onDownload(params, null);
                if (validateResult(params, value)) {
                    result = value;
                }
            } else {
                hitCache = loadFromCacheFile(params, cacheFile);
                if (!isCancelled()) {
                    result = download(params, cacheFile.getPath(), hitCache);
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load JSON data - params = " + Arrays.toString(params) + "\n" + e);
        }

        return new LoadResult<Result>(result, hitCache);
    }

    private boolean loadFromCacheFile(Params[] params, File cacheFile) {
        boolean hitCache = false;
        try {
            DebugUtils.__checkStartMethodTracing();
            final Result result = loadFromCache(params, cacheFile);
            DebugUtils.__checkStopMethodTracing(getClass().getSimpleName(), "loadFromCache");
            if (hitCache = validateResult(params, result)) {
                // If this task was cancelled then invoking publishProgress has no effect.
                publishProgress(result);
            }
        } catch (Exception e) {
            Log.w(getClass().getName(), "Couldn't load JSON data from the cache - " + cacheFile);
        }

        return hitCache;
    }

    private Result download(Params[] params, String cacheFile, boolean hitCache) throws Exception {
        final String tempFile = cacheFile + "." + Thread.currentThread().hashCode();
        final int statusCode  = (int)onDownload(params, tempFile);
        if (statusCode == HttpURLConnection.HTTP_OK && !isCancelled()) {
            // If the cache file is hit and the cache file's contents are equal the temp
            // file's contents. Deletes the temp file and returns null, do not update UI.
            if (hitCache && FileUtils.compareFile(cacheFile, tempFile)) {
                FileUtils.deleteFiles(tempFile, false);
                return null;
            }

            // Parse the temp file and save it to the cache file.
            DebugUtils.__checkStartMethodTracing();
            final Result result = JsonUtils.parse(null, tempFile, this);
            DebugUtils.__checkStopMethodTracing(getClass().getSimpleName(), "download - parse");
            if (!isCancelled() && validateResult(params, result)) {
                // Saves the cache file.
                FileUtils.moveFile(tempFile, cacheFile);
                return result;
            }
        }

        return null;
    }
}
