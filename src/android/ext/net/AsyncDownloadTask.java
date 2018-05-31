package android.ext.net;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import android.ext.util.ByteArrayBuffer;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Class <tt>AsyncDownloadTask</tt> allows to download the resource from the remote server on a
 * background thread and publish results on the UI thread.
 * <h2>Usage</h2>
 * <p>Here is an example:</p><pre>
 * public final class JSONDownloadTask extends AsyncDownloadTask&lt;Object, Object, JSONObject&gt; {
 *     protected void onPostExecute(JSONObject result) {
 *         if (result != null) {
 *             Log.i("JSONDownloadTask", result.toString());
 *         }
 *     }
 * }
 *
 * final JSONDownloadTask task = new JSONDownloadTask();
 * task.newDownloadRequest(url, DownloadPostRequest.class)
 *     .post(obj)
 *     .readTimeout(60000)
 *     .connectTimeout(60000)
 *     .contentType("application/json")
 * task.execute((Object[])null);</pre>
 * @author Garfield
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class AsyncDownloadTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements Cancelable {
    private DownloadRequest mRequest;
    private WeakReference<Object> mOwner;

    /**
     * Constructor
     * @see #AsyncDownloadTask(Object)
     */
    public AsyncDownloadTask() {
        DebugUtils.__checkMemoryLeaks(getClass());
    }

    /**
     * Constructor
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncDownloadTask()
     */
    public AsyncDownloadTask(Object owner) {
        DebugUtils.__checkMemoryLeaks(getClass());
        mOwner = new WeakReference<Object>(owner);
    }

    /**
     * Returns the object that owns this task.
     * @return The owner object or <tt>null</tt> if
     * no owner set or the owner released by the GC.
     * @see #setOwner(Object)
     */
    public final <T> T getOwner() {
        return (mOwner != null ? (T)mOwner.get() : null);
    }

    /**
     * Sets the object that owns this task.
     * @param owner The owner object.
     * @return This task.
     * @see #getOwner()
     */
    public final AsyncDownloadTask<Params, Progress, Result> setOwner(Object owner) {
        mOwner = new WeakReference<Object>(owner);
        return this;
    }

    /**
     * Returns a new download request with the specified <em>url</em>.
     * @param url The url to connect the remote server.
     * @param clazz May be a {@link DownloadRequest} or {@link DownloadPostRequest} <tt>Class</tt>.
     * @return The instance of {@link DownloadRequest} or {@link DownloadPostRequest}.
     * @see #newDownloadRequest(String, Class)
     */
    public final <T extends DownloadRequest> T newDownloadRequest(URL url, Class<T> clazz) {
        return newDownloadRequestImpl(url, clazz);
    }

    /**
     * Equivalent to calling <tt>newDownloadRequest(new URL(url), clazz)</tt>.
     * @param url The url to connect the remote server.
     * @param clazz May be a {@link DownloadRequest} or {@link DownloadPostRequest} <tt>Class</tt>.
     * @return The instance of {@link DownloadRequest} or {@link DownloadPostRequest}.
     * @see #newDownloadRequest(URL, Class)
     */
    public final <T extends DownloadRequest> T newDownloadRequest(String url, Class<T> clazz) {
        return newDownloadRequestImpl(url, clazz);
    }

    @Override
    protected Result doInBackground(Params... params) {
        DebugUtils.__checkError(mRequest == null, "The " + getClass().getName() + " did not call newDownloadRequest()");
        try {
            final int statusCode = mRequest.connectImpl(null);
            return (statusCode == HTTP_OK || statusCode == HTTP_PARTIAL ? onDownload(mRequest.connection, statusCode, params) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't download from - " + mRequest.connection.getURL().toString(), e);
            return null;
        } finally {
            mRequest.disconnect();
        }
    }

    /**
     * Downloads the resource from the remote server write to the {@link ByteArrayBuffer}.
     * @return The {@link ByteArrayBuffer} contains the resource.
     * @throws IOException if an error occurs while downloading to the resource.
     * @see #download(int, String)
     * @see #download(OutputStream)
     */
    protected final ByteArrayBuffer download() throws IOException {
        final InputStream is = mRequest.connection.getInputStream();
        try {
            final ByteArrayBuffer result = new ByteArrayBuffer();
            result.readFrom(is, this);
            return result;
        } finally {
            is.close();
        }
    }

    /**
     * Downloads the resource from the remote server write to the specified <em>out</em>.
     * @param out The {@link OutputStream} to write the resource.
     * @throws IOException if an error occurs while downloading to the resource.
     * @see #download()
     * @see #download(int, String)
     */
    protected final void download(OutputStream out) throws IOException {
        mRequest.downloadImpl(out, this, null);
    }

    /**
     * Downloads the resource from the remote server write to the specified file.
     * <p>Note: This method will be create the necessary directories.</p>
     * @param statusCode The response code returned by the remote server.
     * @param filename The file name to write the resource, must be absolute file path.
     * @throws IOException if an error occurs while downloading to the resource.
     * @see #download()
     * @see #download(OutputStream)
     */
    protected final void download(int statusCode, String filename) throws IOException {
        switch (statusCode) {
        case HTTP_OK:
            mRequest.downloadImpl(filename, this, null, false);
            break;

        case HTTP_PARTIAL:
            mRequest.downloadImpl(filename, this, null, true);
            break;
        }
    }

    /**
     * Override this method to downloads the resource from the remote server on a background thread.
     * <p>The default implementation returns a {@link JSONObject} or {@link JSONArray} object.</p>
     * @param conn The {@link URLConnection} whose connecting the remote server.
     * @param statusCode The response code returned by the remote server.
     * @param params The parameters of this task, passed earlier by {@link #execute(Params[])}.
     * @return A result, defined by the subclass of this task.
     * @throws Exception if an error occurs while downloading the resource.
     * @see #download()
     * @see #download(int, String)
     * @see #download(OutputStream)
     */
    protected Result onDownload(URLConnection conn, int statusCode, Params[] params) throws Exception {
        return mRequest.downloadImpl(this);
    }

    /**
     * Returns a new download request with the specified <em>url</em>.
     */
    private <T extends DownloadRequest> T newDownloadRequestImpl(Object url, Class<T> clazz) {
        try {
            DebugUtils.__checkError(mRequest != null, "The DownloadRequest is already exists. Only one DownloadRequest may be created per " + getClass().getName());
            return (T)(mRequest = clazz.getConstructor(url.getClass()).newInstance(url));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
