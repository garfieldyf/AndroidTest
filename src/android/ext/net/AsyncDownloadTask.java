package android.ext.net;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Class <tt>AsyncDownloadTask</tt> allows to download the resource from the remote HTTP server on a background
 * thread and publish results on the UI thread. This class both support HTTP "GET" and "POST" methods.
 * <h2>Usage</h2>
 * <p>Here is an example:</p>
 * <pre>
 * public final class JSONDownloadTask extends AsyncDownloadTask&lt;Object, Object, JSONObject&gt; {
 *     protected void onPostExecute(JSONObject result) {
 *         if (result != null) {
 *             Log.i("JSONDownloadTask", result.toString());
 *         }
 *     }
 * }
 *
 * final JSONDownloadTask task = new JSONDownloadTask()
 *     .newDownloadRequest(url)
 *     .connectTimeout(60000)
 *     .readTimeout(60000)
 *     .requestHeader("Content-Type", "application/json")
 *     .post(obj)
 *     .execute((Object[])null);
 * </pre>
 * @author Garfield
 * @version 1.0
 */
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
    @SuppressWarnings("unchecked")
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
     * Returns a new {@link DownloadRequest} with the specified <em>url</em>.
     * @param url The url to connect the remote HTTP server.
     * @return The instance of <tt>DownloadRequest</tt>.
     */
    public final DownloadRequest newDownloadRequest(String url) {
        try {
            DebugUtils.__checkError(mRequest != null, "The DownloadRequest is already exists. Only one DownloadRequest may be created per " + getClass().getName());
            return (mRequest = new DownloadRequest(this, url));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Result doInBackground(Params... params) {
        DebugUtils.__checkError(mRequest == null, "The " + getClass().getName() + " did not call newDownloadRequest()");
        Result result = null;
        try {
            if (mRequest.connect(null) == HttpURLConnection.HTTP_OK) {
                result = onDownload(mRequest.connection, params);
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't download from - " + mRequest.connection.getURL().toString(), e);
        } finally {
            mRequest.connection.disconnect();
        }

        return result;
    }

    /**
     * Override this method to downloads the resource from the remote HTTP server on a background thread.
     * <p>The default implementation returns a {@link JSONObject} or {@link JSONArray} object.</p>
     * @param conn The {@link HttpURLConnection} whose connecting the remote HTTP server.
     * @param params The parameters of this task, passed earlier by {@link DownloadRequest#execute}.
     * @return A result, defined by the subclass of this task.
     * @throws Exception if an error occurs while downloading the resource.
     */
    protected Result onDownload(HttpURLConnection conn, Params[] params) throws Exception {
        return mRequest.downloadImpl(this);
    }
}
