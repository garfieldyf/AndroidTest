package android.ext.image;

import static android.ext.image.ImageModule.PARAMETERS;
import static android.ext.image.ImageModule.PARAMS_LENGTH;
import static android.ext.image.ImageModule.PLACEHOLDER;
import static java.net.HttpURLConnection.HTTP_OK;
import android.ext.cache.Cache;
import android.ext.cache.FileCache;
import android.ext.content.AsyncLoader;
import android.ext.content.AsyncLoader.Binder;
import android.ext.image.params.Parameters;
import android.ext.net.DownloadRequest;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.StringUtils;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import java.io.File;
import java.util.Arrays;

/**
 * Class <tt>ImageLoader</tt> allows to load the image from the URI
 * on a background thread and bind it to target on the UI thread.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;[ ImageLoader | loader ]
 *      xmlns:android="http://schemas.android.com/apk/res/android"
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      class="classFullName"
 *      app:flags="[ none | noFileCache | noMemoryCache ]"&gt;
 *
 *      &lt;!-- Optional ImageDecoder --&gt;
 *      &lt;[ BitmapDecoder | ImageDecoder | ContactPhotoDecoder | decoder ]
 *          class="classFullName"
 *          app:attribute1="value1"
 *          app:attribute2="value2"
 *          ... ... /&gt;
 * &lt;/[ ImageLoader | loader ]&gt;</pre>
 * @author Garfield
 */
public class ImageLoader<Image> extends AsyncLoader<Object, Object, Image> implements Binder<Object, Object, Image> {
    /**
     * If set the image loader will be dump the {@link Options} when
     * it will be load image.<p>This flag can be used DEBUG mode.</p>
     */
    public static final int FLAG_DUMP_OPTIONS = 0x04000000;    /* flags 0x0F000000 */

    private final LoadRequest mRequest;
    private final Loader<Image> mLoader;

    protected final ImageModule mModule;
    protected final ImageDecoder<Image> mDecoder;

    /**
     * Constructor
     * @param module The {@link ImageModule}.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the loaded image.
     * @param fileCache May be <tt>null</tt>. The {@link FileCache} to store the loaded image files.
     * @param decoder The {@link ImageDecoder} to decode the image data.
     */
    protected ImageLoader(ImageModule module, Cache<Object, Image> imageCache, FileCache fileCache, ImageDecoder<Image> decoder) {
        super(module.mExecutor, imageCache, module.mTaskPool);

        mRequest = new LoadRequest();
        mDecoder = decoder;
        mModule  = module;
        mLoader  = (fileCache != null ? new FileCacheLoader(fileCache) : new URLLoader());
    }

    /**
     * Loads the image from the specified <em>uri</em>, bind it to the target. If the image
     * is already cached, it is bind immediately. Otherwise loads the image on a background
     * thread. <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * <h3>The default implementation accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>{@link File} (no scheme)</li>
     * <li>ftp ({@link #SCHEME_FTP})</li>
     * <li>http ({@link #SCHEME_HTTP})</li>
     * <li>https ({@link #SCHEME_HTTPS})</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android.asset ({@link #SCHEME_ANDROID_ASSET})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param uri May be <tt>null</tt>. The uri to load.
     * @return The {@link LoadRequest}.
     */
    public final LoadRequest load(Object uri) {
        DebugUtils.__checkUIThread("load");
        mRequest.mUri = resolveUri(uri);
        mRequest.mFlags  = 0;
        mRequest.mBinder = this;
        mRequest.mParams = mModule.mParamsPool.obtain();
        return mRequest;
    }

    /**
     * Equivalent to calling <tt>loadSync(uri, 0, parameters)</tt>.
     * @param uri The uri to load.
     * @param parameters May be <tt>null</tt>. The {@link Parameters} to decode image.
     * @return The image, or <tt>null</tt> if load failed or this loader was shut down.
     * @see #loadSync(Object, int, Object[])
     */
    public final Image loadSync(Object uri, Parameters parameters) {
        return loadSync(resolveUri(uri), 0, parameters);
    }

    @Override
    public Image remove(Object uri) {
        if (mLoader != null) {
            mLoader.remove(uri);
        }

        return super.remove(uri);
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, Image value, int state) {
        final ImageView view = (ImageView)target;
        if (value != null) {
            view.setImageBitmap((Bitmap)value);
        } else if ((state & STATE_LOAD_FROM_BACKGROUND) == 0) {
            ImageModule.setPlaceholder(view, params);
        }
    }

