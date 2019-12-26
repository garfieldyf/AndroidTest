package android.ext.content;

import static java.net.HttpURLConnection.HTTP_OK;
import android.app.Activity;
import android.content.Context;
import android.ext.net.DownloadRequest;
import android.ext.util.ContextCompat;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.os.Process;
import android.util.Log;
import java.io.File;
import java.util.Arrays;

/**
 * Class <tt>AsyncLoadTask</tt> allows to load the resource on a background thread
 * and publish results on the UI thread. This class can be support the cache file.
 * <h3>AsyncLoadTask's generic types</h3>
 * <p>The two types used by a task are the following:</p>
 * <ol><li><tt>Params</tt>, The type of the parameters sent to the task.</li>
 * <li><tt>Result</tt>, The type of the result of the task.</li></ol>
 * <h3>Usage</h3>
 * <p>Here is an example of subclassing:</p><pre>
 * private static class JSONTask extends AsyncLoadTask&lt;String, JSONObject&gt; {
 *     public JSONTask(Activity ownerActivity) {
 *         super(ownerActivity);
 *     }
 *
 *    {@code @Override}
 *     protected File getCacheFile(String[] urls) {
 *         // Builds the cache file, For example:
 *         return new File(mContext.getFilesDir(), "xxx/cacheFile.json");
 *         // or
 *         final File cacheFile = new File(context.getFilesDir(), "xxx/cacheFile.json");
 *         if (!cacheFile.exists()) {
 *             // Copy assets file to cacheFile.
 *         }
 *
 *         return cacheFile.
 *     }
 *
 *    {@code @Override}
 *     protected DownloadRequest newDownloadRequest(String[] urls) throws Exception {
 *         return new DownloadRequest(urls[0]).connectTimeout(30000).readTimeout(30000);
 *     }
 *
 *    {@code @Override}
 *     protected JSONObject parseResult(String[] urls, File cacheFile) throws Exception {
 *         final JSONObject result;
 *         if (cacheFile == null) {
 *             // If no cache file, parse the JSON data from the network.
 *             result = newDownloadRequest(urls).download(this);
 *         } else {
 *             // Parse the JSON data from the cache file.
 *             result = JSONUtils.parse(mContext, cacheFile, this);
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
 *    {@code @Override}
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
 *             // Loading failed, show error or empty UI.
 *         }
 *     }
 * }
 *
 * new JSONTask(activity).execute(url);
 * or
 * private static class LoadCompleteListener implements OnLoadCompleteListener&lt;String, JSONObject&gt; {
 *    {@code @Override}
 *     public void onLoadComplete(String[] urls, JSONObject result) {
 *         if (result != null) {
 *             // Loading succeeded, update UI.
 *         } else {
 *             // Loading failed, show error or empty UI.
 *         }
 *     }
 * }
 *
 * final JSONTask task = new JSONTask(activity);
 * task.setOnLoadCompleteListener(new LoadCompleteListener());
 * task.execute(url);</pre>
 * @author Garfield
 */
public abstract class AsyncLoadTask<Params, Result> extends AbsAsyncTask<Params, Object, Result> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * The parameters of this task.
     */
    protected Params[] mParams;

    /**
     * The {@link OnLoadCompleteListener} to receive callbacks when a load is complete.
     */
    private OnLoadCompleteListener<Params, Result> mListener;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @see #AsyncLoadTask(Activity)
     */
    public AsyncLoadTask(Context context) {
        mContext = ContextCompat.getContext(context);
    }

    /**
     * Constructor
     * @param activity The owner <tt>Activity</tt>.
     * @see #AsyncLoadTask(Context)
     */
    public AsyncLoadTask(Activity ownerActivity) {
        super(ownerActivity);
        mContext = ContextCompat.getContext(ownerActivity);
    }

    /**
     * Sets An {@link OnLoadCompleteListener} to receive callbacks when a load is complete.
     * @param listener The <tt>OnLoadCompleteListener</tt>.
     */
    public final void setOnLoadCompleteListener(OnLoadCompleteListener<Params, Result> listener) {
        mListener = listener;
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
    protected final Result doInBackground(Params[] params) {
        mParams = params;
        final File cacheFile = getCacheFile(params);
        if (cacheFile == null) {
            return parseResult(params);
        } else {
            final boolean hitCache = loadFromCache(params, cacheFile);
            return (isCancelled() ? null : download(params, cacheFile.getPath(), hitCache));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void onProgressUpdate(Object... values) {
        onPostExecute((Result)values[0]);
    }

    @Override
    protected void onPostExecute(Result result) {
        if (mListener != null && validateOwner(mOwner)) {
            mListener.onLoadComplete(mParams, result);
        }
    }

    private Result parseResult(Params[] params) {
        Result result = null;
        try {
            DebugUtils.__checkStartMethodTracing();
            result = parseResult(params, null);
            DebugUtils.__checkStopMethodTracing("AsyncLoadTask", "parse result - params = " + Arrays.toString(params));
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't parse result - params = " + Arrays.toString(params) + "\n" + e);
        }

        return result;
    }

    private boolean loadFromCache(Params[] params, File cacheFile) {
        final int priority = Process.getThreadPriority(Process.myTid());
        try {
            DebugUtils.__checkStartMethodTracing();
            Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
            final Result result = parseResult(params, cacheFile);
            DebugUtils.__checkStopMethodTracing("AsyncLoadTask", "loadFromCache - params = " + Arrays.toString(params) + ", cacheFile = " + cacheFile);
            if (result != null) {
                // If this task was cancelled then invoking publishProgress has no effect.
                publishProgress(result);
                return true;
            }
        } catch (Exception e) {
            Log.w(getClass().getName(), "Couldn't load resource from the cache - " + cacheFile);
        } finally {
            Process.setThreadPriority(priority);
        }

        return false;
    }

    private Result download(Params[] params, String cacheFile, boolean hitCache) {
        final String tempFile = cacheFile + "." + Thread.currentThread().hashCode();
        Result result = null;
        try {
            final int statusCode = newDownloadRequest(params).download(tempFile, this, null);
            if (statusCode == HTTP_OK && !isCancelled() && !(hitCache && FileUtils.compareFile(cacheFile, tempFile))) {
                // If the cache file is not equals the temp file, parse the temp file.
                DebugUtils.__checkStartMethodTracing();
                result = parseResult(params, new File(tempFile));
                DebugUtils.__checkStopMethodTracing("AsyncLoadTask", DebugUtils.toString(result, new StringBuilder("downloads - result = ")).append(", params = ").append(Arrays.toString(params)).toString());
                if (result != null) {
                    // Save the temp file to the cache file.
                    FileUtils.moveFile(tempFile, cacheFile);
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load resource - params = " + Arrays.toString(params) + "\n" + e);
        }

        if (hitCache && result == null) {
            // If the cache file is hit and parse the temp file failed,
            // cancel this task and delete the temp file, do not update UI.
            DebugUtils.__checkDebug(true, "AsyncLoadTask", "cancel task - params = " + Arrays.toString(params));
            cancel(false);
            FileUtils.deleteFiles(tempFile, false);
        }

        return result;
    }

    /**
     * Callback interface when an {@link AsyncLoadTask} has finished loading its data.
     */
    public static interface OnLoadCompleteListener<Params, Result> {
        /**
         * Called on the UI thread when the load is complete.
         * @param params The parameters, passed earlier by {@link #execute}.
         * @param result A result or <tt>null</tt> of the load.
         */
        void onLoadComplete(Params[] params, Result result);
    }
}
