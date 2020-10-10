package android.ext.content;

import static java.net.HttpURLConnection.HTTP_OK;
import android.content.Context;
import android.ext.net.DownloadRequest;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.FileUtils;
import android.os.Process;
import android.util.Log;
import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Class <tt>ResourceLoader</tt> allows to load the resource from the web on a background
 * thread and publish results on the UI thread. This class can be support the cache file.
 * <h3>Usage</h3>
 * <p>Here is an example:</p><pre>
 * private static class JSONLoadParams implements LoadParams&lt;String, JSONObject&gt; {
 *    {@code @Override}
 *     public File getCacheFile(Context context, String[] urls) {
 *         // Builds the cache file, For example:
 *         return new File(context.getFilesDir(), "xxx/cacheFile.json");
 *     }
 *
 *    {@code @Override}
 *     public DownloadRequest newDownloadRequest(Context context, String[] urls) throws Exception {
 *         return new DownloadRequest(urls[0]).connectTimeout(30000).readTimeout(30000);
 *     }
 *
 *    {@code @Override}
 *     public JSONObject parseResult(Context context, String[] urls, File cacheFile, Cancelable cancelable) throws Exception {
 *         final JSONObject result;
 *         if (cacheFile == null) {
 *             // If no cache file, parse the JSON data from the web.
 *             result = newDownloadRequest(context, urls).download(cancelable);
 *         } else if (cacheFile.exists()) {
 *             // Parse the JSON data from the cache file.
 *             result = JSONUtils.parse(context, cacheFile, cancelable);
 *         } else {
 *             // Parse the JSON data from the assets.
 *             result = JSONUtils.parse(context, UriUtils.getAssetUri("xxx/cacheFile.json"), cancelable);
 *             // or
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
 * }
 *
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
 * new ResourceLoader&lt;String, JSONObject&gt;(context, new JSONLoadParams())
 *    .setWeakOnLoadCompleteListener(new LoadCompleteListener())
 *    .setOwner(activity)   // May be an <tt>Activity, LifecycleOwner, Lifecycle</tt> or <tt>Fragment</tt> etc.
 *    .execute(url);</pre>
 * @author Garfield
 */
public class ResourceLoader<Params, Result> extends AsyncTask<Params, Result, Result> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * The {@link LoadParams} used to load resource.
     */
    private final LoadParams<Params, Result> mLoadParams;

    /**
     * The {@link OnLoadCompleteListener} to receive callbacks when a load is complete.
     */
    private WeakReference<OnLoadCompleteListener<Params, Result>> mListener;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param loadParams The {@link LoadParams} used to load resource.
     */
    public ResourceLoader(Context context, LoadParams<Params, Result> loadParams) {
        mLoadParams = loadParams;
        mContext = context.getApplicationContext();
    }

    /**
     * Sets An {@link OnLoadCompleteListener} to receive callbacks when a load is complete.
     * The listener is internally held as {@link WeakReference weak reference}.
     * @param listener The <tt>OnLoadCompleteListener</tt> to set.
     * @return This <em>loader</em>.
     */
    public final ResourceLoader<Params, Result> setWeakOnLoadCompleteListener(OnLoadCompleteListener<Params, Result> listener) {
        mListener = new WeakReference<OnLoadCompleteListener<Params, Result>>(listener);
        return this;
    }

    @Override
    protected Result doInBackground(Params[] params) {
        final File cacheFile = mLoadParams.getCacheFile(mContext, params);
        if (cacheFile == null) {
            return download(params);
        } else {
            final boolean hitCache = loadFromCache(params, cacheFile);
            return (mWorker.isCancelled() ? null : download(params, cacheFile.getPath(), hitCache));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void onProgressUpdate(Result[] values) {
        onPostExecute((Params[])mWorker.mParams, values[0]);
    }

    @Override
    protected void onPostExecute(Params[] params, Result result) {
        DebugUtils.__checkError(mListener == null, "The " + getClass().getName() + " did not call setWeakOnLoadCompleteListener()");
        final OnLoadCompleteListener<Params, Result> listener = mListener.get();
        if (listener != null) {
            listener.onLoadComplete(params, result);
        }
    }

    private Result download(Params[] params) {
        Result result = null;
        try {
            DebugUtils.__checkStartMethodTracing();
            result = mLoadParams.parseResult(mContext, params, null, mWorker);
            DebugUtils.__checkStopMethodTracing(getClass().getName(), "downloads");
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't parse result.\n" + e);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private boolean loadFromCache(Params[] params, File cacheFile) {
        final int priority = Process.getThreadPriority(Process.myTid());
        try {
            DebugUtils.__checkStartMethodTracing();
            Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
            final Result result = mLoadParams.parseResult(mContext, params, cacheFile, null);
            DebugUtils.__checkStopMethodTracing(getClass().getName(), "loadFromCache - cacheFile = " + cacheFile);
            if (result != null) {
                // Loads from the cache file succeeded, update UI.
                setProgress(result);
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
        final File tempFile = new File(cacheFile + ".tmp");
        Result result = null;
        try {
            final DownloadRequest request = mLoadParams.newDownloadRequest(mContext, params);
            DebugUtils.__checkError(request == null, "The " + DeviceUtils.toString(mLoadParams) + " newDownloadRequest must be implementation!");
            final int statusCode = request.download(tempFile, mWorker, null);
            if (statusCode == HTTP_OK && !mWorker.isCancelled() && !(hitCache && FileUtils.compareFile(cacheFile, tempFile.getPath()))) {
                // If the cache file is not equals the temp file, parse the temp file.
                DebugUtils.__checkStartMethodTracing();
                result = mLoadParams.parseResult(mContext, params, tempFile, null);
                DebugUtils.__checkStopMethodTracing(getClass().getName(), "parseResult - tempFile = " + tempFile);
                if (result != null) {
                    // Save the temp file to the cache file.
                    FileUtils.moveFile(tempFile.getPath(), cacheFile);
                    DebugUtils.__checkDebug(true, getClass().getName(), "save - tempFile = " + tempFile + ", cacheFile = " + cacheFile + ", hitCache = " + hitCache);
                }
            }
        } catch (Exception e) {
            tempFile.delete();
            Log.e(getClass().getName(), "Couldn't load resource.\n" + e);
        }

        /*
         * If the cache file was hit and result is null:
         *   1. If download failed or cancelled.
         *   2. The cache file and the temp file contents are equal.
         *   3. Parse the temp file failed.
         * Cancel the task and delete the temp file, do not update UI.
         */
        if (hitCache && result == null) {
            DebugUtils.__checkDebug(true, getClass().getName(), "cancel task - tempFile = " + tempFile);
            tempFile.delete();
            mWorker.cancel(false);
        }

        return result;
    }

    /**
     * Callback interface when a {@link ResourceLoader} has finished loading its data.
     */
    public static interface OnLoadCompleteListener<Params, Result> {
        /**
         * Called on the UI thread when the load is complete.
         * @param params The parameters, passed earlier by {@link #execute}.
         * @param result A result of the load or <tt>null</tt>.
         */
        void onLoadComplete(Params[] params, Result result);
    }

    /**
     * Class <tt>LoadParams</tt> used to {@link ResourceLoader} to load resource.
     */
    public static interface LoadParams<Params, Result> {
        /**
         * Called on a background thread to returns the absolute path of the cache
         * file on the filesystem. The default implementation returns <tt>null</tt>.
         * @param context The <tt>Context</tt>.
         * @param params The parameters, passed earlier by {@link #execute}.
         * @return The path of the cache file, or <tt>null</tt> if no cache file.
         */
        default File getCacheFile(Context context, Params[] params) {
            return null;
        }

        /**
         * Called on a background thread to returns a new download request
         * with the specified <em>params</em>.
         * @param context The <tt>Context</tt>.
         * @param params The parameters, passed earlier by {@link #execute}.
         * @return The {@link DownloadRequest}.
         * @throws Exception if an error occurs while opening the connection.
         */
        default DownloadRequest newDownloadRequest(Context context, Params[] params) throws Exception {
            return null;
        }

        /**
         * Called on a background thread to parse the data from the cache file.
         * @param context The <tt>Context</tt>.
         * @param params The parameters, passed earlier by {@link #execute}.
         * @param cacheFile The cache file to parse, or <tt>null</tt> if no cache file.
         * @param cancelable May be <tt>null</tt>. A {@link Cancelable} can be check the parse is cancelled.
         * @return A result or <tt>null</tt>, defined by the subclass.
         * @throws Exception if the data can not be parse.
         */
        Result parseResult(Context context, Params[] params, File cacheFile, Cancelable cancelable) throws Exception;
    }
}
