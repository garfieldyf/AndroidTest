package android.ext.net;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import android.ext.content.AsyncTask;
import android.ext.net.DownloadRequest.DownloadCallback;
import android.ext.util.ByteArrayBuffer;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;

/**
 * Class <tt>AsyncDownloadTask</tt> allows to download the resource from the remote server on a
 * background thread and publish results on the UI thread.
 * <h3>Usage</h3>
 * <p>Here is an example of subclassing:</p><pre>
 * private static class JSONDownloadTask extends AsyncDownloadTask&lt;String, Object, JSONObject&gt; {
 *    {@code @Override}
 *     protected DownloadRequest newDownloadRequest(String[] urls) throws Exception {
 *         return new DownloadRequest(urls[0]).readTimeout(30000).connectTimeout(30000);
 *     }
 *
 *    {@code @Override}
 *     protected void onPostExecute(JSONObject result) {
 *         if (result != null) {
 *             Log.i(TAG, result.toString());
 *         }
 *     }
 * }
 *
 * new JSONDownloadTask()
 *    .setOwner(activity)   // May be an <tt>Activity, LifecycleOwner, Lifecycle</tt> or <tt>Fragment</tt> etc.
 *    .execute(executor, url);</pre>
 * @author Garfield
 */
public abstract class AsyncDownloadTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements DownloadCallback<Params, Result> {
    private DownloadRequest mRequest;

    /**
     * Constructor
     * @see #AsyncDownloadTask(Object)
     */
    public AsyncDownloadTask() {
    }

    /**
     * Constructor
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncDownloadTask()
     */
    public AsyncDownloadTask(Object owner) {
        super(owner);
    }

    @Override
    protected Result doInBackground(Params[] params) {
        try {
            mRequest = newDownloadRequest(params);
            return mRequest.download(this, params);
        } catch (Exception e) {
            Log.e(getClass().getName(), Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * Override this method to downloads the resource from the remote server on a background thread.
     * <p>The default implementation downloads the JSON data from the remote server and returns a
     * <tt>JSONObject</tt> or <tt>JSONArray</tt> object.</p>
     * @param conn The {@link URLConnection} whose connecting the remote server.
     * @param statusCode The response code returned by the remote server.
     * @param params The parameters of this task, passed earlier by {@link #execute(Params[])}.
     * @return A result, defined by the subclass of this task.
     * @throws Exception if an error occurs while downloading the resource.
     * @see #download()
     * @see #download(File, int)
     * @see #download(OutputStream)
     */
    @Override
    public Result onDownload(URLConnection conn, int statusCode, Params[] params) throws Exception {
        return (statusCode == HTTP_OK ? mRequest.<Result>downloadImpl(this) : null);
    }

    /**
     * Returns a new download request with the specified <em>params</em>.
     * @param params The parameters of this task, passed earlier by {@link #execute(Params[])}.
     * @return The instance of {@link DownloadRequest}.
     * @throws Exception if an error occurs while opening the connection.
     */
    protected abstract DownloadRequest newDownloadRequest(Params[] params) throws Exception;

    /**
     * Downloads the resource from the remote server write to the {@link ByteArrayBuffer}.
     * @return The <tt>ByteArrayBuffer</tt> to store the resource.
     * @throws IOException if an error occurs while downloading to the resource.
     * @see #onDownload(URLConnection, int, Params[])
     */
    protected final ByteArrayBuffer download() throws IOException {
        final ByteArrayBuffer result = new ByteArrayBuffer();
        mRequest.downloadImpl(result, this, null);
        return result;
    }

    /**
     * Downloads the resource from the remote server write to the specified <em>out</em>.
     * @param out The {@link OutputStream} to write the resource.
     * @throws IOException if an error occurs while downloading to the resource.
     * @see #onDownload(URLConnection, int, Params[])
     */
    protected final void download(OutputStream out) throws IOException {
        mRequest.downloadImpl(out, this, null);
    }

    /**
     * Downloads the resource from the remote server write to the specified file.
     * <p>Note: This method will be create the necessary directories.</p>
     * @param file The file to write the resource, must be absolute file path.
     * @param statusCode The response code returned by the remote server.
     * @throws IOException if an error occurs while downloading to the resource.
     * @see #onDownload(URLConnection, int, Params[])
     */
    protected final void download(File file, int statusCode) throws IOException {
        switch (statusCode) {
        case HTTP_OK:
            mRequest.downloadImpl(file, this, null, false);
            break;

        case HTTP_PARTIAL:
            mRequest.downloadImpl(file, this, null, true);
            break;
        }
    }
}
