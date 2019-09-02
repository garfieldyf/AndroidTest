package android.ext.image;

import static java.net.HttpURLConnection.HTTP_OK;
import java.io.File;
import java.util.Arrays;
import android.content.Context;
import android.ext.cache.Cache;
import android.ext.cache.FileCache;
import android.ext.content.AsyncLoader;
import android.ext.image.params.Parameters;
import android.ext.image.transformer.BitmapTransformer;
import android.ext.image.transformer.Transformer;
import android.ext.net.DownloadRequest;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.StringUtils;
import android.ext.util.UriUtils;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

/**
 * Class <tt>ImageLoader</tt> allows to load the image from the URI
 * on a background thread and bind it to target on the UI thread.
 * <h3>ImageLoader's generic types</h3>
 * <p>The two types used by an image loader are the following:</p>
 * <ol><li><tt>URI</tt>, The uri type of the image loader's key.</li>
 * <li><tt>Image</tt>, The image type of the load result.</li></ol>
 * @author Garfield
 */
@SuppressWarnings("unchecked")
public class ImageLoader<URI, Image> extends AsyncLoader<URI, Object, Image> {
    private final Loader<Image> mLoader;
    private final LoadRequest<URI, Image> mRequest;

    protected final ImageDecoder<Image> mDecoder;
    protected final ImageModule<URI, Image> mModule;
    protected final Binder<URI, Object, Image> mBinder;

    /**
     * Constructor
     * @param module The {@link ImageModule}.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the loaded image.
     * @param fileCache May be <tt>null</tt>. The {@link FileCache} to store the loaded image files.
     * @param decoder The {@link ImageDecoder} to decode the image data.
     * @param binder The {@link Binder} to bind the image to target.
     */
    public ImageLoader(ImageModule<URI, Image> module, Cache<URI, Image> imageCache, FileCache fileCache, ImageDecoder<Image> decoder, Binder<URI, Object, Image> binder) {
        super(module.mExecutor, imageCache);

        mRequest = new LoadRequest<URI, Image>(this);
        mDecoder = decoder;
        mModule  = module;
        mBinder  = binder;
        mLoader  = (fileCache != null ? new FileCacheLoader(fileCache) : new URLLoader(module.mContext));
    }

    /**
     * Loads the image from the specified <em>uri</em>, bind it to the target. If the image
     * is already cached, it is bind immediately. Otherwise loads the image on a background
     * thread. <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * <h3>The default implementation accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>ftp ({@link #SCHEME_FTP})</li>
     * <li>http ({@link #SCHEME_HTTP})</li>
     * <li>https ({@link #SCHEME_HTTPS})</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android.asset ({@link #SCHEME_ANDROID_ASSET})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param uri The uri to load.
     * @return The {@link LoadRequest}.
     */
    public final LoadRequest<URI, Image> load(URI uri) {
        DebugUtils.__checkUIThread("load");
        mRequest.mUri = uri;
        mRequest.mBinder = mBinder;
        mRequest.mParams = mModule.mParamsPool.obtain();
        return mRequest;
    }

    /**
     * Equivalent to calling<pre>
     * load(uri).parameters(Parameters.defaultParameters())
     *     .binder(ImageLoader.defaultBinder())
     *     .into(view);</pre>
     * @param uri The uri to load.
     * @param view The {@link ImageView} to bind.
     * @see #load(URI)
     * @see LoadRequest
     */
    public final void load(URI uri, ImageView view) {
        final Object[] params = mModule.mParamsPool.obtain();
        params[0] = Parameters.defaultParameters();
        load(uri, view, 0, (Binder<URI, Object, Image>)DefaultBinder.sInstance, params);
    }

    /**
     * Returns the {@link FileCache} associated with this loader.
     * @return The <tt>FileCache</tt> or <tt>null</tt>.
     */
    public final FileCache getFileCache() {
        return mLoader.getFileCache();
    }

    /**
     * Returns the {@link Binder} associated with this loader.
     * @return The <tt>Binder</tt>.
     */
    public final <T extends Binder<URI, Object, Image>> T getBinder() {
        return (T)mBinder;
    }

    /**
     * Returns the {@link ImageDecoder} associated with this loader.
     * @return The <tt>ImageDecoder</tt>.
     */
    public final <T extends ImageDecoder<Image>> T getImageDecoder() {
        return (T)mDecoder;
    }

    /**
     * Returns the default {@link Binder} associated with this class.
     * The default binder has no default image can only bind the
     * {@link Bitmap} to {@link ImageView}.
     */
    public static <URI> Binder<URI, Object, Bitmap> defaultBinder() {
        return (Binder<URI, Object, Bitmap>)DefaultBinder.sInstance;
    }

    /**
     * Removes the image from this loader's memory cache and file cache.
     * @param uri The uri to remove.
     */
    @Override
    public void remove(URI uri) {
        super.remove(uri);
        if (matchScheme(uri)) {
            mLoader.remove(uri);
        }
    }

