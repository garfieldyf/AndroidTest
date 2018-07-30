package android.ext.content.image;

import static java.net.HttpURLConnection.HTTP_OK;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import android.content.ContentResolver;
import android.content.Context;
import android.ext.cache.Cache;
import android.ext.cache.FileCache;
import android.ext.content.AsyncLoader;
import android.ext.content.image.BitmapDecoder.Parameters;
import android.ext.content.image.ImageBinder.CacheTransformer;
import android.ext.net.DownloadRequest;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.Pools;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.ext.util.StringUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.util.Log;
import android.util.Printer;
import android.widget.ImageView;

/**
 * Class <tt>ImageLoader</tt> allows to load the image from the URI
 * on a background thread and bind it to target on the UI thread.
 * <h5>ImageLoader's generic types</h5>
 * <p>The two types used by an image loader are the following:</p>
 * <ol><li><tt>URI</tt>, The uri type of the image loader's key.</li>
 * <li><tt>Image</tt>, The image type of the load result.</li></ol>
 * @author Garfield
 * @version 6.0
 */
public class ImageLoader<URI, Image> extends AsyncLoader<URI, Object, Image> {
    /**
     * Indicates the image loader will be ignore the file cache
     * when it will be load image. If the image loader has no file
     * cache this flag will be ignore.
     */
    public static final int FLAG_IGNORE_FILE_CACHE = 0x00400000;

    public static final String SCHEME_FTP   = "ftp";
    public static final String SCHEME_HTTP  = "http";
    public static final String SCHEME_HTTPS = "https";

    private final Loader<Image> mLoader;
    private final Pool<byte[]> mBufferPool;

    protected final ImageDecoder<Image> mDecoder;
    protected final Binder<URI, Object, Image> mBinder;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param executor The {@link Executor} to execute load task.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the loaded image.
     * @param fileCache May be <tt>null</tt>. The {@link FileCache} to store the loaded image files.
     * @param decoder The {@link ImageDecoder} to decode the image data.
     * @param binder May be <tt>null</tt>. The {@link Binder} to bind the image to target.
     */
    public ImageLoader(Context context, Executor executor, Cache<URI, Image> imageCache, FileCache fileCache, ImageDecoder<Image> decoder, Binder<URI, Object, Image> binder) {
        super(executor, imageCache);

        mDecoder = decoder;
        mBinder  = binder;
        mLoader  = (fileCache != null ? new FileCacheLoader<Image>(context, this, fileCache) : new Loader<Image>(context, this));
        mBufferPool = Pools.synchronizedPool(Pools.newPool(mLoader, computeMaximumPoolSize(executor)));
        DebugUtils.__checkWarning(imageCache == null && binder instanceof ImageBinder && ((ImageBinder<?, ?>)binder).mTransformer instanceof CacheTransformer, getClass().getName(), "The " + getClass().getSimpleName() + " has no memory cache, The internal binder should be no drawable cache!!!");
    }

    /**
     * Equivalent to calling <tt>loadImage(uri, target, 0, getBinder())</tt>.
     * @param uri The uri to load.
     * @param target The <tt>Object</tt> to bind the image.
     * @see #loadImage(URI, Object, int)
     * @see #loadImage(URI, Object, int, Binder)
     */
    public final void loadImage(URI uri, Object target) {
        DebugUtils.__checkError(mBinder == null, "The mBinder is null. Use loadImage(URI uri, Object target, int flags, Binder<URI, Object, Image> binder) instead.");
        load(uri, target, 0, mBinder, (Object[])null);
    }

