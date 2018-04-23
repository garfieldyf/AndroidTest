package android.ext.content.image;

import static java.net.HttpURLConnection.HTTP_OK;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import android.content.ContentResolver;
import android.content.Context;
import android.ext.content.AsyncLoader;
import android.ext.content.image.BitmapDecoder.Parameters;
import android.ext.net.DownloadRequest;
import android.ext.util.Caches.Cache;
import android.ext.util.Caches.FileCache;
import android.ext.util.FileUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.Pools;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.ext.util.StringUtils;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.util.Log;
import android.util.Printer;

/**
 * Class <tt>ImageLoader</tt> allows to load the image from the URI
 * on a background thread and bind it to target on the UI thread.
 * @author Garfield
 * @version 6.0
 */
public class ImageLoader<URI, Params, Image> extends AsyncLoader<URI, Params, Image> {
    public static final String SCHEME_FTP   = "ftp";
    public static final String SCHEME_HTTP  = "http";
    public static final String SCHEME_HTTPS = "https";

    private final Pool<byte[]> mBufferPool;
    private final Loader<Params, Image> mLoader;

    protected final Binder<URI, Params, Image> mBinder;
    protected final ImageDecoder<Params, Image> mDecoder;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param executor The {@link Executor} to execute load task.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the loaded image.
     * @param fileCache May be <tt>null</tt>. The {@link FileCache} to store the loaded image files.
     * @param decoder The {@link ImageDecoder} to decode the image data.
     * @param binder The {@link Binder} to bind the image to target.
     */
    public ImageLoader(Context context, Executor executor, Cache<URI, Image> imageCache, FileCache fileCache, ImageDecoder<Params, Image> decoder, Binder<URI, Params, Image> binder) {
        super(executor, imageCache);

        mDecoder = decoder;
        mBinder  = binder;
        mLoader  = (fileCache != null ? new FileCacheLoader(fileCache) : new URLLoader(context));
        mBufferPool = Pools.synchronizedPool(Pools.newPool(sFactory, computeMaximumPoolSize(executor)));
        ImageBinder.__checkBinder(getClass(), imageCache, binder);
    }

    /**
     * Equivalent to calling <tt>loadImage(uri, 0, target, (Params[])null)</tt>.
     * @param uri The uri to load.
     * @param target The <tt>Object</tt> to bind the image.
     * @see #loadImage(URI, Object, int)
     * @see #loadImage(URI, int, Object, Params[])
     */
    public final void loadImage(URI uri, Object target) {
        load(uri, target, 0, mBinder, (Params[])null);
    }

    /**
     * Equivalent to calling <tt>loadImage(uri, flags, target, (Params[])null)</tt>.
     * @param uri The uri to load.
     * @param target The <tt>Object</tt> to bind the image.
     * @param flags Loading flags. May be <tt>0</tt> or any combination of <tt>FLAG_XXX</tt> constants.
     * @see #loadImage(URI, Object)
     * @see #loadImage(URI, int, Object, Params[])
     */
    public final void loadImage(URI uri, Object target, int flags) {
        load(uri, target, flags, mBinder, (Params[])null);
    }

    /**
     * Loads the image from the specified <em>uri</em>, bind it to the <em>target</em>. If the image
     * is already cached, it is bind immediately. Otherwise loads the image on a background thread.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * <h5>The default implementation accepts the following URI schemes:</h5>
     * <ul><li>path (no scheme)</li>
     * <li>ftp ({@link #SCHEME_FTP})</li>
     * <li>http ({@link #SCHEME_HTTP})</li>
     * <li>https ({@link #SCHEME_HTTPS})</li>
     * <li>file ({@link ContentResolver#SCHEME_FILE SCHEME_FILE})</li>
     * <li>content ({@link ContentResolver#SCHEME_CONTENT SCHEME_CONTENT})</li>
     * <li>android.resource ({@link ContentResolver#SCHEME_ANDROID_RESOURCE SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param uri The uri to load.
     * @param flags Loading flags. May be <tt>0</tt> or any combination of <tt>FLAG_XXX</tt> constants.
     * @param target The <tt>Object</tt> to bind the image.
     * @param params The parameters of the load task. If the task no parameters, you can pass <em>(Params[])null</em>
     * instead of allocating an empty array.
     * @see #loadImage(URI, Object)
     * @see #loadImage(URI, Object, int)
     */
    @SuppressWarnings("unchecked")
    public final void loadImage(URI uri, int flags, Object target, Params... params) {
        load(uri, target, flags, mBinder, params);
    }

