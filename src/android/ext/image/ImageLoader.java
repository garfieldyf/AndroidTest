package android.ext.image;

import static java.net.HttpURLConnection.HTTP_OK;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import android.content.Context;
import android.ext.cache.Cache;
import android.ext.cache.Caches;
import android.ext.cache.FileCache;
import android.ext.content.AsyncLoader;
import android.ext.image.binder.ImageBinder;
import android.ext.image.decoder.AbsImageDecoder;
import android.ext.image.params.Parameters;
import android.ext.net.DownloadRequest;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.Pools;
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
 * <h2>Usage</h2>
 * <p>Here is an example:</p><pre>
 * mImageLoader.load(uri).into(imageView);</pre>
 * @author Garfield
 */
public class ImageLoader<URI, Image> extends AsyncLoader<URI, Object, Image> {
    /**
     * If set the image decoder will be use the custom {@link Parameters}
     * to decode the image and ignore the internal decode <tt>Parameters</tt>.
     * @see LoadRequest#setParameters(Parameters)
     */
    public static final int FLAG_CUSTOM_PARAMETERS = 0x00400000;

    private final Loader<Image> mLoader;
    private final Pool<byte[]> mBufferPool;
    private final LoadRequest<URI, Image> mRequest;

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
     */
    public ImageLoader(Context context, Executor executor, Cache<URI, Image> imageCache, FileCache fileCache, ImageDecoder<Image> decoder, Binder<URI, Object, Image> binder) {
        super(executor, imageCache);

        mRequest = new LoadRequest<URI, Image>(this);
        mDecoder = decoder;
        mBinder  = binder;
        mLoader  = (fileCache != null ? new FileCacheLoader(fileCache) : new URLLoader(context));
        mBufferPool = Pools.synchronizedPool(Pools.newPool(computeBufferPoolMaxSize(executor), 16384));
        ImageBinder.__checkTransformer(getClass(), imageCache, binder);
    }

    /**
     * Loads the image from the specified <em>uri</em>, bind it to the target. If the image
     * is already cached, it is bind immediately. Otherwise loads the image on a background
     * thread. <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * <h5>The default implementation accepts the following URI schemes:</h5>
     * <ul><li>path (no scheme)</li>
     * <li>ftp ({@link #SCHEME_FTP})</li>
     * <li>http ({@link #SCHEME_HTTP})</li>
     * <li>https ({@link #SCHEME_HTTPS})</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android_asset ({@link #SCHEME_FILE})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param uri The uri to load.
     * @return The {@link LoadRequest}.
     */
    public final LoadRequest<URI, Image> load(URI uri) {
        mRequest.mUri = uri;
        mRequest.mBinder = mBinder;
        return mRequest;
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
    public final Drawable getDefaultImage() {
        return (mBinder instanceof ImageBinder ? ((ImageBinder<?, ?>)mBinder).getDefaultImage() : null);
    }

    @Override
    public void dump(Context context, Printer printer) {
        super.dump(context, printer);
        Pools.dumpPool(mBufferPool, printer);
        if (mDecoder instanceof AbsImageDecoder) {
            ((AbsImageDecoder<?>)mDecoder).dump(printer);
        }

        if (mBinder instanceof ImageBinder) {
            ((ImageBinder<?, ?>)mBinder).dump(context, printer);
        }

        DebugUtils.dumpSummary(printer, new StringBuilder(130), 130, " Dumping shared memory cache and file cache ");
        Caches.dumpCache(getCache(), context, printer);
        if (mLoader instanceof ImageLoader.FileCacheLoader) {
            Caches.dumpCache(((ImageLoader<?, ?>.FileCacheLoader)mLoader).mCache, context, printer);
        }
    }

    /**
     * Returns the default {@link Parameters} associated with this class
     * (The default parameters sample size = 1, config = RGB_565, mutable = false).
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
     * @param params The parameters, passed earlier by {@link #load}.
     * @param flags Loading flags, passed earlier by {@link #load}.
     * @param buffer The temporary byte array to used for loading image data.
     * @return The image object, or <tt>null</tt> if the load failed or cancelled.
     */
    protected Image loadImage(Task<?, ?> task, String url, File imageFile, Object[] params, int flags, byte[] buffer) {
        try {
            final DownloadRequest request = new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
            request.__checkDumpHeaders = false;
            return (request.download(imageFile.getPath(), task, buffer) == HTTP_OK && !isTaskCancelled(task) ? mDecoder.decodeImage(imageFile, params, flags, buffer) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), new StringBuilder("Couldn't load image data from - '").append(url).append("'\n").append(e).toString());
            return null;
        }
    }

