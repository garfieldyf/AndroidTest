package android.ext.content.image;

import static android.ext.content.image.ImageLoader.SCHEME_FTP;
import static android.ext.content.image.ImageLoader.SCHEME_HTTP;
import static android.ext.content.image.ImageLoader.SCHEME_HTTPS;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import android.content.ContentResolver;
import android.content.Context;
import android.ext.content.image.BitmapDecoder.Parameters;
import android.ext.content.image.ImageLoader.ImageDecoder;
import android.ext.net.AsyncDownloadTask;
import android.ext.net.DownloadRequest;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.net.Uri;

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
public class AsyncImageTask<URI, Image> extends AsyncDownloadTask<URI, Object, Image> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * The parameters to decode bitmap.
     */
    private Object mParameters;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @see #AsyncImageTask(Context, Object)
     */
    public AsyncImageTask(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncImageTask(Context)
     */
    public AsyncImageTask(Context context, Object owner) {
        super(owner);
        mContext = context.getApplicationContext();
    }

    /**
     * Sets the {@link Parameters} used to decode the image.
     * @param id The xml resource id of the <tt>Parameters</tt>.
     * @return This task.
     * @see #setParameters(Parameters)
     */
    public final AsyncImageTask<URI, Image> setParameters(int id) {
        mParameters = id;
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
    @SuppressWarnings("unchecked")
    protected Image doInBackground(URI... params) {
        DebugUtils.__checkError(ArrayUtils.getSize(params) <= 0, "Invalid parameter - The params is null or 0-length");
        final URI uri = params[0];
        if (matchScheme(uri)) {
            createDownloadRequest(uri.toString());
            return super.doInBackground(params);
        } else {
            return createImageDecoder(ImageModule.loadParameters(mContext, mParameters)).decodeImage(uri, null, 0, null);
        }
    }

    @Override
    protected Image onDownload(URLConnection conn, int statusCode, URI[] uris) throws Exception {
        if (isCancelled() || statusCode != HttpURLConnection.HTTP_OK) {
            return null;
        }

        final String imageDir  = FileUtils.getCacheDir(mContext, ".temp_image_cache").getPath();
        final String imageFile = new StringBuilder(imageDir.length() + 16).append(imageDir).append('/').append(Thread.currentThread().hashCode()).toString();
        try {
            final byte[] tempBuffer = new byte[16384];
            download(imageFile, statusCode, tempBuffer);
            return (isCancelled() ? null : createImageDecoder(ImageModule.loadParameters(mContext, mParameters)).decodeImage(imageFile, null, 0, tempBuffer));
        } finally {
            FileUtils.deleteFiles(imageFile, false);
        }
    }

    /**
     * Returns a new {@link ImageDecoder} object. Subclasses should override this method to create
     * the image decoder. The default implementation returns a new {@link BitmapDecoder} object.
     * @param parameters The {@link Parameters} used to decode the image.
     * @return The <tt>ImageDecoder</tt> object.
     */
    @SuppressWarnings("unchecked")
    protected ImageDecoder<Image> createImageDecoder(Parameters parameters) {
        return (ImageDecoder<Image>)new BitmapDecoder(mContext, parameters, 1);
    }

    /**
     * Returns a new {@link DownloadRequest} with the specified <em>url</em>. Subclasses should
     * override this method to create the download request. The default implementation returns
     * a new <tt>DownloadRequest</tt> object.
     * @param url The url to connect the remote server.
     * @return The <tt>DownloadRequest</tt> object.
     * @see AsyncDownloadTask#newDownloadRequest(Object, Class)
     */
    protected DownloadRequest createDownloadRequest(String url) {
        return newDownloadRequest(url, DownloadRequest.class).readTimeout(60000).connectTimeout(60000);
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
