package android.ext.content.image;

import static java.net.HttpURLConnection.HTTP_OK;
import java.util.concurrent.Executor;
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
import android.ext.util.UriUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
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
     * cache or load not from net this flag will be ignore.
     */
    public static final int FLAG_IGNORE_FILE_CACHE = 0x00400000;

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
     * @param binder The {@link Binder} to bind the image to target.
     * @see #ImageLoader(ImageLoader, ImageDecoder, Binder)
     */
    public ImageLoader(Context context, Executor executor, Cache<URI, Image> imageCache, FileCache fileCache, ImageDecoder<Image> decoder, Binder<URI, Object, Image> binder) {
        super(executor, imageCache);

        mDecoder = decoder;
        mBinder  = binder;
        mLoader  = (fileCache != null ? new FileCacheLoader<Image>(context, this, fileCache) : new Loader<Image>(context, this));
        mBufferPool = Pools.synchronizedPool(Pools.newPool(mLoader, computeMaximumPoolSize(executor)));
        DebugUtils.__checkWarning(imageCache == null && binder instanceof ImageBinder && ((ImageBinder<?, ?>)binder).mTransformer instanceof CacheTransformer, getClass().getName(), "The " + getClass().getSimpleName() + " has no memory cache, The binder should be no drawable cache!!!");
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link ImageLoader} from the specified <em>loader</em>. The returned loader will
     * be share the internal cache (including memory cache, file cache etc.) with the <em>loader</em>.</p>
     * @param loader The <tt>ImageLoader</tt> to copy.
     * @param decoder May be <tt>null</tt>. The {@link ImageDecoder} to decode the image data.
     * @param binder May be <tt>null</tt>. The {@link Binder} to bind the image to target.
     * @see #ImageLoader(Context, Executor, Cache, FileCache, ImageDecoder, Binder)
     */
    public ImageLoader(ImageLoader<URI, Image> loader, ImageDecoder<Image> decoder, Binder<URI, Object, Image> binder) {
        super(loader);
        mLoader  = (loader.mLoader instanceof FileCacheLoader ? new FileCacheLoader<Image>(loader.mLoader, this) : new Loader<Image>(loader.mLoader, this));
        mBinder  = (binder != null ? binder : loader.mBinder);
        mDecoder = (decoder != null ? decoder : loader.mDecoder);
        mBufferPool = loader.mBufferPool;
    }

    /**
     * Equivalent to calling <tt>loadImage(uri, target, 0, getBinder())</tt>.
     * @param uri The uri to load.
     * @param target The <tt>Object</tt> to bind the image.
     * @see #loadImage(URI, Object, int)
     * @see #loadImage(URI, Object, int, Binder)
     */
    public final void loadImage(URI uri, Object target) {
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
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
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
     * @return The <tt>Binder</tt>.
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

    /**
     * Returns the default image associated with this loader.
     * @return The {@link Drawable} of the default image or <tt>null</tt>.
     */
    public final Drawable getDefaultDrawable() {
        return (mBinder instanceof ImageBinder ? ((ImageBinder<?, ?>)mBinder).mDefaultImage : null);
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
     * Matches the scheme of the specified <em>uri</em>. The default implementation
     * match the "http", "https" and "ftp".
     * @param uri The uri to match.
     * @return <tt>true</tt> if the scheme match successful, <tt>false</tt> otherwise.
     */
    protected boolean matchScheme(URI uri) {
        return UriUtils.matchScheme(uri);
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
            request.__checkDumpHeaders = false;
            return (request.download(imageFile, task, buffer) == HTTP_OK && !isTaskCancelled(task) ? mDecoder.decodeImage(imageFile, params, flags, buffer) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), new StringBuilder("Couldn't load image data from - '").append(url).append("'\n").append(e).toString());
            return null;
        }
    }

    /**
     * Class <tt>Loader</tt> used to load image from the specified url.
     */
    private static class Loader<Image> implements Factory<byte[]> {
        /* package */ final String mCacheDir;
        /* package */ final ImageLoader<?, Image> mOwner;

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
         * Copy constructor
         * @param loader The <tt>Loader</tt>.
         * @param owner The <tt>ImageLoader</tt>.
         */
        public Loader(Loader<Image> loader, ImageLoader<?, Image> owner) {
            mOwner = owner;
            mCacheDir = loader.mCacheDir;
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

        /**
         * Copy constructor
         * @param loader The <tt>Loader</tt>.
         * @param owner The <tt>ImageLoader</tt>.
         */
        public FileCacheLoader(Loader<Image> loader, ImageLoader<?, Image> owner) {
            super(loader, owner);
            mCache = ((FileCacheLoader<Image>)loader).mCache;
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
    public static interface ImageDecoder<Image> {
        /**
         * Decodes an image from the specified <em>uri</em>.
         * @param uri The uri to decode.
         * @param params The parameters, passed earlier by {@link ImageLoader#loadImage}.
         * @param flags The flags, passed earlier by {@link ImageLoader#loadImage}.
         * @param tempStorage The temporary storage to use for decoding. Suggest 16K.
         * @return The image object, or <tt>null</tt> if the image data cannot be decode.
         */
        Image decodeImage(Object uri, Object[] params, int flags, byte[] tempStorage);
    }
}