    /**
     * Computes the maximum buffer pool size of the image loader.
     */
    private static int computeBufferPoolMaxSize(Executor executor) {
        final int maxPoolSize = (executor instanceof ThreadPoolExecutor ? ((ThreadPoolExecutor)executor).getMaximumPoolSize() : 4);
        return (maxPoolSize == Integer.MAX_VALUE ? 12 : maxPoolSize + 1);
    }

    /**
     * Interface <tt>Loader</tt> used to load image from the specified url.
     */
    private static interface Loader<Image> {
        /**
         * Called on a background thread to load an image from the specified <em>url</em>.
         * @param task The current {@link Task} whose executing this method.
         * @param url The url to load.
         * @param params The parameters, passed earlier by <tt>loadInBackground</tt>.
         * @param flags Loading flags, passed earlier by by <tt>loadInBackground</tt>.
         * @param buffer The temporary byte array to use for loading image data.
         * @return The image object, or <tt>null</tt> if the load failed or cancelled.
         */
        Image load(Task<?, ?> task, String url, Object[] params, int flags, byte[] buffer);
    }

    /**
     * Class <tt>URLLoader</tt> is an implementation of a {@link Loader}.
     */
    private final class URLLoader implements Loader<Image> {
        private final File mCacheDir;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         */
        public URLLoader(Context context) {
            mCacheDir = FileUtils.getCacheDir(context, ".temp_image_cache");
        }

        @Override
        public Image load(Task<?, ?> task, String url, Object[] params, int flags, byte[] buffer) {
            final File imageFile = new File(mCacheDir, Integer.toString(Thread.currentThread().hashCode()));
            try {
                return loadImage(task, url, imageFile, params, flags, buffer);
            } finally {
                imageFile.delete();
            }
        }
    }

    /**
     * Class <tt>FileCacheLoader</tt> is an implementation of a {@link Loader}.
     */
    private final class FileCacheLoader implements Loader<Image> {
        /* package */ final FileCache mCache;

        /**
         * Constructor
         * @param cache The {@link FileCache} to store the loaded image files.
         */
        public FileCacheLoader(FileCache cache) {
            mCache = cache;
        }

        @Override
        public Image load(Task<?, ?> task, String url, Object[] params, int flags, byte[] buffer) {
            final String hashKey = StringUtils.toHexString(buffer, 0, MessageDigests.computeString(url, buffer, 0, Algorithm.SHA1));
            final File imageFile = mCache.get(hashKey);
            Image result = null;

            if (imageFile.exists()) {
                // Decodes the image file, If file cache hit.
                if ((result = mDecoder.decodeImage(imageFile, params, flags, buffer)) != null) {
                    return result;
                }

                // Removes the image file from file cache, If decode failed.
                mCache.remove(hashKey);
            }

            if (!isTaskCancelled(task)) {
                // Loads the image from url, If the image file is not exists or decode failed.
                final File tempFile = new File(imageFile.getPath() + "." + Thread.currentThread().hashCode());
                if ((result = loadImage(task, url, tempFile, params, flags, buffer)) != null && FileUtils.moveFile(tempFile.getPath(), imageFile.getPath()) == 0) {
                    // Saves the image file to file cache, If load succeeded.
                    mCache.put(hashKey, imageFile);
                } else {
                    // Deletes the temp file, If load failed.
                    tempFile.delete();
                }
            }

            return result;
        }
    }

