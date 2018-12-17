package android.ext.temp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.concurrent.Executor;
import android.content.Context;
import android.ext.content.AsyncTaskLoader;
import android.ext.net.DownloadRequest;
import android.ext.temp.AsyncJsonLoader.LoadParams;
import android.ext.util.FileUtils;
import android.ext.util.JSONUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.UriUtils;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

/**
 * Class <tt>AsyncJsonLoader</tt> allows to load the JSON value on a background thread and publish
 * results on the UI thread. The URI string of this loader, accepts the following URI schemes:
 * <ul><li>path (no scheme)</li>
 * <li>ftp ({@link #SCHEME_FTP})</li>
 * <li>http ({@link #SCHEME_HTTP})</li>
 * <li>https ({@link #SCHEME_HTTPS})</li>
 * <li>file ({@link #SCHEME_FILE})</li>
 * <li>content ({@link #SCHEME_CONTENT})</li></ul>
 * @param Result The load result type, must be <tt>JSONObject</tt> or <tt>JSONArray</tt>.
 * @author Garfield
 */
public class AsyncJsonLoader<Result> extends AsyncTaskLoader<String, LoadParams, Result> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @see #AsyncJsonLoader(Context, Executor, Object)
     */
    public AsyncJsonLoader(Context context, Executor executor) {
        super(executor);
        mContext = context.getApplicationContext();
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncJsonLoader(Context, Executor)
     */
    public AsyncJsonLoader(Context context, Executor executor, Object owner) {
        super(executor, owner);
        mContext = context.getApplicationContext();
    }

    /**
     * Matches the scheme of the specified <em>uri</em>. The default implementation
     * match the "http", "https" and "ftp".
     * @param uri The uri to match.
     * @return <tt>true</tt> if the scheme match successful, <tt>false</tt> otherwise.
     */
    protected boolean matchScheme(String uri) {
        return UriUtils.matchScheme(uri);
    }

    /**
     * Returns a URL string by parsing <tt>uri</tt>. The default
     * implementation returns the <em>uri</em>.
     * @param uri The uri, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     */
    protected String parseUrl(String uri, LoadParams params) {
        return uri;
    }

    /**
     * Tests if the <em>result</em> is valid. The default implementation returns <tt>true</tt>.
     * @param params The parameters, passed earlier by {@link #load}.
     * @param result The JSON value. May be a <tt>JSONObject</tt> or <tt>JSONArray</tt>.
     * @return <tt>true</tt> if the <em>result</em> is valid, <tt>false</tt> otherwise.
     */
    protected boolean validateResult(LoadParams params, Result result) {
        return true;
    }

    @Override
    protected Result loadInBackground(Task<?, ?> task, String uri, LoadParams[] args) {
        try {
            final LoadParams params = args[0];
            if (!matchScheme(uri)) {
                return loadFromUri(task, uri, params);
            }

            Result result = null;
            final String cacheFile = params.getCacheFile(uri);
            if (TextUtils.isEmpty(cacheFile)) {
                result = params.newDownloadRequest(parseUrl(uri, params)).download(task, null);
            } else {
                loadFromCache(task, cacheFile, params);
                if (!isTaskCancelled(task)) {
                    result = download(task, uri, cacheFile, params);
                }
            }

            return result;
        } catch (Exception e) {
            Log.e(getClass().getName(), new StringBuilder("Couldn't load JSON from - '").append(uri).append("'\n").append(e).toString());
            return null;
        }
    }

    private void loadFromCache(Task<?, ?> task, String cacheFile, LoadParams params) {
        Result result = null;
        try {
            result = loadFromUri(task, cacheFile, params);
        } catch (Exception e) {
            // Ignored.
        }

        params.mHitCache = (result != null);
        task.setProgress(result);
    }

    private Result loadFromUri(Task<?, ?> task, String uri, LoadParams params) throws Exception {
        final JsonReader reader = new JsonReader(new InputStreamReader(UriUtils.openInputStream(mContext, uri)));
        try {
            final Result result = JSONUtils.newInstance(reader, task);
            return (validateResult(params, result) ? result : null);
        } finally {
            reader.close();
        }
    }

    private Result parseResult(Task<?, ?> task, String cacheFile, String tempFile, LoadParams params) throws Exception {
        if (!params.mHitCache) {
            return loadFromUri(task, tempFile, params);
        }

        Result result = null;
        final byte[] digest1 = MessageDigests.computeFile(tempFile, Algorithm.SHA1);
        final byte[] digest2 = MessageDigests.computeFile(cacheFile, Algorithm.SHA1);
        if (Arrays.equals(digest1, digest2)) {
            task.cancel(false);
        } else {
            result = loadFromUri(task, tempFile, params);
        }

        return result;
    }

    private Result download(Task<?, ?> task, String uri, String cacheFile, LoadParams params) throws Exception {
        final String tempFile = new StringBuilder(128).append(cacheFile, 0, cacheFile.lastIndexOf('/') + 1).append(Thread.currentThread().hashCode()).toString();
        Result result = null;
        try {
            final int statusCode = params.newDownloadRequest(parseUrl(uri, params)).download(tempFile, task, null);
            if (statusCode == HttpURLConnection.HTTP_OK && !isTaskCancelled(task)) {
                //result = loadFromUri(task, tempFile, params);
                result = parseResult(task, cacheFile, tempFile, params);
                if (result != null && !isTaskCancelled(task)) {
                    FileUtils.moveFile(tempFile, cacheFile);
                }
            }
        } finally {
            FileUtils.deleteFiles(tempFile, false);
        }

        return result;
    }

    /**
     * Class <tt>LoadParams</tt> used to {@link AsyncJsonLoader} to load JSON value.
     */
    public static class LoadParams {
        /* package */ volatile boolean mHitCache;

        /**
         * Tests if the JSON cache file parse successful.
         * @return <tt>true</tt> if parse successful, <tt>false</tt> otherwise.
         */
        public final boolean hitCache() {
            return mHitCache;
        }

        /**
         * Returns the absolute path of the JSON cache file on the filesystem.
         * @param uri The uri to build the JSON cache file.
         * @return The path of the JSON cache file, or <tt>null</tt> if no cache file.
         */
        public String getCacheFile(String uri) {
            return null;
        }

        /**
         * Returns a new download request with the specified <em>url</em>.
         * @param url The url to load the JSON value.
         * @return The instance of {@link DownloadRequest}.
         * @throws IOException if an error occurs while opening the connection.
         */
        public DownloadRequest newDownloadRequest(String url) throws IOException {
            return new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
        }
    }
}
