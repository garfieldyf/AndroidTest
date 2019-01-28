package android.ext.content;

import java.net.HttpURLConnection;
import java.util.concurrent.Executor;
import android.content.Context;
import android.ext.content.AsyncJsonLoader.LoadParams;
import android.ext.net.DownloadRequest;
import android.ext.util.FileUtils;
import android.ext.util.JsonUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.StringUtils;
import android.text.TextUtils;
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
 * <p>Here is an example:</p><pre>
 * public final class JsonLoader extends AsyncJsonLoader&lt;String, JSONObject&gt; {
 *     public JsonLoader(Executor executor, Activity ownerActivity) {
 *         super(executor, ownerActivity);
 *     }
 *
 *     protected void onStartLoading(String url, LoadParams&lt;String&gt;[] params) {
 *         final String cacheFile = params[0].getCacheFile(url);
 *         if (TextUtils.isEmpty(cacheFile) || FileUtils.access(cacheFile, FileUtils.F_OK) != 0) {
 *             // If the cache file not exists, show loading UI.
 *         }
 *     }
 *
 *     protected boolean validateResult(String url, LoadParams&lt;String&gt; params, JSONObject result) {
 *         return (result != null && result.optInt("retCode") == 200);
 *     }
 *
 *     protected void onLoadComplete(String url, LoadParams&lt;String&gt;[] params, Pair&lt;JSONObject, Boolean&gt result) {
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
 * final JsonLoader&lt;String, JSONObject&gt loader = new JsonLoader&lt;String, JSONObject&gt(executor, activity);
 * loader.load(url, new CacheLoadParams(context));</pre>
 * @author Garfield
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AsyncJsonLoader<Key, Result> extends AsyncTaskLoader<Key, LoadParams<Key>, Pair<Result, Boolean>> {
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
     * Tests if the <em>result</em> is valid. Subclasses should override this method to
     * validate the <em>result</em>.
     * @param key The key, passed earlier by {@link #load}.
     * @param params The {@link LoadParams}, passed earlier by {@link #load}.
     * @param result The JSON data. May be a <tt>JSONObject</tt> or <tt>JSONArray</tt>.
     * @return <tt>true</tt> if the <em>result</em> is valid, <tt>false</tt> otherwise.
     */
    protected boolean validateResult(Key key, LoadParams<Key> params, Result result) {
        return (result != null);
    }

    @Override
    protected void onProgressUpdate(Key key, LoadParams<Key>[] params, Object[] values) {
        onLoadComplete(key, params, new Pair<Result, Boolean>((Result)values[0], false));
    }

    @Override
    protected Pair<Result, Boolean> loadInBackground(Task<?, ?> task, Key key, LoadParams<Key>[] loadParams) {
        boolean hitCache = false;
        Result result = null;
        try {
            final LoadParams params = loadParams[0];
            final String cacheFile  = params.getCacheFile(key);
            if (TextUtils.isEmpty(cacheFile)) {
                result = params.newDownloadRequest(key).download(task, null);
                if (isTaskCancelled(task) || !validateResult(key, params, result)) {
                    result = null;
                }
            } else {
                hitCache = loadFromCache(task, cacheFile);
                if (!isTaskCancelled(task)) {
                    result = download(task, key, params, cacheFile, hitCache);
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load JSON data - key = " + key + "\n" + e);
        }

        return new Pair<Result, Boolean>(result, hitCache);
    }

    private boolean loadFromCache(Task task, String cacheFile) {
        Result result = null;
        try {
            result = JsonUtils.parse(null, cacheFile, task);
        } catch (Exception e) {
            Log.w(getClass().getName(), "Couldn't load JSON data from the cache - " + cacheFile);
        }

        if (result != null) {
            task.setProgress(result);
            return true;
        } else {
            return false;
        }
    }

    private Result download(Task task, Key key, LoadParams params, String cacheFile, boolean hitCache) throws Exception {
        final String tempFile = cacheFile + ".tmp";
        final int statusCode  = params.newDownloadRequest(key).download(tempFile, task, null);
        if (statusCode == HttpURLConnection.HTTP_OK && !isTaskCancelled(task)) {
            // If cache file is hit and the cache file contents equals
            // the temp file contents. Returns null, do not update UI.
            if (hitCache && FileUtils.compareFile(cacheFile, tempFile)) {
                return null;
            }

            // Parse the temp file and save it to the cache file.
            final Result result = JsonUtils.parse(null, tempFile, task);
            if (!isTaskCancelled(task) && validateResult(key, params, result)) {
                FileUtils.moveFile(tempFile, cacheFile);
                return result;
            }
        }

        return null;
    }

    /**
     * Class <tt>LoadParams</tt> used to {@link AsyncJsonLoader} to load JSON data.
     */
    public static abstract class LoadParams<Key> {
        /**
         * Returns the absolute path of the JSON cache file on the filesystem.
         * <p><b>Note: This method called on a background thread.</b></p>
         * @param key The key, passed earlier by {@link AsyncJsonLoader#load}.
         * @return The path of the JSON cache file, or <tt>null</tt> if no cache file.
         */
        public String getCacheFile(Key key) {
            return null;
        }

        /**
         * Returns a new download request with the specified <em>key</em>.
         * <p><b>Note: This method called on a background thread.</b></p>
         * @param key The key, passed earlier by {@link AsyncJsonLoader#load}.
         * @return The instance of {@link DownloadRequest}.
         * @throws Exception if an error occurs while opening the connection.
         */
        public abstract DownloadRequest newDownloadRequest(Key key) throws Exception;
    }

    /**
     * Class <tt>URLLoadParams</tt> is an implementation of a {@link LoadParams}.
     */
    public static final class URLLoadParams extends LoadParams<String> {
        @Override
        public DownloadRequest newDownloadRequest(String url) throws Exception {
            return new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
        }
    }

    /**
     * Class <tt>CacheLoadParams</tt> is an implementation of a {@link LoadParams}.
     */
    public static class CacheLoadParams extends LoadParams<String> {
        /**
         * The application <tt>Context</tt>.
         */
        public final Context mContext;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         */
        public CacheLoadParams(Context context) {
            mContext = context.getApplicationContext();
        }

        @Override
        public String getCacheFile(String url) {
            final byte[] digest = MessageDigests.computeString(url, Algorithm.SHA1);
            return StringUtils.toHexString(new StringBuilder(mContext.getFilesDir().getPath()).append("/.json_files/"), digest, 0, digest.length).toString();
        }

        @Override
        public DownloadRequest newDownloadRequest(String url) throws Exception {
            return new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
        }
    }
}
