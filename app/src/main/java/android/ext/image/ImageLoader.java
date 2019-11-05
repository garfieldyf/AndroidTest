package android.ext.image;

import static android.ext.image.ImageLoader.LoadRequest.PLACEHOLDER_INDEX;
import static android.ext.image.ImageLoader.LoadRequest.TRANSFORMER_INDEX;
import static java.net.HttpURLConnection.HTTP_OK;
import android.content.Context;
import android.ext.cache.Cache;
import android.ext.cache.FileCache;
import android.ext.content.AsyncLoader;
import android.ext.content.AsyncLoader.Binder;
import android.ext.image.params.Parameters;
import android.ext.image.transformer.BitmapTransformer;
import android.ext.image.transformer.Transformer;
import android.ext.net.DownloadRequest;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.Optional;
import android.ext.util.StringUtils;
import android.ext.util.UriUtils;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import java.io.File;
import java.util.Arrays;

/**
 * Class <tt>ImageLoader</tt> allows to load the image from the URI
 * on a background thread and bind it to target on the UI thread.
 * <h3>ImageLoader's generic types</h3>
 * <p>The two types used by an image loader are the following:</p>
 * <ol><li><tt>URI</tt>, The uri type of the image loader's key.</li>
 * <li><tt>Image</tt>, The image type of the load result.</li></ol>
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;[ ImageLoader | loader ]
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      class="classFullName"
 *      app:flags="[ none | noFileCache | noMemoryCache ]"
 *      app:decoder="classFullName" /&gt;</pre>
 * @author Garfield
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ImageLoader<URI, Image> extends AsyncLoader<URI, Object, Image> implements Binder<Object, Object, Object> {
    /**
     * If set the image loader will be dump the {@link Options} when
     * it will be load image. <p>This flag can be used DEBUG mode.</p>
     */
    public static final int FLAG_DUMP_OPTIONS = 0x00400000;

    private final Loader mLoader;
    private final LoadRequest mRequest;

    protected final ImageDecoder<Image> mDecoder;
    protected final ImageModule<URI, Image> mModule;

    /**
     * Constructor
     * @param module The {@link ImageModule}.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the loaded image.
     * @param fileCache May be <tt>null</tt>. The {@link FileCache} to store the loaded image files.
     * @param decoder The {@link ImageDecoder} to decode the image data.
     */
    public ImageLoader(ImageModule<URI, Image> module, Cache<URI, Image> imageCache, FileCache fileCache, ImageDecoder<Image> decoder) {
        super(module.mExecutor, Optional.ofNullable(imageCache), 48);

        mRequest = new LoadRequest(this);
        mDecoder = decoder;
        mModule  = module;
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
    public LoadRequest load(URI uri) {
        DebugUtils.__checkUIThread("load");
        mRequest.mUri = resolveUri(uri);
        mRequest.mBinder = this;
        mRequest.mParams = mModule.mParamsPool.obtain();
        return mRequest;
    }

    /**
     * Returns a <tt>Drawable</tt> with the specified <em>params</em> and <em>value</em>.
     * @param params The parameters, passed earlier by {@link Binder#bindValue}.
     * @param value The image value, passed earlier by {@link Binder#bindValue}.
     * @return The <tt>Drawable</tt> to bind to target.
     */
    public static Drawable getImageDrawable(Object[] params, Object value) {
        if (value == null) {
            return (Drawable)params[PLACEHOLDER_INDEX];
        } else if (value instanceof Drawable) {
            return (Drawable)value;
        } else {
            return ((Transformer<Object>)params[TRANSFORMER_INDEX]).transform(value);
        }
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, Object value, int state) {
        ((ImageView)target).setImageDrawable(getImageDrawable(params, value));
    }

    @Override
    protected Image loadInBackground(Task task, URI uri, Object[] params, int flags) {
        final byte[] buffer = mModule.mBufferPool.obtain();
        try {
            final Object target = getTarget(task);
            return (matchScheme(uri) ? (Image)mLoader.load(task, uri.toString(), target, params, flags, buffer) : mDecoder.decodeImage(uri, target, params, flags, buffer));
        } finally {
            mModule.mBufferPool.recycle(buffer);
        }
    }

    @Override
    protected void onRecycle(Object[] params) {
        Arrays.fill(params, null);  // Prevent memory leak.
        mModule.mParamsPool.recycle(params);
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
    protected Image loadImage(Task task, String url, File imageFile, Object target, Object[] params, int flags, byte[] buffer) {
        try {
            final DownloadRequest request = new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
            request.__checkDumpHeaders = false;
            return (request.download(imageFile.getPath(), task, buffer) == HTTP_OK && !isTaskCancelled(task) ? mDecoder.decodeImage(imageFile, target, params, flags, buffer) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load image data from - " + url + "\n" + e);
            return null;
        }
    }

    /**
     * Resolves an empty (0-length) string to <tt>null</tt>.
     */
    private static Object resolveUri(Object uri) {
        return (uri instanceof String && ((String)uri).length() == 0 ? null : uri);
    }

    /**
     * Interface <tt>Loader</tt> used to load image from the specified url.
     */
    private static interface Loader {
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
        Object load(Task task, String url, Object target, Object[] params, int flags, byte[] buffer);
    }

    /**
     * Class <tt>URLLoader</tt> is an implementation of a {@link Loader}.
     */
    private final class URLLoader implements Loader {
        private final File mCacheDir;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         */
        public URLLoader(Context context) {
            mCacheDir = FileUtils.getCacheDir(context, null);
        }

        @Override
        public Object load(Task task, String url, Object target, Object[] params, int flags, byte[] buffer) {
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
    private final class FileCacheLoader implements Loader {
        private final FileCache mCache;

        /**
         * Constructor
         * @param cache The {@link FileCache} to store the loaded image files.
         */
        public FileCacheLoader(FileCache cache) {
            mCache = cache;
        }

        @Override
        public Object load(Task task, String url, Object target, Object[] params, int flags, byte[] buffer) {
            final String hashKey = StringUtils.toHexString(buffer, 0, MessageDigests.computeString(url, buffer, 0, Algorithm.SHA1));
            final File imageFile = mCache.get(hashKey);
            Object result = null;

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
     * The <tt>LoadRequest</tt> class used to {@link ImageLoader} to load the image.
     * <h3>Usage</h3>
     * <p>Here is an example:</p><pre>
     * module.load(R.xml.image_loader, uri)
     *       .parameters(R.xml.decode_params)
     *       .transformer(R.xml.transformer)
     *       .placeholder(R.drawable.ic_placeholder)
     *       .into(imageView);</pre>
     */
    public static final class LoadRequest {
        public static final int PARAMETERS_INDEX   = 0;
        public static final int TRANSFORMER_INDEX  = 1;
        public static final int PLACEHOLDER_INDEX  = 2;
        public static final int COOKIE_INDEX       = 3;

        /* package */ Object mUri;
        /* package */ int mFlags;
        /* package */ Binder mBinder;
        /* package */ Object[] mParams;
        /* package */ final ImageLoader mLoader;

        /**
         * Constructor
         * @param loader The {@link ImageLoader}.
         */
        /* package */ LoadRequest(ImageLoader loader) {
            mLoader = loader;
        }

        /**
         * Adds the loading flags to load image.
         * @param flags Loading flags. May be any combination of
         * <tt>FLAG_XXX</tt> constants.
         * @return This request.
         */
        public final LoadRequest flags(int flags) {
            mFlags |= flags;
            return this;
        }

        /**
         * Equivalent to calling <tt>flags(FLAG_IGNORE_MEMORY_CACHE)</tt>.
         * @return This request.
         * @see #flags(int)
         * @see AsyncLoader#FLAG_IGNORE_MEMORY_CACHE
         */
        public final LoadRequest skipMemory() {
            mFlags |= FLAG_IGNORE_MEMORY_CACHE;
            return this;
        }

        /**
         * Sets the {@link Parameters} to decode image.
         * @param id The xml resource id of the <tt>Parameters</tt>.
         * @return This request.
         * @see #parameters(Parameters)
         */
        public final LoadRequest parameters(int id) {
            mParams[PARAMETERS_INDEX] = mLoader.mModule.getParameters(id);
            return this;
        }

        /**
         * Sets the {@link Parameters} to decode image.
         * @param parameters The <tt>Parameters</tt> to decode.
         * @return This request.
         * @see #parameters(int)
         */
        public final LoadRequest parameters(Parameters parameters) {
            mParams[PARAMETERS_INDEX] = parameters;
            return this;
        }

        /**
         * Sets the {@link Transformer} to bind image.
         * @param id The xml resource id of the <tt>Transformer</tt>.
         * @return This request.
         * @see #transformer(Transformer)
         */
        public final LoadRequest transformer(int id) {
            mParams[TRANSFORMER_INDEX] = mLoader.mModule.getTransformer(id);
            return this;
        }

        /**
         * Sets the {@link Transformer} to bind image.
         * @param transformer The <tt>Transformer</tt>.
         * @return This request.
         * @see #transformer(int)
         */
        public final LoadRequest transformer(Transformer transformer) {
            mParams[TRANSFORMER_INDEX] = transformer;
            return this;
        }

        /**
         * Sets the <tt>Drawable</tt> to be used when the image is loading.
         * @param id The resource id of the <tt>Drawable</tt>.
         * @return This request.
         * @see #placeholder(Drawable)
         */
        public final LoadRequest placeholder(int id) {
            mParams[PLACEHOLDER_INDEX] = mLoader.mModule.getDrawable(id);
            return this;
        }

        /**
         * Sets the <tt>Drawable</tt> to be used when the image is loading.
         * @param drawable The <tt>Drawable</tt>.
         * @return This request.
         * @see #placeholder(int)
         */
        public final LoadRequest placeholder(Drawable drawable) {
            mParams[PLACEHOLDER_INDEX] = drawable;
            return this;
        }

        /**
         * Sets an object by user-defined to load image.
         * @param cookie An object by user-defined.
         * @return This request.
         */
        public final LoadRequest cookie(Object cookie) {
            mParams[COOKIE_INDEX] = cookie;
            return this;
        }

        /**
         * Sets the {@link Binder} to bind the image to target.
         * @param id The xml resource id of the <tt>Binder</tt>.
         * @return This request.
         * @see #binder(Binder)
         */
        public final LoadRequest binder(int id) {
            mBinder = (Binder)mLoader.mModule.getBinder(id);
            return this;
        }

        /**
         * Sets the {@link Binder} to bind the image to target.
         * @param binder The <tt>Binder</tt> to bind.
         * @return This request.
         * @see #binder(int)
         */
        public final LoadRequest binder(Binder binder) {
            mBinder = binder;
            return this;
        }

        /**
         * Loads the image with the arguments supplied to this request.
         * @param target The <tt>Object</tt> to bind.
         */
        public final void into(Object target) {
            load(target, mBinder);
        }

        /**
         * Preloads the image with the arguments supplied to this request.
         */
        public final void preload() {
            DebugUtils.__checkError(mUri == null, "uri == null");
            load(mUri, AsyncLoader.emptyBinder());
        }

        private void load(Object target, Binder binder) {
            if (mParams[PARAMETERS_INDEX] == null) {
                mParams[PARAMETERS_INDEX] = Parameters.defaultParameters();
            }

            if (mParams[TRANSFORMER_INDEX] == null) {
                mParams[TRANSFORMER_INDEX] = BitmapTransformer.getInstance(mLoader.mModule.mContext);
            }

            mLoader.load(mUri, target, mFlags, binder, mParams);
            mFlags  = 0;
            mParams = null;
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