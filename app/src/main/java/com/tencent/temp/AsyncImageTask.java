package com.tencent.temp;

import android.app.Activity;
import android.content.Context;
import android.ext.content.AbsAsyncTask;
import android.ext.content.res.XmlResources;
import android.ext.graphics.BitmapUtils;
import android.ext.image.params.Parameters;
import android.ext.net.DownloadRequest;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.Pools;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.util.Log;
import java.io.File;
import java.net.HttpURLConnection;

/**
 * Class <tt>AsyncImageTask</tt> allows to load the images from the specified
 * URI on a background thread and publish results on the UI thread.
 * <h3>AsyncImageTask's generic types</h3>
 * <p>The one type used by an image task are the following:</p>
 * <tt>URI</tt>, The URI type of the task, accepts the following URI schemes:
 * <ul><li>path (no scheme)</li>
 * <li>ftp ({@link #SCHEME_FTP})</li>
 * <li>http ({@link #SCHEME_HTTP})</li>
 * <li>https ({@link #SCHEME_HTTPS})</li>
 * <li>file ({@link #SCHEME_FILE})</li>
 * <li>content ({@link #SCHEME_CONTENT})</li>
 * <li>android.asset ({@link #SCHEME_ANDROID_ASSET})</li>
 * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
 * <h3>Usage</h3>
 * <p>Here is an example of subclassing:</p><pre>
 * private static final class ImageTask extends AsyncImageTask&lt;String&gt; {
 *     public ImageTask(Activity ownerActivity) {
 *         super(ownerActivity);
 *     }
 *
 *     {@code @Override}
 *     protected void onPostExecute(Object[] results) {
 *         final Activity activity = getOwnerActivity();
 *         if (activity == null) {
 *             // The owner activity has been destroyed or release by the GC.
 *             return;
 *         }
 *
 *         if (results[0] != null) {
 *             // Loading succeeded, update UI.
 *         } else {
 *             // Loading failed, show error or empty UI.
 *         }
 *     }
 * }
 *
 * new ImageTask(ownerActivity)
 *    .setParameters(R.xml.params)
 *    .execute(url);</pre>
 * @author Garfield
 */
public abstract class AsyncImageTask<URI> extends AbsAsyncTask<URI, Object, Object[]> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * The {@link Parameters} to decode bitmap.
     */
    protected Parameters mParameters;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @see #AsyncImageTask(Activity)
     */
    public AsyncImageTask(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Constructor
     * @param ownerActivity The owner <tt>Activity</tt>.
     * @see #AsyncImageTask(Context)
     */
    public AsyncImageTask(Activity ownerActivity) {
        super(ownerActivity);
        mContext = ownerActivity.getApplicationContext();
    }

    /**
     * Sets the {@link Parameters} used to decode the image.
     * @param id The xml resource id of the <tt>Parameters</tt>.
     * @return This task.
     * @see #setParameters(Parameters)
     */
    public final AsyncImageTask<URI> setParameters(int id) {
        mParameters = XmlResources.load(mContext, id);
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
        DebugUtils.__checkError(ArrayUtils.getSize(params) == 0, "Invalid parameter - The params is null or 0-length");
        final byte[] tempBuffer = Pools.obtainByteArray();
        final Object[] results  = new Object[params.length];
        try {
            for (int i = 0; i < params.length && !isCancelled(); ++i) {
                results[i] = decodeImage(params[i], tempBuffer, null);
            }
        } finally {
            Pools.recycleByteArray(tempBuffer);
        }

        return results;
    }

    /**
     * Decodes an image from the specified <em>uri</em>.
     * @param uri The uri to decode.
     * @param tempBuffer The temporary storage to use for decoding.
     * @param reserved Reserved.
     * @return The image object, or <tt>null</tt> if the image data cannot be decode.
     * @see #decodeImage(Object, byte[])
     */
    protected Object decodeImage(URI uri, byte[] tempBuffer, Object reserved) {
        return (matchScheme(uri) ? downloadImage(uri.toString(), tempBuffer) : decodeImage(uri, tempBuffer));
    }

    /**
     * Decodes an image from the specified <em>uri</em>. Subclasses should override this
     * method to decode image. The default implementation returns a new {@link Bitmap} object.
     * @param uri The uri to decode.
     * @param tempBuffer The temporary storage to use for decoding.
     * @return The image object, or <tt>null</tt> if the image data cannot be decode.
     * @see #decodeImage(URI, byte[], Object)
     */
    protected Object decodeImage(Object uri, byte[] tempBuffer) {
        try {
            final Options opts = new Options();
            opts.inTempStorage = tempBuffer;

            if (mParameters != null) {
                // Decodes the bitmap bounds.
                opts.inJustDecodeBounds = true;
                //BitmapUtils.decodeBitmap(mContext, uri, opts);
                opts.inJustDecodeBounds = false;

                // Computes the sample size.
                opts.inMutable = true;
                opts.inPreferredConfig = mParameters.config;
                mParameters.computeSampleSize(null, opts);
            }

            // Decodes the bitmap pixels.
            return null;//BitmapUtils.decodeBitmap(mContext, uri, opts);
        } catch (Exception e) {
            Log.e(BitmapUtils.class.getName(), "Couldn't decode image from - " + uri + "\n" + e);
            return null;
        }
    }

    /**
     * Downloads an image from the specified <em>url</em>
     * @param url The url to download.
     * @param tempBuffer The temporary storage to use for downloading.
     * @return The image object, or <tt>null</tt> if the image data cannot be decode.
     */
    protected Object downloadImage(String url, byte[] tempBuffer) {
        final File imageFile = new File(FileUtils.getCacheDir(mContext, null), Integer.toString(Thread.currentThread().hashCode()));
        try {
            final int statusCode = new DownloadRequest(url).connectTimeout(30000).readTimeout(30000).download(imageFile, this, tempBuffer);
            return (statusCode == HttpURLConnection.HTTP_OK && !isCancelled() ? decodeImage(imageFile, tempBuffer) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load image data from - " + url + "\n" + e);
            return null;
        } finally {
            imageFile.delete();
        }
    }

    private static boolean matchScheme(Object uri) {
        DebugUtils.__checkError(uri == null, "uri == null");
        final String uriString = uri.toString();
        return ("http://".regionMatches(true, 0, uriString, 0, 7) || "https://".regionMatches(true, 0, uriString, 0, 8) || "ftp://".regionMatches(true, 0, uriString, 0, 6));
    }
}
