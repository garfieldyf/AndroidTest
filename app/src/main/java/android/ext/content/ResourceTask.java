package android.ext.content;

import static java.net.HttpURLConnection.HTTP_OK;
import android.app.Activity;
import android.content.Context;
import android.ext.net.DownloadRequest;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.util.Log;
import java.io.File;
import java.util.Arrays;

/**
 * Class <tt>ResourceTask</tt> allows to load the resource on a background thread
 * and publish results on the UI thread. This class can be support the cache file.
 * <h3>ResourceTask's generic types</h3>
 * <p>The two types used by a task are the following:</p>
 * <ol><li><tt>Params</tt>, The type of the parameters sent to the task.</li>
 * <li><tt>Result</tt>, The type of the result of the task.</li></ol>
 * <h3>Usage</h3>
 * <p>Here is an example of subclassing:</p><pre>
 * private static class JSONTask extends ResourceTask&lt;String, JSONObject&gt; {
 *     public JSONTask(Activity ownerActivity) {
 *         super(ownerActivity);
 *     }
 *
 *     {@code @Override}
 *     protected File getCacheFile(String[] urls) {
 *         // Builds the cache file, For example:
 *         return new File(mContext.getFilesDir(), "xxx/cacheFile.json");
 *     }
 *
 *     {@code @Override}
 *     protected DownloadRequest newDownloadRequest(String[] urls) throws Exception {
 *         return new DownloadRequest(urls[0]).connectTimeout(30000).readTimeout(30000);
 *     }
 *
 *     {@code @Override}
 *     protected JSONObject parseResult(String[] urls, File cacheFile) throws Exception {
 *         final JSONObject result;
 *         if (cacheFile == null) {
 *             // If no cache file, parse the JSON data from the network.
 *             result = newDownloadRequest(urls).download(this);
 *         } else if (cacheFile.exists()) {
 *             // Parse the JSON data from the cache file.
 *             result = JSONUtils.parse(mContext, cacheFile, this);
 *         } else {
 *             // If the cache file not exists, parse the JSON data from the "assets" file.
 *             result = JSONUtils.parse(mContext, UriUtils.getAssetUri("cacheFile.json"), this);
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
 *
 *     {@code @Override}
 *     protected void onPostExecute(JSONObject result) {
 *         final Activity activity = getOwnerActivity();
 *         if (activity == null) {
 *             // The owner activity has been destroyed or release by the GC.
 *             return;
 *         }
 *
 *         if (result != null) {
 *             // Loading succeeded, update UI.
 *         } else {
 *             // 1.If the cache file is hit, do not update UI.
 *             // 2.Loading failed, show error or empty UI.
 *         }
 *     }
 * }
 *
 * new JSONTask(activity).execute(url);</pre>
 * @author Garfield
 */
public abstract class ResourceTask<Params, Result> extends AbsAsyncTask<Params, Object, Result> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @see #ResourceTask(Activity)
     */
    public ResourceTask(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Constructor
     * @param activity The owner <tt>Activity</tt>.
     * @see #ResourceTask(Context)
     */
    public ResourceTask(Activity ownerActivity) {
        super(ownerActivity);
        mContext = ownerActivity.getApplicationContext();
    }

    /**
     * Called on a background thread to returns the absolute path of the cache file on the filesystem.
     * The default implementation returns <tt>null</tt>.
     * @param params The parameters, passed earlier by {@link #execute(Params[])}.
     * @return The path of the cache file, or <tt>null</tt> if no cache file.
     */
    protected File getCacheFile(Params[] params) {
        return null;
    }

    /**
     * Called on a background thread to returns a new download request with the specified <em>params</em>.
     * @param params The parameters, passed earlier by {@link #execute(Params[])}.
     * @return The instance of {@link DownloadRequest}.
     * @throws Exception if an error occurs while opening the connection.
     */
    protected abstract DownloadRequest newDownloadRequest(Params[] params) throws Exception;

    /**
     * Called on a background thread to parse the data from the cache file.
     * @param params The parameters, passed earlier by {@link #execute(Params[])}.
     * @param cacheFile The cache file's content to parse, or <tt>null</tt> if no cache file.
     * @return A result or <tt>null</tt>, defined by the subclass of this task.
     * @throws Exception if the data can not be parse.
     */
    protected abstract Result parseResult(Params[] params, File cacheFile) throws Exception;

    @Override
    @SuppressWarnings("unchecked")
    protected void onProgressUpdate(Object... values) {
        onPostExecute((Result)values[0]);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Result doInBackground(Params... params) {
        Result result = null;
        try {
            final File cacheFile = getCacheFile(params);
            if (cacheFile == null) {
                DebugUtils.__checkStartMethodTracing();
                result = parseResult(params, null);
                DebugUtils.__checkStopMethodTracing("ResourceTask", "no cache file parseResult params = " + Arrays.toString(params));
            } else {
                DebugUtils.__checkError(cacheFile.getPath().length() == 0, "The cacheFile is 0-length");
                final boolean hitCache = loadFromCache(params, cacheFile);
                if (!isCancelled()) {
                    result = download(params, cacheFile.getPath(), hitCache);
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load resource - params = " + Arrays.toString(params) + "\n" + e);
        }

        return result;
    }

    private boolean loadFromCache(Params[] params, File cacheFile) {
        try {
            DebugUtils.__checkStartMethodTracing();
            final Result result = parseResult(params, cacheFile);
            DebugUtils.__checkStopMethodTracing("ResourceTask", "loadFromCache params = " + Arrays.toString(params) + ", cacheFile = " + cacheFile);
            if (result != null) {
                // If this task was cancelled then invoking publishProgress has no effect.
                publishProgress(result);
                return true;
            }
        } catch (Exception e) {
            Log.w(getClass().getName(), "Couldn't load resource from the cache - " + cacheFile.getPath());
        }

        return false;
    }

    private Result download(Params[] params, String cacheFile, boolean hitCache) throws Exception {
        final String tempFile = cacheFile + "." + Thread.currentThread().hashCode();
        final int statusCode  = newDownloadRequest(params).download(tempFile, this, null);
        // If download failed or this task was cancelled, deletes the temp file.
        if (statusCode != HTTP_OK || isCancelled()) {
            DebugUtils.__checkDebug(true, "ResourceTask", "downloads params = " + Arrays.toString(params) + " statusCode = " + statusCode + ", isCancelled = " + isCancelled());
            FileUtils.deleteFiles(tempFile, false);
            return null;
        }

        // If the cache file is hit and the cache file's contents are equal the temp
        // file's contents. Deletes the temp file and cancel this task, do not update UI.
        if (hitCache && FileUtils.compareFile(cacheFile, tempFile)) {
            DebugUtils.__checkDebug(true, "ResourceTask", "compareFile is equals, do not update UI. params = " + Arrays.toString(params));
            FileUtils.deleteFiles(tempFile, false);
            cancel(false);
            return null;
        }

        // Parse the temp file and save it to the cache file.
        DebugUtils.__checkStartMethodTracing();
        final Result result = parseResult(params, new File(tempFile));
        DebugUtils.__checkStopMethodTracing("ResourceTask", "downloads params = " + Arrays.toString(params));
        if (result != null) {
            FileUtils.moveFile(tempFile, cacheFile);
        }

        return result;
    }
}
