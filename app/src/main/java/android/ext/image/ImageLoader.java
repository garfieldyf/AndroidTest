package android.ext.image;

import static android.ext.image.ImageModule.COOKIE;
import static android.ext.image.ImageModule.MAX_POOL_SIZE;
import static android.ext.image.ImageModule.PARAMETERS;
import static android.ext.image.ImageModule.PLACEHOLDER;
import static android.ext.image.ImageModule.getPlaceholder;
import static java.net.HttpURLConnection.HTTP_OK;
import android.content.Context;
import android.ext.cache.Cache;
import android.ext.cache.FileCache;
import android.ext.content.AsyncLoader;
import android.ext.content.AsyncLoader.Binder;
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
 *      app:decoder="[ BitmapDecoder | ImageDecoder | classFullName ]" /&gt;</pre>
 * @author Garfield
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ImageLoader<URI, Image> extends AsyncLoader<URI, Object, Image> implements Binder<URI, Object, Image> {
    /**
     * If set the image loader will be dump the {@link Options} when it will
     * be load image. (flags 0x00FF0000) <p>This flag can be used DEBUG mode.</p>
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
    protected ImageLoader(ImageModule<URI, Image> module, Cache<URI, Image> imageCache, FileCache fileCache, ImageDecoder<Image> decoder) {
        super(module.mExecutor, imageCache, MAX_POOL_SIZE);

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
        mRequest.mFlags  = 0;
        mRequest.mBinder = this;
        mRequest.mParams = mModule.mParamsPool.obtain();
        return mRequest;
    }

    @Override
    public void bindValue(URI uri, Object[] params, Object target, Image value, int state) {
        final ImageView view = (ImageView)target;
        if (value instanceof Bitmap) {
            view.setImageBitmap((Bitmap)value);
        } else if (value instanceof Drawable) {
            view.setImageDrawable((Drawable)value);
        } else {
            view.setImageDrawable(getPlaceholder(view.getResources(), params));
        }
    }

    @Override
    protected Image loadInBackground(Task task, URI uri, Object[] params, int flags) {
        final byte[] buffer = mModule.mBufferPool.obtain();
        try {
            return (UriUtils.matchScheme(uri) ? (Image)mLoader.load(task, uri.toString(), params, flags, buffer) : mDecoder.decodeImage(uri, params, flags, buffer));
        } finally {
            mModule.mBufferPool.recycle(buffer);
        }
    }

    @Override
    protected final void onRecycle(Object[] params) {
        Arrays.fill(params, null);  // Prevent memory leak.
        mModule.mParamsPool.recycle(params);
    }

//    /**
//     * Called on a background thread to load an image from the specified <em>uri</em>.
//     * @param task The current {@link Task} whose executing this method.
//     * @param uri The uri to load, passed earlier by {@link #load}.
//     * @param params The parameters, passed earlier by {@link #load}.
//     * @param flags Loading flags, passed earlier by {@link #load}.
//     * @param buffer The temporary byte array to used for loading image data.
//     * @return The image object, or <tt>null</tt> if the load failed or cancelled.
//     */
//    protected Image loadImage(Task task, URI uri, Object[] params, int flags, byte[] buffer) {
//        return (UriUtils.matchScheme(uri) ? (Image)mLoader.load(task, uri.toString(), params, flags, buffer) : mDecoder.decodeImage(uri, params, flags, buffer));
//    }

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
    protected Image loadImage(Task task, String url, File imageFile, Object[] params, int flags, byte[] buffer) {
        try {
            final DownloadRequest request = new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
            request.__checkDumpHeaders = false;
            return (request.download(imageFile, task, buffer) == HTTP_OK && !isTaskCancelled(task) ? mDecoder.decodeImage(imageFile, params, flags, buffer) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load image data from - " + url + "\n" + e);
            return null;
        }
    }

    /**
     * Constructor
     */
    /* package */ ImageLoader(ImageModule module, Cache imageCache) {
        super(module.mExecutor, imageCache);

        mModule  = module;
        mLoader  = null;
        mDecoder = null;
        mRequest = new LoadRequest(this);
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
         * @param params The parameters, passed earlier by {@link #load}.
         * @param flags Loading flags, passed earlier by {@link #load}.
         * @param buffer The temporary byte array to use for loading image data.
         * @return The image object, or <tt>null</tt> if the load failed or cancelled.
         */
        Object load(Task task, String url, Object[] params, int flags, byte[] buffer);
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
        public Object load(Task task, String url, Object[] params, int flags, byte[] buffer) {
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
        public Object load(Task task, String url, Object[] params, int flags, byte[] buffer) {
            final String hashKey = StringUtils.toHexString(buffer, 0, MessageDigests.computeString(url, buffer, 0, Algorithm.SHA1));
            final File imageFile = mCache.get(hashKey);
            Object result = null;

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
     * The <tt>LoadRequest</tt> class used to {@link ImageLoader} to load the image.
     */
    public static final class LoadRequest {
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
         * @see #FLAG_IGNORE_MEMORY_CACHE
         */
        public final LoadRequest skipMemory() {
            mFlags |= FLAG_IGNORE_MEMORY_CACHE;
            return this;
        }

        /**
         * Sets the {@link Parameters} to decode image.
         * @param id The xml resource id of the <tt>Parameters</tt>.
         * @return This request.
         * @see #parameters(Object)
         */
        public final LoadRequest parameters(int id) {
            mParams[PARAMETERS] = mLoader.mModule.getResource(id);
            return this;
        }

        /**
         * Sets the parameters to decode image.
         * @param parameters The <tt>Object</tt> to decode.
         * @return This request.
         * @see #parameters(int)
         */
        public final LoadRequest parameters(Object parameters) {
            mParams[PARAMETERS] = parameters;
            return this;
        }

        /**
         * Sets the <tt>Drawable</tt> to be used when the image is loading.
         * @param id The resource id of the <tt>Drawable</tt>.
         * @return This request.
         * @see #placeholder(Drawable)
         */
        public final LoadRequest placeholder(int id) {
            mParams[PLACEHOLDER] = id;
            return this;
        }

        /**
         * Sets the <tt>Drawable</tt> to be used when the image is loading.
         * @param drawable The <tt>Drawable</tt>.
         * @return This request.
         * @see #placeholder(int)
         */
        public final LoadRequest placeholder(Drawable drawable) {
            mParams[PLACEHOLDER] = drawable;
            return this;
        }

        /**
         * Sets an object by user-defined to load image.
         * @param cookie An object by user-defined.
         * @return This request.
         */
        public final LoadRequest cookie(Object cookie) {
            mParams[COOKIE] = cookie;
            return this;
        }

        /**
         * Sets the {@link Binder} to bind the image to target.
         * @param id The xml resource id of the <tt>Binder</tt>.
         * @return This request.
         * @see #binder(Binder)
         */
        public final LoadRequest binder(int id) {
            mBinder = (Binder)mLoader.mModule.getResource(id);
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
            mLoader.load(mUri, target, mFlags, mBinder, mParams);
        }

        /**
         * preloads the image with the arguments supplied to this request.
         */
//        public final void preload() {
//            if (mLoader.getCache() != Caches.emptyCache()) {
//                DebugUtils.__checkError(mUri == null, "uri == null");
//                mLoader.load(mUri, mUri, mFlags, AsyncLoader.emptyBinder(), mParams);
//            }
//        }
    }

    /**
     * The <tt>ImageDecoder</tt> class used to decode the image data.
     */
    public static interface ImageDecoder<Image> {
        /**
         * Decodes an image from the specified <em>uri</em>.
         * @param uri The uri to decode.
         * @param params The parameters, passed earlier by {@link ImageLoader#load}.
         * @param flags The flags, passed earlier by {@link ImageLoader#load}.
         * @param tempStorage The temporary storage to use for decoding. Suggest 16K.
         * @return The image object, or <tt>null</tt> if the image data cannot be decode.
         */
        Image decodeImage(Object uri, Object[] params, int flags, byte[] tempStorage);
    }
}