    /**
     * Equivalent to calling <tt>loadImage(uri, target, flags, getBinder())</tt>.
     * @param uri The uri to load.
     * @param target The <tt>Object</tt> to bind the image.
     * @param flags Loading flags. May be <tt>0</tt> or any combination of <tt>FLAG_XXX</tt> constants.
     * @see #loadImage(URI, Object)
     * @see #loadImage(URI, Object, int, Binder)
     */
    public final void loadImage(URI uri, Object target, int flags) {
        DebugUtils.__checkError(mBinder == null, "The mBinder is null. Use loadImage(URI uri, Object target, int flags, Binder<URI, Object, Image> binder) instead.");
        load(uri, target, flags, mBinder, (Object[])null);
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
     * @param target The <tt>Object</tt> to bind the image.
     * @param flags Loading flags. May be <tt>0</tt> or any combination of <tt>FLAG_XXX</tt> constants.
     * @param binder The {@link Binder} to bind the image to <em>target</em>.
     * @see #loadImage(URI, Object)
     * @see #loadImage(URI, Object, int)
     */
    public final void loadImage(URI uri, Object target, int flags, Binder<URI, Object, Image> binder) {
        load(uri, target, flags, binder, (Object[])null);
    }

    /**
     * Returns the {@link Binder} associated with this loader.
     * @return The <tt>Binder</tt> or <tt>null</tt>.
     */
    public final Binder<URI, Object, Image> getBinder() {
        return mBinder;
    }

    /**
     * Returns the {@link ImageDecoder} associated with this loader.
     * @return The <tt>ImageDecoder</tt>.
     */
    public final ImageDecoder<Image> getImageDecoder() {
        return mDecoder;
    }

    @Override
    public void dump(Context context, Printer printer) {
        Pools.dumpPool(mBufferPool, printer);
        if (mDecoder instanceof AbsImageDecoder) {
            ((AbsImageDecoder<?>)mDecoder).dump(printer);
        }

        if (mBinder instanceof ImageBinder) {
            ((ImageBinder<?, ?>)mBinder).dump(context, printer);
        }

        super.dump(context, printer);
    }

    /**
     * Returns the default {@link Parameters} associated with this class
     * (The default parameters sample size = 1, config = RGB_565).
     */
    public static Parameters defaultParameters() {
        return DefaultParameters.sInstance;
    }

    /**
     * Returns the default {@link Binder} associated with this class.
     * The default binder has no default image, no drawable cache and
     * can only bind the {@link Bitmap} to {@link ImageView}.
     */
    @SuppressWarnings("unchecked")
    public static <URI> Binder<URI, Object, Bitmap> defaultBinder() {
        return (Binder<URI, Object, Bitmap>)DefaultBinder.sInstance;
    }

    @Override
    protected Image loadInBackground(Task<?, ?> task, URI uri, Object[] params, int flags) {
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
    protected Image loadImage(Task<?, ?> task, String url, String imageFile, Object[] params, int flags, byte[] buffer) {
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
     * Class <tt>Loader</tt> used to load image from the specified url.
     */
    private static class Loader<Image> implements Factory<byte[]> {
        protected final String mCacheDir;
        protected final ImageLoader<?, Image> mOwner;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         * @param owner The <tt>ImageLoader</tt>.
         */
        public Loader(Context context, ImageLoader<?, Image> owner) {
            mOwner = owner;
            mCacheDir = FileUtils.getCacheDir(context, ".temp_image_cache").getPath();
        }

        /**
         * Returns a new byte array.
         * @return A new byte array.
         */
        @Override
        public byte[] newInstance() {
            return new byte[16384];
        }

        /**
         * Called on a background thread to load an image from the specified <em>url</em>.
         * @param task The current {@link Task} whose executing this method.
         * @param url The url to load.
         * @param params The parameters, passed earlier by <tt>loadInBackground</tt>.
         * @param flags Loading flags, passed earlier by by <tt>loadInBackground</tt>.
         * @param buffer The temporary byte array to use for loading image data.
         * @return The image object, or <tt>null</tt> if the load failed or cancelled.
         */
        public Image load(Task<?, ?> task, String url, Object[] params, int flags, byte[] buffer) {
            final String imageFile = new StringBuilder(mCacheDir.length() + 16).append(mCacheDir).append('/').append(Thread.currentThread().hashCode()).toString();
            try {
                return mOwner.loadImage(task, url, imageFile, params, flags, buffer);
            } finally {
                FileUtils.deleteFiles(imageFile, false);
            }
        }
    }

    /**
     * Class <tt>FileCacheLoader</tt> is an implementation of a {@link Loader}.
     */
    private static final class FileCacheLoader<Image> extends Loader<Image> {
        private final FileCache mCache;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         * @param owner The <tt>ImageLoader</tt>.
         * @param cache The {@link FileCache} to store the loaded image files.
         */
        public FileCacheLoader(Context context, ImageLoader<?, Image> owner, FileCache cache) {
            super(context, owner);
            mCache = cache;
        }

        @Override
        public Image load(Task<?, ?> task, String url, Object[] params, int flags, byte[] buffer) {
            if ((flags & FLAG_IGNORE_FILE_CACHE) != 0) {
                return super.load(task, url, params, flags, buffer);
            }

            final StringBuilder builder = StringUtils.toHexString(new StringBuilder(mCache.getCacheDir().length() + 16), buffer, 0, MessageDigests.computeString(url, buffer, 0, Algorithm.SHA1), true);
            final String hashKey = builder.toString();
            final String imageFile = mCache.get(hashKey);
            Image result = null;

            if (FileUtils.access(imageFile, FileUtils.F_OK) == 0) {
                // Decodes the image file, If file cache hit.
                if ((result = mOwner.mDecoder.decodeImage(imageFile, params, flags, buffer)) != null) {
                    return result;
                }

                // Removes the image file from file cache, If decode failed.
                mCache.remove(hashKey);
            }

            if (!mOwner.isTaskCancelled(task)) {
                // Loads the image from url, If the image file is not exists or decode failed.
                builder.setLength(0);
                final String tempFile = builder.append(imageFile, 0, imageFile.lastIndexOf('/') + 1).append(Thread.currentThread().hashCode()).toString();
                if ((result = mOwner.loadImage(task, url, tempFile, params, flags, buffer)) != null && FileUtils.moveFile(tempFile, imageFile) == 0) {
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
     * Class <tt>DefaultParameters</tt> (The default parameters sample size = 1, config = RGB_565).
     */
    private static final class DefaultParameters {
        public static final Parameters sInstance = new Parameters(1, Config.RGB_565);
    }

    /**
     * Class <tt>DefaultBinder</tt> used to bind the <tt>Bitmap</tt> to <tt>ImageView</tt>.
     * The <tt>DefaultBinder</tt> has no image cache and the default image.
     */
    private static final class DefaultBinder implements Binder<Object, Object, Bitmap> {
        public static final DefaultBinder sInstance = new DefaultBinder();

        @Override
        public void bindValue(Object key, Object[] params, Object target, Bitmap value, int state) {
            final ImageView view = (ImageView)target;
            if (value != null) {
                view.setImageBitmap(value);
            } else {
                view.setImageDrawable(null);
            }
        }
    }

    /**
     * The <tt>ImageDecoder</tt> class used to decode the image data.
     */
    public static abstract class ImageDecoder<Image> {
        /**
         * Returns the scheme with the specified <em>uri</em>. Example: "http".
         * @param uri The uri to parse.
         * @return The scheme or <tt>null</tt> if the <em>uri</em> has no scheme.
         */
        public static String parseScheme(Object uri) {
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
        public static InputStream openInputStream(Context context, Object uri) throws FileNotFoundException {
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
        public abstract Image decodeImage(Object uri, Object[] params, int flags, byte[] tempStorage);
    }
}