    /**
     * Returns the {@link Binder} associated with this loader.
     * @return The <tt>Binder</tt>.
     */
    public final Binder<URI, Params, Image> getBinder() {
        return mBinder;
    }

    /**
     * Returns the {@link ImageDecoder} associated with this loader.
     * @return The <tt>ImageDecoder</tt>.
     */
    public final ImageDecoder<Params, Image> getImageDecoder() {
        return mDecoder;
    }

    @SuppressWarnings("rawtypes")
    public final void dump(Context context, Printer printer) {
        Pools.dumpPool(mBufferPool, printer);
        if (mDecoder instanceof AbsImageDecoder) {
            ((AbsImageDecoder)mDecoder).dump(printer);
        }

        if (mBinder instanceof ImageBinder) {
            ((ImageBinder)mBinder).dump(context, printer);
        }

        dumpTasks(printer);
    }

    /**
     * Returns the default {@link Parameters} associated with this class
     * (The default parameters sample size = 1, config = RGB_565).
     */
    public static Parameters defaultParameters() {
        return DefaultParameters.sInstance;
    }

    @Override
    protected Image loadInBackground(Task<?, ?> task, URI uri, Params[] params, int flags) {
        final byte[] buffer = mBufferPool.obtain();
        try {
            return (matchScheme(uri) ? mLoader.load(task, uri.toString(), params, flags, buffer) : mDecoder.decodeImage(uri, params, flags, buffer));
        } finally {
            mBufferPool.recycle(buffer);
        }
    }

    /**
     * Called on a background thread to load an image from the specified <em>url</em>.
     * @param task The current {@link Task} whose executing this method.
     * @param url The url to load.
     * @param imageFile The image file to store the image data.
     * @param params The parameters, passed earlier by {@link #loadImage}.
     * @param flags Loading flags, passed earlier by {@link #loadImage}.
     * @param buffer The temporary byte array to used for loading image data.
     * @return The image object, or <tt>null</tt> if the load failed or cancelled.
     */
    protected Image loadImage(Task<?, ?> task, String url, String imageFile, Params[] params, int flags, byte[] buffer) {
        try {
            final DownloadRequest request = new DownloadRequest(url).readTimeout(60000).connectTimeout(60000);
            request.__checkHeaders = false;
            return (request.download(imageFile, task, buffer) == HTTP_OK && !isTaskCancelled(task) ? mDecoder.decodeImage(imageFile, params, flags, buffer) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), new StringBuilder("Couldn't load image data from - '").append(url).append("'\n").append(e).toString());
            return null;
        }
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

    /**
     * The <tt>Loader</tt> interface used to load image from the specified url.
     */
    private static interface Loader<Params, Image> {
        /**
         * Called on a background thread to load an image from the specified <em>url</em>.
         * @param task The current {@link Task} whose executing this method.
         * @param url The url to load.
         * @param params The parameters, passed earlier by <tt>loadInBackground</tt>.
         * @param flags Loading flags, passed earlier by by <tt>loadInBackground</tt>.
         * @param buffer The temporary byte array to use for loading image data.
         * @return The image object, or <tt>null</tt> if the load failed or cancelled.
         */
        Image load(Task<?, ?> task, String url, Params[] params, int flags, byte[] buffer);
    }

    /**
     * Class <tt>URLLoader</tt> is an implementation of a {@link Loader}.
     */
    private final class URLLoader implements Loader<Params, Image> {
        private final String mCacheDir;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         */
        public URLLoader(Context context) {
            mCacheDir = FileUtils.getCacheDir(context, ".temp_image_cache").getPath();
        }

        @Override
        public Image load(Task<?, ?> task, String url, Params[] params, int flags, byte[] buffer) {
            final String imageFile = new StringBuilder(mCacheDir.length() + 16).append(mCacheDir).append('/').append(Thread.currentThread().hashCode()).toString();
            try {
                return loadImage(task, url, imageFile, params, flags, buffer);
            } finally {
                FileUtils.deleteFiles(imageFile, false);
            }
        }
    }

    /**
     * Class <tt>FileCacheLoader</tt> is an implementation of a {@link Loader}.
     */
    private final class FileCacheLoader implements Loader<Params, Image> {
        private final FileCache mCache;

        /**
         * Constructor
         * @param cache The {@link FileCache} to store the loaded image files.
         */
        public FileCacheLoader(FileCache cache) {
            mCache = cache;
        }

