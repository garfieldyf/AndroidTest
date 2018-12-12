package android.ext.content;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.concurrent.Executor;
import android.content.Context;
import android.ext.content.AsyncJsonLoader.LoadParams;
import android.ext.net.DownloadRequest;
import android.ext.util.FileUtils;
import android.ext.util.JSONUtils;
import android.ext.util.UriUtils;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

/**
 * Class <tt>AsyncJsonLoader</tt> allows to load the JSON value on a background thread and publish results
 * on the UI thread.
 * <h5>AsyncJsonLoader's generic types</h5>
 * <p>The two types used by a JSON loader are the following:</p>
 * <ol><li><tt>URI</tt>, The URI type of the load task, accepts the following URI schemes:</li>
 * <ul><li>path (no scheme)</li>
 * <li>ftp ({@link #SCHEME_FTP})</li>
 * <li>http ({@link #SCHEME_HTTP})</li>
 * <li>https ({@link #SCHEME_HTTPS})</li>
 * <li>file ({@link #SCHEME_FILE})</li>
 * <li>content ({@link #SCHEME_CONTENT})</li>
 * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
 * <li><tt>Result</tt>, The load result type, must be <tt>JSONObject</tt> or <tt>JSONArray</tt>.</li></ol>
 * @author Garfield
 */
public class AsyncJsonLoader<URI, Result> extends AsyncTaskLoader<URI, LoadParams, Result> {
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
    protected boolean matchScheme(URI uri) {
        return UriUtils.matchScheme(uri);
    }

    /**
     * Returns a URL string by parsing <tt>uri</tt>. The default
     * implementation returns <em>uri.toString()</em>.
     * @param uri The uri, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     */
    protected String parseUrl(URI uri, LoadParams[] params) {
        return uri.toString();
    }

    @Override
    protected Result loadInBackground(Task<?, ?> task, URI uri, LoadParams[] params) {
        try {
            if (!matchScheme(uri)) {
                return loadFromUri(task, uri);
            }

            final String url = parseUrl(uri, params);
            final LoadParams param = params[0];
            final String cacheFile = param.getCacheFile(url);
            return (TextUtils.isEmpty(cacheFile) ? param.newDownloadRequest(url).<Result>download(task, null) : download(task, url, cacheFile, param));
        } catch (Exception e) {
            Log.e(getClass().getName(), new StringBuilder("Couldn't load JSON from - '").append(uri).append("'\n").append(e).toString());
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onProgressUpdate(URI uri, LoadParams[] params, Object[] values) {
        onLoadComplete(uri, params, (Result)values[0]);
    }

    private void loadFromCache(Task<?, ?> task, String cacheFile) {
        try {
            task.setProgress(loadFromUri(task, cacheFile));
        } catch (Exception e) {
            // Ignored.
        }
    }

    private Result loadFromUri(Task<?, ?> task, Object uri) throws Exception {
        final JsonReader reader = new JsonReader(new InputStreamReader(UriUtils.openInputStream(mContext, uri)));
        try {
            return JSONUtils.newInstance(reader, task);
        } finally {
            reader.close();
        }
    }

    private Result download(Task<?, ?> task, String url, String cacheFile, LoadParams params) throws Exception {
        final String tempFile = new StringBuilder(128).append(cacheFile, 0, cacheFile.lastIndexOf('/') + 1).append(Thread.currentThread().hashCode()).toString();
        Result result = null;
        try {
            loadFromCache(task, cacheFile);
            final int statusCode = params.newDownloadRequest(url).download(tempFile, task, null);
            if (statusCode == HttpURLConnection.HTTP_OK && !isTaskCancelled(task)) {
                result = loadFromUri(task, tempFile);
                FileUtils.moveFile(tempFile, cacheFile);
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
        /**
         * Returns the absolute path of the JSON cache file on the filesystem.
         * @param url The url to load the JSON value.
         * @return The path of the JSON cache file, or <tt>null</tt> if no cache file.
         */
        public String getCacheFile(String url) {
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
