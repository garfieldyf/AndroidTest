package android.ext.net;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
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
 * public final class JSONDownloadTask extends AsyncDownloadTask&lt;String, Object, JSONObject&gt; {
 *     public JSONDownloadTask(Activity ownerActivity) {
 *         super(ownerActivity);
 *     }
 *
 *     protected DownloadRequest newDownloadRequest(String[] params) throws Exception {
 *         return new DownloadRequest(params[0]).readTimeout(60000).connectTimeout(60000);
 *     }
 *
 *     protected void onPostExecute(JSONObject result) {
 *         final Activity activity = getOwner();
 *         if (activity == null || activity.isDestroyed()) {
 *              // The owner activity has been destroyed or release by the GC.
 *              return;
 *         }
 *
 *         if (result != null) {
 *             Log.i(TAG, result.toString());
 *         }
 *     }
 * }
 *
 * new JSONDownloadTask(activity).execute(url);</pre>
 * @author Garfield
 */
public abstract class AsyncDownloadTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements Cancelable {
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
    @SuppressWarnings("unchecked")
    public final <T> T getOwner() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        return (T)mOwner.get();
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

    @Override
    @SuppressWarnings("unchecked")
    protected Result doInBackground(Params... params) {
        try {
            mRequest = newDownloadRequest(params);
            return onDownload(mRequest.mConnection, mRequest.connect(null), params);
        } catch (Exception e) {
            Log.e(getClass().getName(), Log.getStackTraceString(e));
            return null;
        } finally {
            if (mRequest != null) {
                mRequest.disconnect();
            }
        }
    }

    /**
     * Downloads the resource from the remote server write to the {@link ByteArrayBuffer}.
     * @return The {@link ByteArrayBuffer} contains the resource.
     * @throws IOException if an error occurs while downloading to the resource.
     * @see #download(String, int, byte[])
     * @see #download(OutputStream, byte[])
     */
    protected final ByteArrayBuffer download() throws IOException {
        final InputStream is = mRequest.mConnection.getInputStream();
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
     * @param tempBuffer May be <tt>null</tt>. The temporary byte array to use for downloading.
     * @throws IOException if an error occurs while downloading to the resource.
     * @see #download()
     * @see #download(String, int, byte[])
     */
    protected final void download(OutputStream out, byte[] tempBuffer) throws IOException {
        mRequest.downloadImpl(out, this, tempBuffer);
    }

    /**
     * Downloads the resource from the remote server write to the specified file.
     * <p>Note: This method will be create the necessary directories.</p>
     * @param filename The file name to write the resource, must be absolute file path.
     * @param statusCode The response code returned by the remote server.
     * @param tempBuffer May be <tt>null</tt>. The temporary byte array to use for downloading.
     * @throws IOException if an error occurs while downloading to the resource.
     * @see #download()
     * @see #download(OutputStream, byte[])
     */
    protected final void download(String filename, int statusCode, byte[] tempBuffer) throws IOException {
        switch (statusCode) {
        case HTTP_OK:
            mRequest.downloadImpl(filename, this, tempBuffer, false);
            break;

        case HTTP_PARTIAL:
            mRequest.downloadImpl(filename, this, tempBuffer, true);
            break;
        }
    }

    /**
     * Returns a new download request with the specified <em>params</em>.
     * @param params The parameters of this task, passed earlier by {@link #execute(Params[])}.
     * @return The instance of {@link DownloadRequest}.
     * @throws Exception if an error occurs while opening the connection.
     */
    protected abstract DownloadRequest newDownloadRequest(Params[] params) throws Exception;

    /**
     * Override this method to downloads the resource from the remote server on a background thread.
     * <p>The default implementation downloads the JSON data from the remote server and returns a
     * {@link JSONObject} or {@link JSONArray} object.</p>
     * @param conn The {@link URLConnection} whose connecting the remote server.
     * @param statusCode The response code returned by the remote server.
     * @param params The parameters of this task, passed earlier by {@link #execute(Params[])}.
     * @return A result, defined by the subclass of this task.
     * @throws Exception if an error occurs while downloading the resource.
     * @see #download()
     * @see #download(String, int, byte[])
     * @see #download(OutputStream, byte[])
     */
    protected Result onDownload(URLConnection conn, int statusCode, Params[] params) throws Exception {
        return (statusCode == HTTP_OK ? mRequest.<Result>downloadImpl(this) : null);
    }
}
