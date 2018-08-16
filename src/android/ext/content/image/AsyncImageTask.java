package android.ext.content.image;

import static android.ext.content.image.ImageLoader.SCHEME_FTP;
import static android.ext.content.image.ImageLoader.SCHEME_HTTP;
import static android.ext.content.image.ImageLoader.SCHEME_HTTPS;
import static java.net.HttpURLConnection.HTTP_OK;
import java.io.IOException;
import java.lang.ref.WeakReference;
import android.content.ContentResolver;
import android.content.Context;
import android.ext.content.XmlResources;
import android.ext.content.image.BitmapDecoder.Parameters;
import android.ext.net.DownloadRequest;
import android.ext.util.ArrayUtils;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Class <tt>AsyncImageTask</tt> allows to load an image from the specified URI on a background thread
 * and publish result on the UI thread.
 * <h5>AsyncImageTask's generic types</h5>
 * <p>The two types used by an image task are the following:</p>
 * <ol><li><tt>URI</tt>, The URI type of the task, accepts the following URI schemes:</li>
 * <ul><li>path (no scheme)</li>
 * <li>ftp ({@link #SCHEME_FTP})</li>
 * <li>http ({@link #SCHEME_HTTP})</li>
 * <li>https ({@link #SCHEME_HTTPS})</li>
 * <li>file ({@link ContentResolver#SCHEME_FILE SCHEME_FILE})</li>
 * <li>content ({@link ContentResolver#SCHEME_CONTENT SCHEME_CONTENT})</li>
 * <li>android.resource ({@link ContentResolver#SCHEME_ANDROID_RESOURCE SCHEME_ANDROID_RESOURCE})</li></ul>
 * <li><tt>Image</tt>, The image type of the load result.</li></ol>
 * <h2>Usage</h2>
 * <p>Here is an example:</p><pre>
 * public final class DownloadBitmapTask extends AsyncImageTask&lt;String, Bitmap&gt; {
 *     public DownloadBitmapTask(Context context) {
 *         super(context);
 *     }
 *
 *     protected void onPostExecute(Bitmap result) {
 *         if (result != null) {
 *             Log.i(TAG, result.toString());
 *         }
 *     }
 * }
 *
 * new DownloadBitmapTask(context)
 *    .setParameters(R.xml.params)
 *    .execute(url);</pre>
 * @author Garfield
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class AsyncImageTask<URI, Image> extends AsyncTask<URI, Object, Image> implements Cancelable {
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
    public final <T> T getOwner() {
        return (mOwner != null ? (T)mOwner.get() : null);
    }

    /**
     * Sets the object that owns this task.
     * @param owner The owner object.
     * @return This task.
     * @see #getOwner()
     */
    public final AsyncImageTask<URI, Image> setOwner(Object owner) {
        mOwner = new WeakReference<Object>(owner);
        return this;
    }

    /**
     * Sets the {@link Parameters} used to decode the image.
     * @param id The xml resource id of the <tt>Parameters</tt>.
     * @return This task.
     * @see #setParameters(Parameters)
     */
    public final AsyncImageTask<URI, Image> setParameters(int id) {
        mParameters = XmlResources.loadParameters(mContext, id);
        return this;
    }

    /**
     * Sets the {@link Parameters} used to decode the image.
     * @param parameters The <tt>Parameters</tt>.
     * @return This task.
     * @see #setParameters(int)
     */
    public final AsyncImageTask<URI, Image> setParameters(Parameters parameters) {
        mParameters = parameters;
        return this;
    }

    @Override
    protected Image doInBackground(URI... params) {
        DebugUtils.__checkError(ArrayUtils.getSize(params) <= 0, "Invalid parameter - The params is null or 0-length");
        final byte[] tempBuffer = new byte[16384];
        final URI uri = params[0];
        if (!matchScheme(uri)) {
            return decodeImage(uri, tempBuffer);
        }

        final String imageDir  = FileUtils.getCacheDir(mContext, ".temp_image_cache").getPath();
        final String imageFile = new StringBuilder(imageDir.length() + 16).append(imageDir).append('/').append(Thread.currentThread().hashCode()).toString();
        try {
            final int statusCode = createDownloadRequest(uri).download(imageFile, this, tempBuffer);
            return (statusCode == HTTP_OK && !isCancelled() ? decodeImage(imageFile, tempBuffer) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), new StringBuilder("Couldn't load image data from - '").append(uri).append("'\n").append(e).toString());
            return null;
        } finally {
            FileUtils.deleteFiles(imageFile, false);
        }
    }

    /**
     * Returns a new download request with the specified <em>url</em>. Subclasses should
     * override this method to create the download request. The default implementation
     * returns a new {@link DownloadRequest} object.
     * @param uri The uri to create.
     * @return The <tt>DownloadRequest</tt> object.
     * @throws IOException if an error occurs while creating the download request.
     */
    protected DownloadRequest createDownloadRequest(URI uri) throws IOException {
        return new DownloadRequest(uri.toString()).readTimeout(60000).connectTimeout(60000);
    }

    /**
     * Decodes an image from the specified <em>uri</em>.
     * @param uri The uri to decode.
     * @param tempBuffer May be <tt>null</tt>. The temporary storage to use for decoding. Suggest 16K.
     * @return The image object, or <tt>null</tt> if the image data cannot be decode.
     */
    protected Image decodeImage(Object uri, byte[] tempBuffer) {
        return (Image)new BitmapDecoder(mContext, mParameters != null ? mParameters : Parameters.defaultParameters(), 1).decodeImage(uri, null, 0, tempBuffer);
    }

    /**
     * Matches the scheme of the specified <em>uri</em>. The default implementation match
     * the {@link #SCHEME_HTTP}, {@link #SCHEME_HTTPS} and {@link #SCHEME_FTP}.
     * @param uri The uri to match.
     * @return <tt>true</tt> if the scheme match successful, <tt>false</tt> otherwise.
     */
    protected boolean matchScheme(URI uri) {
        final String scheme = (uri instanceof Uri ? ((Uri)uri).getScheme() : uri.toString());
        return (SCHEME_HTTP.regionMatches(true, 0, scheme, 0, 4) || SCHEME_HTTPS.regionMatches(true, 0, scheme, 0, 5) || SCHEME_FTP.regionMatches(true, 0, scheme, 0, 3));
    }
}
