package android.ext.temp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.concurrent.Executor;
import android.content.Context;
import android.ext.content.AsyncTaskLoader;
import android.ext.net.DownloadRequest;
import android.ext.temp.AsyncJsonLoader.LoadParams;
import android.ext.util.FileUtils;
import android.ext.util.JsonUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.StringUtils;
import android.text.TextUtils;
import android.util.Log;

/**
 * Class <tt>AsyncJsonLoader</tt> allows to load the JSON value on a background thread and publish
 * results on the UI thread.
 * <h5>AsyncJsonLoader's generic types</h5>
 * <p>The two types used by a JSON loader are the following:</p>
 * <ol><li><tt>Key</tt>, The loader's key type.</li>
 * <li><tt>Result</tt>, The load result type, must be <tt>JSONObject</tt> or <tt>JSONArray</tt>.</li></ol>
 * @author Garfield
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class AsyncJsonLoader<Key, Result> extends AsyncTaskLoader<Key, LoadParams<Key>, Result> {
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
     * @param result The JSON value. May be a <tt>JSONObject</tt> or <tt>JSONArray</tt>.
     * @return <tt>true</tt> if the <em>result</em> is valid, <tt>false</tt> otherwise.
     */
    protected boolean validateResult(Key key, LoadParams<Key> params, Result result) {
        return true;
    }

    @Override
    protected Result loadInBackground(Task<?, ?> task, Key key, LoadParams<Key>[] args) {
        Result result = null;
        try {
            final LoadParams params = args[0];
            final String cacheFile  = params.getCacheFile(key);
            if (TextUtils.isEmpty(cacheFile)) {
                result = params.newDownloadRequest(key).download(task, null);
            } else {
                loadFromCache(task, key, params, cacheFile);
                if (!isTaskCancelled(task)) {
                    result = download(task, key, params, cacheFile);
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load JSON value - key = " + key + "\n" + e);
        }

        return result;
    }

    private void loadFromCache(Task task, Key key, LoadParams params, String cacheFile) {
        Result result = null;
        try {
            result = JsonUtils.newInstance(cacheFile, task);
        } catch (Exception e) {
            Log.w(getClass().getName(), "Couldn't load JSON value from the cache - " + cacheFile);
        }

        params.mHitCache = (result != null);
        task.setProgress(result);
    }

    private Result download(Task task, Key key, LoadParams params, String cacheFile) throws Exception {
        final String tempFile = cacheFile + ".tmp";
        final int statusCode  = params.newDownloadRequest(key).download(tempFile, task, null);
        if (statusCode == HttpURLConnection.HTTP_OK && !isTaskCancelled(task)) {
            if (params.mHitCache) {
                final byte[] digest1 = MessageDigests.computeFile(tempFile, Algorithm.SHA1);
                final byte[] digest2 = MessageDigests.computeFile(cacheFile, Algorithm.SHA1);
                if (Arrays.equals(digest1, digest2)) {
                    return null;
                }
            }

            final Result result = JsonUtils.newInstance(tempFile, task);
            if (!isTaskCancelled(task) && validateResult(key, params, result)) {
                FileUtils.moveFile(tempFile, cacheFile);
                return result;
            }
        }

        return null;
    }

    /**
     * Class <tt>LoadParams</tt> used to {@link AsyncJsonLoader} to load JSON value.
     */
    public static abstract class LoadParams<Key> {
        /* package */ volatile boolean mHitCache;

        /**
         * Tests if the JSON cache file parse succeeded.
         * @return <tt>true</tt> if parse succeeded, <tt>false</tt> otherwise.
         */
        public final boolean isHitCache() {
            return mHitCache;
        }

        /**
         * Returns the absolute path of the JSON cache file on the filesystem.
         * @param key The key, passed earlier by {@link AsyncJsonLoader#load}.
         * @return The path of the JSON cache file, or <tt>null</tt> if no cache file.
         */
        public String getCacheFile(Key key) {
            return null;
        }

        /**
         * Returns a new download request with the specified <em>key</em>.
         * @param key The key, passed earlier by {@link AsyncJsonLoader#load}.
         * @return The instance of {@link DownloadRequest}.
         * @throws IOException if an error occurs while opening the connection.
         */
        public abstract DownloadRequest newDownloadRequest(Key key) throws IOException;
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
            return StringUtils.toHexString(new StringBuilder(mContext.getCacheDir().getPath()).append("/.json_files/"), digest, 0, digest.length, true).toString();
        }

        @Override
        public DownloadRequest newDownloadRequest(String url) throws IOException {
            return new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
        }
    }
}