        @Override
        public Image load(Task<?, ?> task, String url, Params[] params, int flags, byte[] buffer) {
            final StringBuilder builder = StringUtils.toHexString(new StringBuilder(mCache.getCacheDir().length() + 16), buffer, 0, MessageDigests.computeString(url, buffer, 0, Algorithm.SHA1), true);
            final String hashKey = builder.toString();
            final String imageFile = mCache.get(hashKey);
            Image result = null;

            if (FileUtils.access(imageFile, FileUtils.F_OK) == 0) {
                // Decodes the image file, If file cache hit.
                if ((result = mDecoder.decodeImage(imageFile, params, flags, buffer)) != null) {
                    return result;
                }

                // Removes the hash key from file cache, If decode failed.
                mCache.remove(hashKey);
            }

            if (!isTaskCancelled(task)) {
                // Loads the image from url, If the image file not exists or decode failed.
                builder.setLength(0);
                final String tempFile = builder.append(imageFile, 0, imageFile.lastIndexOf('/') + 1).append(Thread.currentThread().hashCode()).toString();
                if ((result = loadImage(task, url, tempFile, params, flags, buffer)) != null && FileUtils.moveFile(tempFile, imageFile) == 0) {
                    // Saves the image file to file cache, If load succeeded.
                    mCache.put(hashKey, imageFile);
                } else {
                    // Deletes the image file, If load failed.
                    FileUtils.deleteFiles(tempFile, false);
                }
            }

            return result;
        }
    }

    /**
     * The byte array factory.
     */
    private static final Factory<byte[]> sFactory = new Factory<byte[]>() {
        @Override
        public byte[] newInstance() {
            return new byte[16384];
        }
    };

    /**
     * Class <tt>DefaultParameters</tt> (The default parameters sample size = 1, config = RGB_565).
     */
    private static final class DefaultParameters {
        public static final Parameters sInstance = new Parameters(1, Config.RGB_565);
    }

    /**
     * The <tt>ImageDecoder</tt> class used to decode the image data.
     */
    public static abstract class ImageDecoder<Params, Image> {
        /**
         * Returns the scheme with the specified <em>uri</em>. Example: "http".
         * @param uri The uri to parse.
         * @return The scheme or <tt>null</tt> if the <em>uri</em> has no scheme.
         */
        protected static String parseScheme(Object uri) {
            String scheme = null;
            if (uri instanceof Uri) {
                scheme = ((Uri)uri).getScheme();
            } else {
                final String uriString = uri.toString();
                final int index = uriString.indexOf(':');
                if (index != -1) {
                    scheme = uriString.substring(0, index);
                }
            }

            return scheme;
        }

        /**
         * Opens an <tt>InputStream</tt> from the specified <em>uri</em>.
         * <h5>Accepts the following URI schemes:</h5>
         * <ul><li>path (no scheme)</li>
         * <li>file ({@link ContentResolver#SCHEME_FILE SCHEME_FILE})</li>
         * <li>content ({@link ContentResolver#SCHEME_CONTENT SCHEME_CONTENT})</li>
         * <li>android.resource ({@link ContentResolver#SCHEME_ANDROID_RESOURCE SCHEME_ANDROID_RESOURCE})</li></ul>
         * @param context The <tt>Context</tt>.
         * @param uri The uri to open.
         * @return The <tt>InputStream</tt>.
         * @throws FileNotFoundException if the <em>uri</em> could not be opened.
         */
        protected static InputStream openInputStream(Context context, Object uri) throws FileNotFoundException {
            if (uri instanceof Uri) {
                return context.getContentResolver().openInputStream((Uri)uri);
            } else {
                final String uriString = uri.toString();
                if (uriString.indexOf(':') == -1) {
                    return new FileInputStream(uriString);
                } else {
                    return context.getContentResolver().openInputStream(Uri.parse(uriString));
                }
            }
        }

        /**
         * Decodes an image from the specified <em>uri</em>.
         * @param uri The uri to decode.
         * @param params The parameters, passed earlier by {@link ImageLoader#loadImage}.
         * @param flags The flags, passed earlier by {@link ImageLoader#loadImage}.
         * @param tempStorage The temporary storage to use for decoding. Suggest 16K.
         * @return The image object, or <tt>null</tt> if the image data cannot be decode.
         * @see #openInputStream(Context, Object)
         */
        public abstract Image decodeImage(Object uri, Params[] params, int flags, byte[] tempStorage);
    }
}