    @Override
    protected Image loadInBackground(Task task, Object uri, Object[] params, int flags) {
        final byte[] buffer = mModule.mBufferPool.obtain();
        try {
            final Object target = getTarget(task);
            final String uriString = uri.toString();
            return (matchScheme(uriString) ? mLoader.load(task, uriString, target, params, flags, buffer) : mDecoder.decodeImage(uri, target, params, flags, buffer));
        } finally {
            mModule.mBufferPool.recycle(buffer);
        }
    }

//    @Override
//    protected void onShutdown() {
//        if (mDecoder != null) {
//            mDecoder.onShutdown();
//        }
//    }

    @Override
    protected final void onRecycle(Object[] params) {
        ImageModule.__checkParameters(params, PARAMS_LENGTH - 1);
        Arrays.fill(params, null);  // Clear for recycle.
        mModule.mParamsPool.recycle(params);
    }

    /**
     * Matches the scheme of the specified <em>uri</em>. The default
     * implementation match the scheme "http", "https" and "ftp".
     */
    protected boolean matchScheme(String uri) {
        return ("http://".regionMatches(true, 0, uri, 0, 7) || "https://".regionMatches(true, 0, uri, 0, 8) || "ftp://".regionMatches(true, 0, uri, 0, 6));
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
     * @return The image, or <tt>null</tt> if the load failed or cancelled.
     */
    protected Image loadImage(Task task, String url, File imageFile, Object target, Object[] params, int flags, byte[] buffer) {
        try {
            final DownloadRequest request = new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
            request.__checkDumpHeaders = false;
            return (request.download(imageFile, task, buffer) == HTTP_OK && !isTaskCancelled(task) ? mDecoder.decodeImage(imageFile, target, params, flags, buffer) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load image data from - " + url + "\n" + e);
            return null;
        }
    }

    /**
     * Constructor
     * @param module The {@link ImageModule}.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the loaded image.
     */
    /* package */ ImageLoader(ImageModule module, Cache<Object, Image> imageCache) {
        super(module.mExecutor, imageCache, module.mTaskPool);

        mModule  = module;
        mLoader  = null;
        mDecoder = null;
        mRequest = new LoadRequest();
    }

    /**
     * Resolves an empty (0-length) string to <tt>null</tt>.
     */
    private static Object resolveUri(Object uri) {
        return (uri instanceof String && ((String)uri).isEmpty() ? null : uri);
    }

    /**
     * Interface <tt>Loader</tt> used to load image from the specified url.
     */
    private static interface Loader<Image> {
        /**
         * Removes the cache file for the specified
         * <em>uri</em> from the cache of this loader.
         * @param uri The uri to remove.
         */
        default void remove(Object uri) {
        }

        /**
         * Called on a background thread to load an image from the specified <em>url</em>.
         * @param task The current {@link Task} whose executing this method.
         * @param url The url to load.
         * @param target The <tt>Object</tt> to bind, passed earlier by {@link ImageLoader#load}.
         * @param params The parameters, passed earlier by {@link ImageLoader#load}.
         * @param flags Loading flags, passed earlier by {@link ImageLoader#load}.
         * @param buffer The temporary byte array to use for loading image data.
         * @return The image, or <tt>null</tt> if the load failed or cancelled.
         */
        Image load(Task task, String url, Object target, Object[] params, int flags, byte[] buffer);
    }

    /**
     * Class <tt>URLLoader</tt> is an implementation of a {@link Loader}.
     */
    /* package */ final class URLLoader implements Loader<Image> {
        @Override
        public Image load(Task task, String url, Object target, Object[] params, int flags, byte[] buffer) {
            final File imageFile = new File(mModule.mCacheDir, Integer.toString(Thread.currentThread().hashCode()));
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
    private final class FileCacheLoader implements Loader<Image> {
        private final FileCache mCache;

        /**
         * Constructor
         * @param cache The {@link FileCache} to store the loaded image files.
         */
        public FileCacheLoader(FileCache cache) {
            mCache = cache;
        }

        @Override
        public void remove(Object uri) {
            final String uriString = uri.toString();
            if (matchScheme(uriString)) {
                DebugUtils.__checkStartMethodTracing();
                mCache.remove(StringUtils.toHexString(MessageDigests.computeString(uriString, Algorithm.SHA1)));
                DebugUtils.__checkStopMethodTracing("ImageLoader", "FileCacheLoader.remove");
            }
        }

        @Override
        public Image load(Task task, String url, Object target, Object[] params, int flags, byte[] buffer) {
            final String hashKey = StringUtils.toHexString(buffer, 0, MessageDigests.computeString(url, buffer, 0, Algorithm.SHA1));
            final File imageFile = mCache.get(hashKey);
            Image result = null;

            if (imageFile.exists()) {
                // Decodes the image file, If exists.
                if ((result = mDecoder.decodeImage(imageFile, target, params, flags, buffer)) != null) {
                    return result;
                }

                // Removes the image file from file cache, If decode failed.
                mCache.remove(hashKey);
            }

            // Loads the image from url, If the image file is not exists or decode failed.
            final File tempFile = new File(mModule.mCacheDir, Integer.toString(Thread.currentThread().hashCode()));
            if ((result = loadImage(task, url, tempFile, target, params, flags, buffer)) != null && FileUtils.moveFile(tempFile.getPath(), imageFile.getPath()) == 0) {
                // Saves the image file to file cache, If load succeeded.
                mCache.put(hashKey, imageFile);
            } else {
                // Deletes the temp file, If load failed.
                tempFile.delete();
                DebugUtils.__checkWarning(result == null, "ImageLoader", "loadImage failed - delete tempFile = " + tempFile + ", url = " + url);
            }

            return result;
        }
    }

    /**
     * The <tt>LoadRequest</tt> class used to {@link ImageLoader} to load the image.
     * <h3>Usage</h3>
     * <p>Here is an example:</p><pre>
     * loader.load(uri)
     *     .parameters(R.xml.decode_params)
     *     .placeholder(R.drawable.ic_placeholder)
     *     .into(imageView);</pre>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final class LoadRequest {
        /* package */ Object mUri;
        /* package */ int mFlags;
        /* package */ Binder mBinder;
        /* package */ Object[] mParams;

        /**
         * Constructor
         */
        /* package */ LoadRequest() {
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
         * Equivalent to calling <tt>flags(FLAG_DUMP_OPTIONS)</tt>.
         * @return This request.
         * @see #flags(int)
         * @see #FLAG_DUMP_OPTIONS
         */
        public final LoadRequest dumpOptions() {
            mFlags |= FLAG_DUMP_OPTIONS;
            return this;
        }

        /**
         * Sets the {@link Parameters} to decode image.
         * @param id The xml resource id of the <tt>Parameters</tt>.
         * @return This request.
         * @see #parameters(Object)
         */
        public final LoadRequest parameters(int id) {
            mParams[PARAMETERS] = mModule.getResource(id, null);
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
         * Sets the {@link Binder} to bind the image to target.
         * @param id The xml resource id of the <tt>Binder</tt>.
         * @return This request.
         * @see #binder(Binder)
         */
        public final LoadRequest binder(int id) {
            mBinder = (Binder)mModule.getResource(id, null);
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
            load(mUri, target, mFlags, mBinder, mParams);
        }

//        /**
//         * preloads the image with the arguments supplied to this request.
//         */
//        public final void preload() {
//            DebugUtils.__checkError(mUri == null, "Invalid parameter - uri == null");
//            DebugUtils.__checkWarning(getCache() == null, "ImageLoader", "No image cache, invoking this method has no effect.");
//            DebugUtils.__checkWarning((mFlags & FLAG_IGNORE_MEMORY_CACHE) != 0, "ImageLoader", "The FLAG_IGNORE_MEMORY_CACHE is set, invoking this method has no effect.");
//            if (getCache() != null && (mFlags & FLAG_IGNORE_MEMORY_CACHE) == 0) {
//                load(mUri, mUri, mFlags, Binder.emptyBinder(), mParams);
//            }
//        }
    }

    /**
     * The <tt>ImageDecoder</tt> class used to decode the image data.
     */
    public static interface ImageDecoder<Image> {
//        /**
//         * Called on the UI thread when the {@link ImageLoader} has been shut
//         * down. Subclasses should override this method to clear the resources.
//         */
//        default void onShutdown() {
//        }

        /**
         * Decodes an image from the specified <em>uri</em>.
         * @param uri The uri to decode.
         * @param target The target, passed earlier by {@link ImageLoader#load}.
         * @param params The parameters, passed earlier by {@link ImageLoader#load}.
         * @param flags The flags, passed earlier by {@link ImageLoader#load}.
         * @param tempStorage The temporary storage to use for decoding. Suggest 16K.
         * @return The image, or <tt>null</tt> if the image data cannot be decode.
         */
        Image decodeImage(Object uri, Object target, Object[] params, int flags, byte[] tempStorage);
    }
}
