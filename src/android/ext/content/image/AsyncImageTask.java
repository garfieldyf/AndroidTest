package android.ext.content.image;

import static android.ext.content.image.ImageLoader.SCHEME_FTP;
import static android.ext.content.image.ImageLoader.SCHEME_HTTP;
import static android.ext.content.image.ImageLoader.SCHEME_HTTPS;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import android.content.Context;
import android.ext.content.image.BitmapDecoder.Parameters;
import android.ext.content.image.BitmapDecoder.SizeParameters;
import android.ext.net.AsyncDownloadTask;
import android.ext.net.DownloadRequest;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.util.Log;

public class AsyncImageTask<URI> extends AsyncDownloadTask<URI, Object, Bitmap> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * The {@link Parameters} to decode bitmap.
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

    public final AsyncImageTask<URI> setParameters(int id) {
        mParameters = id;
        return this;
    }

    public final AsyncImageTask<URI> setParameters(Parameters parameters) {
        mParameters = parameters;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Bitmap doInBackground(URI... params) {
        DebugUtils.__checkError(ArrayUtils.getSize(params) <= 0, "Invalid parameter - The params is null or 0-length");
        final URI uri = params[0];
        if (matchScheme(uri)) {
            newDownloadRequest(uri.toString(), DownloadRequest.class).readTimeout(60000).connectTimeout(60000);
            return super.doInBackground(params);
        } else {
            return decodeBitmap(uri, ImageModule.loadParameters(mContext, mParameters), (byte[])null);
        }
    }

    @Override
    protected Bitmap onDownload(URLConnection conn, int statusCode, URI[] uris) throws Exception {
        if (isCancelled() || statusCode != HttpURLConnection.HTTP_OK) {
            return null;
        }

        final String imageFile = FileUtils.buildPath(FileUtils.getCacheDir(mContext, ".async_image_cache").getPath(), Integer.toString(Thread.currentThread().hashCode()));
        final byte[] buffer = new byte[16384];
        try {
            download(imageFile, statusCode, buffer);
            return (isCancelled() ? null : decodeBitmap(imageFile, ImageModule.loadParameters(mContext, mParameters), buffer));
        } finally {
            FileUtils.deleteFiles(imageFile, false);
        }
    }

    protected Bitmap decodeBitmap(Object uri, Parameters parameters, byte[] tempStorage) {
        try {
            final Options opts = new Options();
            opts.inTempStorage = tempStorage;
            opts.inPreferredConfig = parameters.config;

            // Decodes the image bounds, if needed.
            if (mParameters instanceof SizeParameters) {
                opts.inJustDecodeBounds = true;
                decodeBitmap(uri, opts);
                opts.inJustDecodeBounds = false;
            }

            // Decodes the bitmap pixels.
            parameters.computeSampleSize(mContext, uri, opts);
            return decodeBitmap(uri, opts);
        } catch (Exception e) {
            Log.e(getClass().getName(), new StringBuilder("Couldn't decode image from - '").append(uri).append("'\n").append(e).toString());
            return null;
        }
    }

    protected Bitmap decodeBitmap(Object uri, Options opts) throws Exception {
        final InputStream is = ImageDecoder.openInputStream(mContext, uri);
        try {
            return BitmapFactory.decodeStream(is, null, opts);
        } finally {
            is.close();
        }
    }

    protected boolean matchScheme(URI uri) {
        final String scheme = (uri instanceof Uri ? ((Uri)uri).getScheme() : uri.toString());
        return (SCHEME_HTTP.regionMatches(true, 0, scheme, 0, 4) || SCHEME_HTTPS.regionMatches(true, 0, scheme, 0, 5) || SCHEME_FTP.regionMatches(true, 0, scheme, 0, 3));
    }
}