    @Override
    protected void onRecycle(Object[] params) {
        Arrays.fill(params, null);
        mModule.mParamsPool.recycle(params);
    }

    @Override
    protected Image loadInBackground(Task<?, ?> task, URI uri, Object[] params, int flags) {
        final byte[] buffer = mModule.mBufferPool.obtain();
        try {
            final Object target = getTarget(task);
            return (matchScheme(uri) ? mLoader.load(task, uri.toString(), target, params, flags, buffer) : mDecoder.decodeImage(uri, target, params, flags, buffer));
        } finally {
            mModule.mBufferPool.recycle(buffer);
        }
    }

    /**
     * Matches the scheme of the specified <em>uri</em>. The default implementation
     * match the "http", "https" and "ftp".
     * @param uri The uri to match.
     * @return <tt>true</tt> if the scheme match successful, <tt>false</tt> otherwise.
     * @see UriUtils#matchScheme(Object)
     */
    protected boolean matchScheme(URI uri) {
        return UriUtils.matchScheme(uri);
    }

    /**
     * Called on a background thread to load an image from the specified <em>url</em>.
     * @param task The current {@link Task} whose executing this method.
     * @param url The url to load.
     * @param imageFile The image file to store the image data.
     * @param target The <tt>Object</tt> to bind, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @param flags Loading flags, passed earlier by {@link #load}.
     * @param buffer The temporary byte array to used for loading image data.
     * @return The image object, or <tt>null</tt> if the load failed or cancelled.
     */
    protected Image loadImage(Task<?, ?> task, String url, File imageFile, Object target, Object[] params, int flags, byte[] buffer) {
        try {
            final DownloadRequest request = new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
            request.__checkDumpHeaders = false;
            return (request.download(imageFile.getPath(), task, buffer) == HTTP_OK && !isTaskCancelled(task) ? mDecoder.decodeImage(imageFile, target, params, flags, buffer) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load image data from - '" + url + "'\n" + e);
            return null;
        }
    }

    /**
     * Class <tt>Loader</tt> used to load image from the specified url.
     */
    /* package */ static abstract class Loader<Image> {
        /**
         * Returns the {@link FileCache} associated with this loader.
         * @return The <tt>FileCache</tt> or <tt>null</tt>.
         */
        public FileCache getFileCache() {
            return null;
        }

        /**
         * Removes the cache file for the specified <em>uri</em>.
         * @param uri The uri to remove.
         */
        public void remove(Object uri) {
        }

        /**
         * Called on a background thread to load an image from the specified <em>url</em>.
         * @param task The current {@link Task} whose executing this method.
         * @param url The url to load.
         * @param target The <tt>Object</tt> to bind, passed earlier by {@link #load}.
         * @param params The parameters, passed earlier by {@link #load}.
         * @param flags Loading flags, passed earlier by {@link #load}.
         * @param buffer The temporary byte array to use for loading image data.
         * @return The image object, or <tt>null</tt> if the load failed or cancelled.
         */
        public abstract Image load(Task<?, ?> task, String url, Object target, Object[] params, int flags, byte[] buffer);
    }

    /**
     * Class <tt>URLLoader</tt> is an implementation of a {@link Loader}.
     */
    private final class URLLoader extends Loader<Image> {
        private final File mCacheDir;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         */
        public URLLoader(Context context) {
            mCacheDir = FileUtils.getCacheDir(context, null);
        }

        @Override
        public Image load(Task<?, ?> task, String url, Object target, Object[] params, int flags, byte[] buffer) {
            final File imageFile = new File(mCacheDir, Integer.toString(Thread.currentThread().hashCode()));
            try {
                return loadImage(task, url, imageFile, target, params, flags, buffer);
            } finally {
                imageFile.delete();
            }
        }
    }

    /**
     * Class <tt>FileCacheLoader</tt> is an implementation of a {@link Loader}.
     */
    private final class FileCacheLoader extends Loader<Image> {
        private final FileCache mCache;

        /**
         * Constructor
         * @param cache The {@link FileCache} to store the loaded image files.
         */
        public FileCacheLoader(FileCache cache) {
            mCache = cache;
        }

        @Override
        public FileCache getFileCache() {
            return mCache;
        }

        @Override
        public void remove(Object uri) {
            mCache.remove(StringUtils.toHexString(MessageDigests.computeString(uri.toString(), Algorithm.SHA1)));
        }

        @Override
        @SuppressWarnings("synthetic-access")
        public Image load(Task<?, ?> task, String url, Object target, Object[] params, int flags, byte[] buffer) {
            final String hashKey = StringUtils.toHexString(buffer, 0, MessageDigests.computeString(url, buffer, 0, Algorithm.SHA1));
            final File imageFile = mCache.get(hashKey);
            Image result = null;

            if (imageFile.exists()) {
                // Decodes the image file, If file cache hit.
                if ((result = mDecoder.decodeImage(imageFile, target, params, flags, buffer)) != null) {
                    return result;
                }

                // Removes the image file from file cache, If decode failed.
                mCache.remove(hashKey);
            }

            if (!isTaskCancelled(task)) {
                // Loads the image from url, If the image file is not exists or decode failed.
                final File tempFile = new File(imageFile.getPath() + "." + Thread.currentThread().hashCode());
                if ((result = loadImage(task, url, tempFile, target, params, flags, buffer)) != null && FileUtils.moveFile(tempFile.getPath(), imageFile.getPath()) == 0) {
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
     * Class <tt>DefaultBinder</tt> used to bind the <tt>Bitmap</tt> to <tt>ImageView</tt>.
     * The <tt>DefaultBinder</tt> has no image cache.
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
     * <h3>Usage</h3>
     * <p>Here is an example:</p><pre>
     * mImageLoader.load(uri)
     *     .parameters(R.xml.decode_params)
     *     .placeholder(R.drawable.ic_placeholder)
     *     .transformer(R.xml.round_rect_transformer)
     *     .into(imageView);</pre>
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
            if (mParams[0] == null) {
                mParams[0] = Parameters.defaultParameters();
            }

            if (mParams[1] == null) {
                mParams[1] = BitmapTransformer.getInstance(mLoader.mModule.mContext);
            }

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
        public final LoadRequest<URI, Image> flags(int flags) {
            mFlags |= flags;
            return this;
        }

        /**
         * Equivalent to calling <tt>flags(FLAG_IGNORE_MEMORY_CACHE)</tt>.
         * @return This request.
         * @see #flags(int)
         * @see AsyncLoader#FLAG_IGNORE_MEMORY_CACHE FLAG_IGNORE_MEMORY_CACHE
         */
        public final LoadRequest<URI, Image> skipMemory() {
            mFlags |= FLAG_IGNORE_MEMORY_CACHE;
            return this;
        }

        /**
         * Sets the {@link Parameters} to decode image.
         * @param id The xml resource id of the <tt>Parameters</tt>.
         * @return This request.
         * @see #parameters(Parameters)
         */
        public final LoadRequest<URI, Image> parameters(int id) {
            mParams[0] = mLoader.mModule.getParameters(id);
            return this;
        }

        /**
         * Sets the {@link Parameters} to decode image.
         * @param parameters The <tt>Parameters</tt> to decode.
         * @return This request.
         * @see #parameters(int)
         */
        public final LoadRequest<URI, Image> parameters(Parameters parameters) {
            mParams[0] = parameters;
            return this;
        }

        /**
         * Sets the {@link Transformer} to bind image.
         * @param id The xml resource id of the <tt>Transformer</tt>.
         * @return This request.
         * @see #transformer(Transformer)
         */
        public final LoadRequest<URI, Image> transformer(int id) {
            mParams[1] = mLoader.mModule.getTransformer(id);
            return this;
        }

        /**
         * Sets the {@link Transformer} to bind image.
         * @param transformer The <tt>Transformer</tt>.
         * @return This request.
         * @see #transformer(int)
         */
        public final LoadRequest<URI, Image> transformer(Transformer<Image> transformer) {
            mParams[1] = transformer;
            return this;
        }

        /**
         * Sets the <tt>Drawable</tt> to be used when the image is loading.
         * @param id The resource id of the <tt>Drawable</tt>.
         * @return This request.
         * @see #placeholder(Drawable)
         */
        @SuppressWarnings("deprecation")
        public final LoadRequest<URI, Image> placeholder(int id) {
            mParams[2] = mLoader.mModule.mContext.getResources().getDrawable(id);
            return this;
        }

        /**
         * Sets the <tt>Drawable</tt> to be used when the image is loading.
         * @param drawable The <tt>Drawable</tt>.
         * @return This request.
         * @see #placeholder(int)
         */
        public final LoadRequest<URI, Image> placeholder(Drawable drawable) {
            mParams[2] = drawable;
            return this;
        }

        /**
         * Sets the {@link Binder} to bind the image to target.
         * @param binder The <tt>Binder</tt> to bind.
         * @return This request.
         */
        public final LoadRequest<URI, Image> binder(Binder<URI, Object, Image> binder) {
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
         * @param uri The uri to decode.
         * @param target The target, passed earlier by {@link ImageLoader#load}.
         * @param params The parameters, passed earlier by {@link ImageLoader#load}.
         * @param flags The flags, passed earlier by {@link ImageLoader#load}.
         * @param tempStorage The temporary storage to use for decoding. Suggest 16K.
         * @return The image object, or <tt>null</tt> if the image data cannot be decode.
         */
        Image decodeImage(Object uri, Object target, Object[] params, int flags, byte[] tempStorage);
    }
}
