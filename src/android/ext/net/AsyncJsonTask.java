package android.ext.net;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Arrays;
import android.ext.content.AbsAsyncTask;
import android.ext.util.FileUtils;
import android.ext.util.JsonUtils;
import android.util.Log;
import android.util.Pair;

/**
 * Class <tt>AsyncJsonTask</tt> allows to load the JSON data on a background thread and publish
 * results on the UI thread. This class can be support the JSON cache file.
 * <h5>AsyncJsonTask's generic types</h5>
 * <p>The two types used by a JSON task are the following:</p>
 * <ol><li><tt>Params</tt>, The parameters type of the task.</li>
 * <li><tt>Result</tt>, The load result type, must be <tt>JSONObject</tt> or <tt>JSONArray</tt>.</li></ol>
 * <h2>Usage</h2>
 * <p>Here is an example of subclassing:</p><pre>
 * private static class JsonTask extends AsyncJsonTask&lt;String, JSONObject&gt; {
 *     public JsonTask(Activity ownerActivity) {
 *         super(ownerActivity);
 *     }
 *
 *     protected File getCacheFile(String[] params) {
 *         return new File("mnt/sdcard/xxx/cacheFile.json");
 *     }
 *
 *     protected DownloadRequest newDownloadRequest(String[] params) throws Exception {
 *         return new DownloadRequest(params[0]).connectTimeout(30000).readTimeout(30000);
 *     }
 *
 *     protected boolean validateResult(String[] params, JSONObject result) {
 *         return (result != null && result.optInt("retCode") == 200);
 *     }
 *
 *     protected void onPostExecute(Pair&lt;JSONObject, Boolean&gt result) {
 *         final Activity activity = getOwnerActivity();
 *         if (activity == null) {
 *             // The owner activity has been destroyed or release by the GC.
 *             return;
 *         }
 *
 *         if (result.first != null) {
 *             // Loading succeeded, update UI.
 *         } else if (!result.second) {
 *             // Loading failed and file cache not hit, show error or empty UI.
 *         }
 *     }
 * }
 *
 * new JsonTask(activity).execute(url);</pre>
 * @author Garfield
 */
public abstract class AsyncJsonTask<Params, Result> extends AbsAsyncTask<Params, Object, Pair<Result, Boolean>> {
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
     * The default implementation returns <tt>null</tt> value.</p>
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
     * @param result The JSON value or <tt>null<tt>.
     * @return <tt>true</tt> if the <em>result</em> is valid, <tt>false</tt> otherwise.
     */
    protected boolean validateResult(Params[] params, Result result) {
        return (result != null);
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
        onPostExecute(new Pair<Result, Boolean>((Result)values[0], false));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Pair<Result, Boolean> doInBackground(Params... params) {
        boolean hitCache = false;
        Result result = null;
        try {
            final File cacheFile = getCacheFile(params);
            if (cacheFile == null) {
                result = newDownloadRequest(params).download(this, null);
                if (isCancelled() || !validateResult(params, result)) {
                    result = null;
                }
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
        Result result = null;
        try {
            result = JsonUtils.parse(null, cacheFile, this);
        } catch (Exception e) {
            Log.w(getClass().getName(), "Couldn't load JSON data from the cache - " + cacheFile);
        }

        final boolean hitCache = validateResult(params, result);
        if (hitCache) {
            publishProgress(result);
        }

        return hitCache;
    }

    private Result download(Params[] params, String cacheFile, boolean hitCache) throws Exception {
        final String tempFile = cacheFile + ".tmp";
        final int statusCode  = newDownloadRequest(params).download(tempFile, this, null);
        if (statusCode == HttpURLConnection.HTTP_OK && !isCancelled()) {
            // If cache file is hit and the cache file's contents are equal
            // the temp file's contents. Returns null, do not update UI.
            if (hitCache && FileUtils.compareFile(cacheFile, tempFile)) {
                return null;
            }

            // Parse the temp file and save it to the cache file.
            final Result result = JsonUtils.parse(null, tempFile, this);
            if (!isCancelled() && validateResult(params, result)) {
                FileUtils.moveFile(tempFile, cacheFile);
                return result;
            }
        }

        return null;
    }
}
