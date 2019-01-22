package android.ext.content.image;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import android.content.Context;
import android.ext.content.XmlResources;
import android.ext.content.image.params.Parameters;
import android.ext.graphics.BitmapUtils;
import android.ext.net.DownloadRequest;
import android.ext.util.ArrayUtils;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.UriUtils;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Class <tt>AsyncImageTask</tt> allows to load an image from the specified URI
 * on a background thread and publish result on the UI thread.
 * <h5>AsyncImageTask's generic types</h5>
 * <p>The one type used by an image task are the following:</p>
 * <tt>URI</tt>, The URI type of the task, accepts the following URI schemes:
 * <ul><li>path (no scheme)</li>
 * <li>ftp ({@link #SCHEME_FTP})</li>
 * <li>http ({@link #SCHEME_HTTP})</li>
 * <li>https ({@link #SCHEME_HTTPS})</li>
 * <li>file ({@link #SCHEME_FILE})</li>
 * <li>content ({@link #SCHEME_CONTENT})</li>
 * <li>android_asset ({@link #SCHEME_FILE})</li>
 * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
 * <h2>Usage</h2>
 * <p>Here is an example:</p><pre>
 * public final class DownloadBitmapTask extends AsyncImageTask&lt;String&gt; {
 *     public DownloadBitmapTask(Activity ownerActivity) {
 *         super(ownerActivity, ownerActivity);
 *     }
 *
 *     protected void onPostExecute(Object[] results) {
 *         final Activity activity = getOwner();
 *         if (activity == null || activity.isDestroyed()) {
 *              // The owner activity has been destroyed or release by the GC.
 *              return;
 *         }
 *
 *         final Bitmap bitmap = (Bitmap)results[0];
 *         if (bitmap != null) {
 *             Log.i(TAG, bitmap.toString());
 *         }
 *     }
 * }
 *
 * new DownloadBitmapTask(activity)
 *    .setParameters(R.xml.params)
 *    .execute(url);</pre>
 * @author Garfield
 */
public class AsyncImageTask<URI> extends AsyncTask<URI, Object, Object[]> implements Cancelable {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * The {@link Parameters} to decode bitmap.
     */
    protected Parameters mParameters;

    /**
     * The owner object.
     */
    private WeakReference<Object> mOwner;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @see #AsyncImageTask(Context, Object)
     */
    public AsyncImageTask(Context context) {
        DebugUtils.__checkMemoryLeaks(getClass());
        mContext = context.getApplicationContext();
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncImageTask(Context)
     */
    public AsyncImageTask(Context context, Object owner) {
        DebugUtils.__checkMemoryLeaks(getClass());
        mOwner = new WeakReference<Object>(owner);
        mContext = context.getApplicationContext();
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
    public final AsyncImageTask<URI> setOwner(Object owner) {
        mOwner = new WeakReference<Object>(owner);
        return this;
    }

    /**
     * Sets the {@link Parameters} used to decode the image.
     * @param id The xml resource id of the <tt>Parameters</tt>.
     * @return This task.
     * @see #setParameters(Parameters)
     */
    public final AsyncImageTask<URI> setParameters(int id) {
        mParameters = XmlResources.loadParameters(mContext, id);
        return this;
    }

    /**
     * Sets the {@link Parameters} used to decode the image.
     * @param parameters The <tt>Parameters</tt>.
     * @return This task.
     * @see #setParameters(int)
     */
    public final AsyncImageTask<URI> setParameters(Parameters parameters) {
        mParameters = parameters;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object[] doInBackground(URI... params) {
        DebugUtils.__checkError(ArrayUtils.getSize(params) <= 0, "Invalid parameter - The params is null or 0-length");
        final byte[] tempBuffer = new byte[16384];
        final Object[] results  = new Object[params.length];
        for (int i = 0; i < params.length && !isCancelled(); ++i) {
            results[i] = decodeImageInternal(params[i], tempBuffer);
        }

        return results;
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
     * Returns a new download request with the specified <em>url</em>. Subclasses should
     * override this method to create the download request. The default implementation
     * returns a new {@link DownloadRequest} object.
     * @param uri The uri to create.
     * @return The <tt>DownloadRequest</tt> object.
     * @throws IOException if an error occurs while creating the download request.
     */
    protected DownloadRequest newDownloadRequest(URI uri) throws IOException {
        return new DownloadRequest(uri.toString()).connectTimeout(30000).readTimeout(30000);
    }

    /**
     * Decodes an image from the specified <em>uri</em>. Subclasses should override this method
     * to decode image. The default implementation returns a new {@link Bitmap} object.
     * @param uri The uri to decode.
     * @param tempBuffer May be <tt>null</tt>. The temporary storage to use for decoding. Suggest 16K.
     * @return The image object, or <tt>null</tt> if the image data cannot be decode.
     */
    protected Object decodeImage(Object uri, byte[] tempBuffer) {
        return BitmapUtils.decodeBitmap(mContext, uri, mParameters, tempBuffer);
    }

    /**
     * Decodes an image from the specified <em>uri</em>.
     */
    private Object decodeImageInternal(URI uri, byte[] tempBuffer) {
        if (!matchScheme(uri)) {
            return decodeImage(uri, tempBuffer);
        }

        final String imageFile = FileUtils.getCacheDir(mContext, ".temp_image_cache").getPath() + "/" + Thread.currentThread().hashCode();
        try {
            final int statusCode = newDownloadRequest(uri).download(imageFile, this, tempBuffer);
            return (statusCode == HttpURLConnection.HTTP_OK && !isCancelled() ? decodeImage(imageFile, tempBuffer) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), new StringBuilder("Couldn't load image data from - '").append(uri).append("'\n").append(e).toString());
            return null;
        } finally {
            FileUtils.deleteFiles(imageFile, false);
        }
    }
}
