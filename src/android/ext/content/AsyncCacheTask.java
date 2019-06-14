package android.ext.content;

import static java.net.HttpURLConnection.HTTP_OK;
import java.io.File;
import java.util.Arrays;
import android.app.Activity;
import android.content.Context;
import android.ext.net.DownloadRequest;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.JsonUtils;
import android.util.Log;

/**
 * Class <tt>AsyncCacheTask</tt> allows to load the resource on a background thread
 * and publish results on the UI thread. This class can be support the cache file.
 * <h3>Usage</h3>
 * <p>Here is an example of subclassing:</p><pre>
 * private static class JsonTask extends AsyncCacheTask&lt;String&gt; {
 *     public JsonTask(Activity ownerActivity) {
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
 *     protected Object parseResult(String[] urls, File cacheFile) throws Exception {
 *         if (!cacheFile.exists()) {
 *             // If the cache file not exists, return null or parse the JSON data from the "assets" file.
 *             return null;
 *         }
 *
 *         // Parse the JSON data ... ...
 *         final JSONObject result = JsonUtils.parse(mContext, cacheFile, this);
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
 *     protected void onPostExecute(Object result) {
 *         final Activity activity = getOwnerActivity();
 *         if (activity == null) {
 *             // The owner activity has been destroyed or release by the GC.
 *             return;
 *         }
 *
 *         if (isInvalidResult(result)) {
 *             // The result is invalid, do not update UI.
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
public abstract class AsyncCacheTask<Params> extends AbsAsyncTask<Params, Object, Object> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @see #AsyncCacheTask(Activity)
     */
    public AsyncCacheTask(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Constructor
     * @param activity The <tt>Activity</tt>.
     * @see #AsyncCacheTask(Context)
     */
    public AsyncCacheTask(Activity activity) {
        super(activity);
        mContext = activity.getApplicationContext();
    }

    /**
     * Returns the absolute path of the cache file on the filesystem.
     * <p>Subclasses should override this method to returns the cache
     * file path. The default implementation returns <tt>null</tt>.</p>
     * @param params The parameters, passed earlier by {@link #execute(Params[])}.
     * @return The path of the cache file, or <tt>null</tt> if no cache file.
     */
    protected File getCacheFile(Params[] params) {
        return null;
    }

    /**
     * Tests if the <em>result</em> is invalid.
     * @param result The result to test.
     * @return <tt>true</tt> if the <em>result</em> is invalid, <tt>false</tt> otherwise.
     */
    protected final boolean isInvalidResult(Object result) {
        return (result == this);
    }

    /**
     * Called on a background thread to parse the data from the cache file. Subclasses
     * should override this method to parse their result. <p>The default implementation
     * parse the cache file's contents to a <tt>JSONObject</tt> or <tt>JSONArray</tt>.</p>
     * @param params The parameters, passed earlier by {@link #execute(Params[])}.
     * @param cacheFile The cache file to parse.
     * @return A result or <tt>null</tt>, defined by the subclass of this task.
     * @throws Exception if the data can not be parse.
     * @see JsonUtils#parse(Context, Object, Cancelable)
     */
    protected Object parseResult(Params[] params, File cacheFile) throws Exception {
        return JsonUtils.parse(mContext, cacheFile, this);
    }

    /**
     * Returns a new download request with the specified <em>params</em>.
     * @param params The parameters, passed earlier by {@link #execute(Params[])}.
     * @return The instance of {@link DownloadRequest}.
     * @throws Exception if an error occurs while opening the connection.
     */
    protected abstract DownloadRequest newDownloadRequest(Params[] params) throws Exception;

    @Override
    protected void onProgressUpdate(Object... values) {
        onPostExecute(values[0]);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object doInBackground(Params... params) {
        Object result = null;
        try {
            final File cacheFile = getCacheFile(params);
            if (cacheFile == null) {
                DebugUtils.__checkStartMethodTracing();
                result = download(params);
                DebugUtils.__checkStopMethodTracing("AsyncCacheTask", "download");
            } else {
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

    private Object download(Params[] params) throws Exception {
        final File tempFile = new File(FileUtils.getCacheDir(mContext, null), "._act-" + Thread.currentThread().hashCode());
        try {
            final int statusCode = newDownloadRequest(params).download(tempFile.getPath(), this, null);
            final Object result  = (statusCode == HTTP_OK && !isCancelled() ? parseResult(params, tempFile) : null);
            DebugUtils.__checkError(isInvalidResult(result), "Invalid result = " + result);
            return result;
        } finally {
            tempFile.delete();
        }
    }

    private boolean loadFromCache(Params[] params, File cacheFile) {
        try {
            DebugUtils.__checkStartMethodTracing();
            final Object result = parseResult(params, cacheFile);
            DebugUtils.__checkStopMethodTracing("AsyncCacheTask", "loadFromCache");
            DebugUtils.__checkError(isInvalidResult(result), "Invalid result = " + result);
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

    private Object download(Params[] params, String cacheFile, boolean hitCache) throws Exception {
        final String tempFile = cacheFile + "." + Thread.currentThread().hashCode();
        final int statusCode  = newDownloadRequest(params).download(tempFile, this, null);
        if (statusCode == HTTP_OK && !isCancelled()) {
            // If the cache file is hit and the cache file's contents are equal the temp
            // file's contents. Deletes the temp file and returns this, do not update UI.
            if (hitCache && FileUtils.compareFile(cacheFile, tempFile)) {
                FileUtils.deleteFiles(tempFile, false);
                return this;
            }

            // Parse the temp file and save it to the cache file.
            DebugUtils.__checkStartMethodTracing();
            final Object result = parseResult(params, new File(tempFile));
            DebugUtils.__checkStopMethodTracing("AsyncCacheTask", "parseResult");
            DebugUtils.__checkError(isInvalidResult(result), "Invalid result = " + result);
            if (result != null) {
                FileUtils.moveFile(tempFile, cacheFile);
                return result;
            }
        }

        return null;
    }
}
