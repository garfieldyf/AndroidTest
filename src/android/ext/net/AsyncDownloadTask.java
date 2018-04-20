package android.ext.net;

import static java.net.HttpURLConnection.HTTP_OK;
import java.lang.ref.WeakReference;
import java.net.URLConnection;
import org.json.JSONArray;
import org.json.JSONObject;
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
     */
    public final <T extends DownloadRequest> T newDownloadRequest(String url, Class<T> clazz) {
        try {
            DebugUtils.__checkError(mRequest != null, "The DownloadRequest is already exists. Only one DownloadRequest may be created per " + getClass().getName());
            return (T)(mRequest = clazz.getConstructor(String.class).newInstance(url));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Result doInBackground(Params... params) {
        DebugUtils.__checkError(mRequest == null, "The " + getClass().getName() + " did not call newDownloadRequest()");
        try {
            return (mRequest.connect(null) == HTTP_OK ? onDownload(mRequest.connection, params) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't download from - " + mRequest.connection.getURL().toString(), e);
            return null;
        } finally {
            mRequest.disconnect();
        }
    }

    /**
     * Override this method to downloads the resource from the remote server on a background thread.
     * <p>The default implementation returns a {@link JSONObject} or {@link JSONArray} object.</p>
     * @param conn The {@link URLConnection} whose connecting the remote server.
     * @param params The parameters of this task, passed earlier by {@link #execute(Params[])}.
     * @return A result, defined by the subclass of this task.
     * @throws Exception if an error occurs while downloading the resource.
     */
    protected Result onDownload(URLConnection conn, Params[] params) throws Exception {
        return mRequest.downloadImpl(this);
    }
}