    /**
     * Class <tt>DefaultParameters</tt> (The default parameters sampleSize = 1, config = RGB_565, mutable = false).
     */
    private static final class DefaultParameters {
        public static final Parameters sInstance = new Parameters(Config.RGB_565, 1, false);
    }

    /**
     * Class <tt>DefaultBinder</tt> used to bind the <tt>Bitmap</tt> to <tt>ImageView</tt>.
     * The <tt>DefaultBinder</tt> has no image cache, no default image.
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
     * The <tt>LoadRequest</tt> class used to {@link ImageLoader} to load the image.
     */
    public static final class LoadRequest<URI, Image> {
        /* package */ URI mUri;
        /* package */ int mFlags;
        /* package */ Object[] mParams;
        /* package */ Binder<URI, Object, Image> mBinder;
        /* package */ final ImageLoader<URI, Image> mLoader;

        /**
         * Constructor
         * @param loader The {@link ImageLoader}.
         */
        /* package */ LoadRequest(ImageLoader<URI, Image> loader) {
            mLoader = loader;
        }

        /**
         * Loads the image with the arguments supplied to this request.
         * @param target The <tt>Object</tt> to bind.
         */
        public final void into(Object target) {
            mLoader.load(mUri, target, mFlags, mBinder, mParams);
            mFlags  = 0;
            mParams = null;
        }

        /**
         * Adds the loading flags to load image.
         * @param flags Loading flags. May be any combination of
         * <tt>FLAG_XXX</tt> constants.
         * @return This request.
         */
        public final LoadRequest<URI, Image> addFlags(int flags) {
            mFlags |= flags;
            return this;
        }

        /**
         * Equivalent to calling <tt>addFlags(FLAG_IGNORE_MEMORY_CACHE)</tt>.
         * @return This request.
         * @see #addFlags(int)
         * @see AsyncLoader#FLAG_IGNORE_MEMORY_CACHE FLAG_IGNORE_MEMORY_CACHE
         */
        public final LoadRequest<URI, Image> skipMemoryCache() {
            mFlags |= FLAG_IGNORE_MEMORY_CACHE;
            return this;
        }

        /**
         * Sets the parameters to load image.
         * @param params The parameters of the load task.
         * @return This request.
         */
        public final LoadRequest<URI, Image> setParams(Object... params) {
            mParams = params;
            return this;
        }

        /**
         * Sets the custom {@link Parameters} to decode image.
         * @param parameters The <tt>Parameters</tt> to decode.
         * @return This request.
         * @see ImageLoader#FLAG_CUSTOM_PARAMETERS FLAG_CUSTOM_PARAMETERS
         */
        public final LoadRequest<URI, Image> setParameters(Parameters parameters) {
            mFlags |= FLAG_CUSTOM_PARAMETERS;
            mParams = new Object[] { parameters };
            return this;
        }

        /**
         * Sets the {@link Binder} to bind the image to target.
         * @param binder The <tt>Binder</tt> to bind.
         * @return This request.
         */
        public final LoadRequest<URI, Image> setBinder(Binder<URI, Object, Image> binder) {
            mBinder = binder;
            return this;
        }
    }

    /**
     * The <tt>ImageDecoder</tt> class used to decode the image data.
     */
    public static interface ImageDecoder<Image> {
        /**
         * Decodes an image from the specified <em>uri</em>.
         * @param uri The uri to decode, passed earlier by {@link ImageLoader#load}.
         * @param params The parameters, passed earlier by {@link ImageLoader#load}.
         * @param flags The flags, passed earlier by {@link ImageLoader#load}.
         * @param tempStorage May be <tt>null</tt>. The temporary storage to use for
         * decoding. Suggest 16K.
         * @return The image object, or <tt>null</tt> if the image data cannot be decode.
         */
        Image decodeImage(Object uri, Object[] params, int flags, byte[] tempStorage);
    }
}